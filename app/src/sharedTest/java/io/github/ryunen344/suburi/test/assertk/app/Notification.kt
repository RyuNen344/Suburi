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

package io.github.ryunen344.suburi.test.assertk.app

import android.app.Notification
import assertk.Assert
import assertk.assertions.containsAtLeast
import assertk.assertions.containsNone
import assertk.assertions.prop
import io.github.ryunen344.suburi.test.assertk.internal.FlagUtil

/**
 * assertk extension of [androidx.test.ext.truth.app.NotificationSubject.extras]
 */
fun Assert<Notification>.extras() = prop("extras", Notification::extras)

/**
 * assertk extension of [androidx.test.ext.truth.app.NotificationSubject.contentIntent]
 */
fun Assert<Notification>.contentIntent() = prop("contentIntent", Notification::contentIntent)

/**
 * assertk extension of [androidx.test.ext.truth.app.NotificationSubject.deleteIntent]
 */
fun Assert<Notification>.deleteIntent() = prop("deleteIntent", Notification::deleteIntent)

/**
 * assertk extension of [androidx.test.ext.truth.app.NotificationSubject.tickerText]
 */
fun Assert<Notification>.tickerText() = prop("tickerText") { if (it.tickerText != null) it.tickerText.toString() else null }

/**
 * assertk extension of [androidx.test.ext.truth.app.NotificationSubject.hasFlags]
 */
fun Assert<Notification>.hasFlags(flags: Int) = prop("flags") { FlagUtil.flagNames(it.flags) }.containsAtLeast(FlagUtil.flagNames(flags))

/**
 * assertk extension of [androidx.test.ext.truth.app.NotificationSubject.doesNotHaveFlags]
 */
fun Assert<Notification>.doesNotHaveFlags(flags: Int) =
    prop("flags") { FlagUtil.flagNames(it.flags) }.containsNone(FlagUtil.flagNames(flags))
