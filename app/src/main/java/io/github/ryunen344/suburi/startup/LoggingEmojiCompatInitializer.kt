package io.github.ryunen344.suburi.startup

import android.content.Context
import android.graphics.Color
import android.os.Looper
import android.os.Process
import android.os.Trace
import androidx.core.os.HandlerCompat
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.startup.AppInitializer
import androidx.startup.Initializer
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
                        }
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
                            )
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
                    Timber.d("EmojiCompat has already configured")
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
