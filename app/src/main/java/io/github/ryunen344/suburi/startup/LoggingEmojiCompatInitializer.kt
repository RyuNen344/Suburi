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

package io.github.ryunen344.suburi.startup

import android.content.Context
import android.graphics.Color
import android.os.Looper
import android.os.Process
import androidx.core.os.HandlerCompat
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.tracing.Trace
import io.github.ryunen344.suburi.BuildConfig
import timber.log.Timber
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * initialize [androidx.emoji2.text.EmojiCompat] with logging
 *
 * see also [androidx.emoji2.text.EmojiCompatInitializer]
 */
class LoggingEmojiCompatInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        @Suppress("RestrictedApi")
        val factory = object : DefaultEmojiCompatConfig.DefaultEmojiCompatConfigFactory(null) {
            override fun create(context: Context): EmojiCompat.Config? {
                return super.create(context)?.apply {
                    setMetadataLoadStrategy(EmojiCompat.LOAD_STRATEGY_MANUAL)
                    setReplaceAll(true)
                    setUseEmojiAsDefaultStyle(true)
                    setEmojiSpanIndicatorEnabled(BuildConfig.DEBUG)
                    setEmojiSpanIndicatorColor(Color.GREEN)
                    registerInitCallback(
                        object : EmojiCompat.InitCallback() {
                            override fun onInitialized() {
                                Timber.d("EmojiCompat initialized")
                            }

                            override fun onFailed(throwable: Throwable?) {
                                Timber.e(throwable, "EmojiCompat initialization failed")
                            }
                        },
                    )
                    if (this is FontRequestEmojiCompatConfig) {
                        setLoadingExecutor(
                            ThreadPoolExecutor(
                                0,
                                1,
                                FONT_LOAD_TIMEOUT_SECONDS,
                                TimeUnit.SECONDS,
                                LinkedBlockingDeque(),
                                ThreadFactory { Thread(it, THREAD_NAME).apply { priority = Process.THREAD_PRIORITY_BACKGROUND } },
                            ),
                        )
                    }
                }
            }
        }
        factory.create(context)?.let(EmojiCompat::init)
        delayUntilFirstResume(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(TimberInitializer::class.java, ProcessLifecycleInitializer::class.java)
    }

    /**
     * see also [androidx.emoji2.text.EmojiCompatInitializer.delayUntilFirstResume]
     */
    private fun delayUntilFirstResume(context: Context) {
        val initializer = AppInitializer.getInstance(context)
        val lifecycle = initializer.initializeComponent(ProcessLifecycleInitializer::class.java).lifecycle
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                loadEmojiCompatAfterDelay()
                lifecycle.removeObserver(this)
            }
        })
    }

    /**
     * see also [androidx.emoji2.text.EmojiCompatInitializer.loadEmojiCompatAfterDelay]
     */
    private fun loadEmojiCompatAfterDelay() {
        val handler = HandlerCompat.createAsync(Looper.getMainLooper())
        handler.postDelayed(LoadEmojiCompatRunnable(), STARTUP_THREAD_CREATION_DELAY_MS)
    }

    /**
     * see also [androidx.emoji2.text.EmojiCompatInitializer.LoadEmojiCompatRunnable]
     */
    private class LoadEmojiCompatRunnable : Runnable {
        override fun run() {
            try {
                Trace.beginSection("LoggingEmojiCompatInitializer.run")
                if (EmojiCompat.isConfigured()) {
                    EmojiCompat.get().load()
                } else {
                    Timber.d("EmojiCompat has not configured, yet")
                }
            } finally {
                Trace.endSection()
            }
        }
    }

    private companion object {
        const val FONT_LOAD_TIMEOUT_SECONDS = 15L
        const val STARTUP_THREAD_CREATION_DELAY_MS = 500L
        const val THREAD_NAME = "LoggingEmojiCompatInitializer"
    }
}
