package io.github.ryunen344.suburi.slf4j

import android.os.Build
import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern

class TimberSLF4JLoggerFactory : ILoggerFactory {

    private val loggers: ConcurrentMap<String, Logger> = ConcurrentHashMap()

    override fun getLogger(name: String?): Logger {
        val tag = name?.let(::createTag) ?: TAG_ANONYMOUS
        return loggers[tag] ?: TimberSLF4JLogger(tag).also { loggers[tag] = it }
    }

    private fun createTag(name: String): String {
        var tag = name
        val matcher = ANONYMOUS_CLASS.matcher(tag)
        if (matcher.find()) {
            tag = matcher.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tag
        } else {
            tag.substring(0, MAX_TAG_LENGTH)
        }
    }

    private companion object {
        const val MAX_TAG_LENGTH = 23
        val ANONYMOUS_CLASS: Pattern = Pattern.compile("(\\$\\d+)+$")
        const val TAG_ANONYMOUS: String = "anonymous"
    }
}
