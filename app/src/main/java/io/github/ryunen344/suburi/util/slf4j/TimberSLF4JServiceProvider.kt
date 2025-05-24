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

package io.github.ryunen344.suburi.util.slf4j

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
