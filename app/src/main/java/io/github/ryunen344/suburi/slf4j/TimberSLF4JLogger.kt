package io.github.ryunen344.suburi.slf4j

import android.util.Log
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter
import timber.log.Timber

class TimberSLF4JLogger(val tag: String) : Logger {

    override fun getName(): String {
        return tag
    }

    override fun isTraceEnabled(): Boolean {
        return enabledInternal()
    }

    override fun isTraceEnabled(marker: Marker?): Boolean {
        return enabledInternal()
    }

    override fun trace(msg: String?) {
        logInternal(Log.VERBOSE, msg)
    }

    override fun trace(format: String?, arg: Any?) {
        logInternal(Log.VERBOSE, format, arg)
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.VERBOSE, format, arg1, arg2)
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        logInternal(Log.VERBOSE, format, arguments)
    }

    override fun trace(msg: String?, t: Throwable?) {
        logInternal(Log.VERBOSE, msg, t)
    }

    override fun trace(marker: Marker?, msg: String?) {
        logInternal(Log.VERBOSE, msg)
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        logInternal(Log.VERBOSE, format, arg)
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.VERBOSE, format, arg1, arg2)
    }

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        logInternal(Log.VERBOSE, format, argArray)
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        logInternal(Log.VERBOSE, msg, t)
    }

    override fun isDebugEnabled(): Boolean {
        return enabledInternal()
    }

    override fun isDebugEnabled(marker: Marker?): Boolean {
        return enabledInternal()
    }

    override fun debug(msg: String?) {
        logInternal(Log.DEBUG, msg)
    }

    override fun debug(format: String?, arg: Any?) {
        logInternal(Log.DEBUG, format, arg)
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.DEBUG, format, arg1, arg2)
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        logInternal(Log.DEBUG, format, arguments)
    }

    override fun debug(msg: String?, t: Throwable?) {
        logInternal(Log.DEBUG, msg, t)
    }

    override fun debug(marker: Marker?, msg: String?) {
        logInternal(Log.DEBUG, msg)
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        logInternal(Log.DEBUG, format, arg)
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.DEBUG, format, arg1, arg2)
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        logInternal(Log.DEBUG, format, arguments)
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        logInternal(Log.DEBUG, msg, t)
    }

    override fun isInfoEnabled(): Boolean {
        return enabledInternal()
    }

    override fun isInfoEnabled(marker: Marker?): Boolean {
        return enabledInternal()
    }

    override fun info(msg: String?) {
        logInternal(Log.INFO, msg)
    }

    override fun info(format: String?, arg: Any?) {
        logInternal(Log.INFO, format, arg)
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.INFO, format, arg1, arg2)
    }

    override fun info(format: String?, vararg arguments: Any?) {
        logInternal(Log.INFO, format, arguments)
    }

    override fun info(msg: String?, t: Throwable?) {
        logInternal(Log.INFO, msg, t)
    }

    override fun info(marker: Marker?, msg: String?) {
        logInternal(Log.INFO, msg)
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        logInternal(Log.INFO, format, arg)
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.INFO, format, arg1, arg2)
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        logInternal(Log.INFO, format, arguments)
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        logInternal(Log.INFO, msg, t)
    }

    override fun isWarnEnabled(): Boolean {
        return enabledInternal()
    }

    override fun isWarnEnabled(marker: Marker?): Boolean {
        return enabledInternal()
    }

    override fun warn(msg: String?) {
        logInternal(Log.WARN, msg)
    }

    override fun warn(format: String?, arg: Any?) {
        logInternal(Log.WARN, format, arg)
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.WARN, format, arg1, arg2)
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        logInternal(Log.WARN, format, arguments)
    }

    override fun warn(msg: String?, t: Throwable?) {
        logInternal(Log.WARN, msg, t)
    }

    override fun warn(marker: Marker?, msg: String?) {
        logInternal(Log.WARN, msg)
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        logInternal(Log.WARN, format, arg)
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.WARN, format, arg1, arg2)
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        logInternal(Log.WARN, format, arguments)
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        logInternal(Log.WARN, msg, t)
    }

    override fun isErrorEnabled(): Boolean {
        return enabledInternal()
    }

    override fun isErrorEnabled(marker: Marker?): Boolean {
        return enabledInternal()
    }

    override fun error(msg: String?) {
        logInternal(Log.ERROR, msg)
    }

    override fun error(format: String?, arg: Any?) {
        logInternal(Log.ERROR, format, arg)
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.ERROR, format, arg1, arg2)
    }

    override fun error(format: String?, vararg arguments: Any?) {
        logInternal(Log.ERROR, format, arguments)
    }

    override fun error(msg: String?, t: Throwable?) {
        logInternal(Log.ERROR, msg, t)
    }

    override fun error(marker: Marker?, msg: String?) {
        logInternal(Log.ERROR, msg)
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        logInternal(Log.ERROR, format, arg)
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logInternal(Log.ERROR, format, arg1, arg2)
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        logInternal(Log.ERROR, format, arguments)
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        logInternal(Log.ERROR, msg, t)
    }

    private fun enabledInternal(): Boolean {
        return Timber.treeCount > 0
    }

    private fun logInternal(priority: Int, format: String?, vararg arguments: Any?) {
        val ft = MessageFormatter.arrayFormat(format, arguments)
        Timber.tag(tag).log(priority, ft.throwable, ft.message)
    }
}
