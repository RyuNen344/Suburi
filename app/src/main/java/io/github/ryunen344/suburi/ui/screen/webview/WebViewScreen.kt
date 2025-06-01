/*
 * Copyright (C) 2025 RyuNen344
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE.md
 */

package io.github.ryunen344.suburi.ui.screen.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.webkit.ServiceWorkerClientCompat
import androidx.webkit.ServiceWorkerControllerCompat
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewFeature
import okhttp3.Cache
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http.HttpMethod
import okio.FileSystem
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

@Composable
internal fun WebViewScreen(
    okHttpClient: OkHttpClient,
) {
    val cachedOkHttpClient = remember {
        okHttpClient.newBuilder()
            .cookieJar(
                object : CookieJar {
                    private val store: ConcurrentHashMap<HttpUrl, List<Cookie>> = ConcurrentHashMap()

                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        return store.getOrDefault(url, emptyList())
                    }

                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        store[url] = store[url].orEmpty() + cookies
                    }
                },
            )
            .cache(
                Cache(
                    directory = (FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "web_view_cache").toFile(),
                    maxSize = 500L * 1024L * 1024L, // 500 MB
                ),
            )
            .build()
    }

    var savedView: WebView? by remember { mutableStateOf(null) }
    var savedState: Bundle? by rememberSaveable { mutableStateOf(null) }
    var canGoBack: Boolean by remember { mutableStateOf(false) }

    BackHandler(canGoBack) {
        savedView?.goBack()
    }

    LifecycleResumeEffect(savedView) {
        savedView?.onResume()
        onPauseOrDispose {
            savedView?.onPause()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                @SuppressLint("SetJavaScriptEnabled")
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    savedState?.let(::restoreState)
                    savedView = this

                    if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BASIC_USAGE)) {
                        val swController = ServiceWorkerControllerCompat.getInstance()
                        swController.setServiceWorkerClient(
                            object : ServiceWorkerClientCompat() {
                                override fun shouldInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
                                    val url = request.url.toString().toHttpUrlOrNull()
                                    return if (url != null) {
                                        val response = cachedOkHttpClient.newCall(
                                            Request.Builder()
                                                .method(
                                                    method = request.method,
                                                    body = if (HttpMethod.permitsRequestBody(request.method)) {
                                                        "".toRequestBody()
                                                    } else {
                                                        null
                                                    },
                                                )
                                                .url(url)
                                                .headers(request.requestHeaders.toHeaders())
                                                .build(),
                                        ).execute()
                                        val contentType = response.body?.contentType()
                                        val charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                                        WebResourceResponse(
                                            response.body?.contentType()?.let { "${it.type}/${it.subtype}" } ?: "text/plain",
                                            charset.name(),
                                            response.body?.byteStream(),
                                        )
                                    } else {
                                        null
                                    }
                                }
                            },
                        )
                        with(swController.serviceWorkerWebSettings) {
                            // do stuff
                        }
                    }

                    webViewClient = object : WebViewClientCompat() {
                        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                            val url = request.url.toString().toHttpUrlOrNull()
                            return if (url != null) {
                                val response = cachedOkHttpClient.newCall(
                                    Request.Builder()
                                        .url(url)
                                        .method(
                                            method = request.method,
                                            body = if (HttpMethod.permitsRequestBody(request.method)) {
                                                "".toRequestBody()
                                            } else {
                                                null
                                            },
                                        )
                                        .headers(request.requestHeaders.toHeaders())
                                        .build(),
                                ).execute()
                                val contentType = response.body?.contentType()
                                val charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                                WebResourceResponse(
                                    response.body?.contentType()?.let { "${it.type}/${it.subtype}" } ?: "text/plain",
                                    charset.name(),
                                    response.body?.byteStream(),
                                )
                            } else {
                                super.shouldInterceptRequest(view, request)
                            }
                        }

                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceErrorCompat) {
                            super.onReceivedError(view, request, error)
                        }

                        override fun onLoadResource(view: WebView?, url: String?) {
                            super.onLoadResource(view, url)
                        }

                        override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                            canGoBack = view.canGoBack()
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                            Timber.tag("WebView")
                                .log(
                                    consoleMessage.priority,
                                    "${consoleMessage.message()} ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}",
                                )
                            return false
                        }
                    }

                    clipToPadding = false
                }
            },
            update = { view ->
                view.loadUrl("https://www.google.com")
            },
            onReset = { view ->
                savedState = Bundle().apply { view.saveState(this) }
            },
            onRelease = { view ->
                savedState = null
                savedView = null
                view.destroy()
            },
        )
    }
}

private val ConsoleMessage.priority: Int
    get() = when (messageLevel()) {
        ConsoleMessage.MessageLevel.TIP -> Log.VERBOSE
        ConsoleMessage.MessageLevel.LOG -> Log.INFO
        ConsoleMessage.MessageLevel.WARNING -> Log.WARN
        ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
        ConsoleMessage.MessageLevel.DEBUG -> Log.DEBUG
    }
