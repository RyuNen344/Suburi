package io.github.ryunen344.suburi.slf4j

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.NOPMDCAdapter
import org.slf4j.helpers.NOP_FallbackServiceProvider
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

class TimberSLF4JServiceProvider : SLF4JServiceProvider {

    private val loggerFactory by lazy { TimberSLF4JLoggerFactory() }
    private val markerFactory by lazy { BasicMarkerFactory() }
    private val mdcAdapter by lazy { NOPMDCAdapter() }

    override fun getLoggerFactory(): ILoggerFactory {
        return loggerFactory
    }

    override fun getMarkerFactory(): IMarkerFactory {
        return markerFactory
    }

    override fun getMDCAdapter(): MDCAdapter {
        return mdcAdapter
    }

    override fun getRequestedApiVersion(): String {
        return NOP_FallbackServiceProvider.REQUESTED_API_VERSION
    }

    override fun initialize() {
        // lazy initialization
    }
}
