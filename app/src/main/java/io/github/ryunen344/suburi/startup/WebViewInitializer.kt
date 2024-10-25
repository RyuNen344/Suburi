package io.github.ryunen344.suburi.startup

import android.content.Context
import android.os.Build
import android.webkit.WebView
import androidx.startup.Initializer
import androidx.webkit.WebViewCompat
import io.github.ryunen344.suburi.BuildConfig
import timber.log.Timber

class WebViewInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val packageInfo = WebViewCompat.getCurrentWebViewPackage(context)
        if (packageInfo != null) {
            Timber.d(
                buildString {
                    append("Loading ")
                    append(packageInfo.packageName)
                    append(" version ")
                    append(packageInfo.versionName)
                    append(" (code ")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        append(packageInfo.longVersionCode)
                    } else {
                        @Suppress("DEPRECATION")
                        append(packageInfo.versionCode)
                    }
                    append(")")
                },
            )
        } else {
            Timber.d("WebView package not found")
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
