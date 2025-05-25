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

package io.github.ryunen344.suburi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import io.github.ryunen344.suburi.coil.LocalImageLoader
import io.github.ryunen344.suburi.ui.screen.Routes
import io.github.ryunen344.suburi.ui.screen.WrappedUuid
import io.github.ryunen344.suburi.ui.screen.cube.CubeScreen
import io.github.ryunen344.suburi.ui.screen.mutton.MuttonScreen
import io.github.ryunen344.suburi.ui.screen.routes
import io.github.ryunen344.suburi.ui.screen.structure.StructureScreen
import io.github.ryunen344.suburi.ui.screen.toRoutes
import io.github.ryunen344.suburi.ui.screen.top.TopScreen
import io.github.ryunen344.suburi.ui.screen.uuid.UuidScreen
import io.github.ryunen344.suburi.ui.theme.SuburiTheme
import io.github.ryunen344.suburi.util.coroutines.DefaultDispatcher
import io.github.ryunen344.suburi.util.coroutines.IoDispatcher
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var httpClient: dagger.Lazy<HttpClient>

    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    @Inject
    @DefaultDispatcher
    lateinit var defaultDispatcher: dagger.Lazy<CoroutineDispatcher>

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: dagger.Lazy<CoroutineDispatcher>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SuburiTheme {
                val navController = rememberNavController()
                val onNewIntentListener = remember { Consumer<Intent>(navController::handleDeepLink) }
                DisposableEffect(onNewIntentListener) {
                    addOnNewIntentListener(onNewIntentListener)
                    onDispose {
                        removeOnNewIntentListener(onNewIntentListener)
                    }
                }

                CompositionLocalProvider(LocalImageLoader provides imageLoader.get()) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Top::class,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        routes<Routes.Cube> {
                            CubeScreen()
                        }
                        routes<Routes.Mutton> {
                            MuttonScreen()
                        }
                        routes<Routes.Structures> {
                            StructureScreen(structure = it.toRoutes<Routes.Structures>().structure)
                        }
                        routes<Routes.Top> {
                            TopScreen(
                                onClickCube = {
                                    navController.navigate(Routes.Cube)
                                },
                                onClickMutton = {
                                    navController.navigate(Routes.Mutton)
                                },
                                onClickUuid = {
                                    navController.navigate(Routes.Uuid(WrappedUuid(UUID.randomUUID())))
                                },
                                onClickStructure = ::request,
                            )
                        }
                        routes<Routes.Uuid> {
                            UuidScreen(uuid = it.toRoutes<Routes.Uuid>().uuid)
                        }
                    }
                }
            }
        }
    }

    private fun request() {
        lifecycleScope.async(defaultDispatcher.get()) {
            runCatching {
                withContext(ioDispatcher.get()) {
                    httpClient.get().post {
                        url("https://www.google.com")
                        contentType(io.ktor.http.ContentType.Application.Json)
                        @Suppress("MagicNumber")
                        setBody(
                            listOf(
                                Customer(3, 34546, "Jet", true),
                                Customer(3, 34546, "Jet", true),
                                Customer(3, 34546, "Jet", true),
                                Customer(3, 34546, "Jet", true),
                                Customer(3, 34546, "Jet", true),
                                Customer(3, 34546, "Jet", true),
                            ),
                        )
                    }.body<Customer>()
                }
            }.onSuccess { response ->
                Timber.d("ktor response $response")
            }.onFailure {
                Timber.e(it)
            }
        }
    }
}

@Serializable
data class Customer(val userId: Int, val id: Int, val title: String, val completed: Boolean)
