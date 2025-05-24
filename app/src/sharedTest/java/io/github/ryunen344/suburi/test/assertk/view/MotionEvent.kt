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

package io.github.ryunen344.suburi.test.assertk.view

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.fail

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasAction]
 */
fun Assert<MotionEvent>.hasAction(action: Int) = prop("action") { actual -> actual.action }.isEqualTo(action)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasActionButton]
 */
fun Assert<MotionEvent>.hasActionButton(actionButton: Int) = prop("actionButton") { actual ->
    if (VERSION.SDK_INT < VERSION_CODES.M) {
        fail("hasActionButton is not supported on API < 23")
    } else {
        actual.actionButton
    }
}.isEqualTo(actionButton)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasButtonState]
 */
fun Assert<MotionEvent>.hasButtonState(buttonState: Int) = prop("buttonState") { actual -> actual.buttonState }.isEqualTo(buttonState)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasDeviceId]
 */
fun Assert<MotionEvent>.hasDeviceId(deviceId: Int) = prop("deviceId") { actual -> actual.deviceId }.isEqualTo(deviceId)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasDownTime]
 */
fun Assert<MotionEvent>.hasDownTime(downTime: Long) = prop("downTime") { actual -> actual.downTime }.isEqualTo(downTime)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasEdgeFlags]
 */
fun Assert<MotionEvent>.hasEdgeFlags(edgeFlags: Int) = prop("edgeFlags") { actual -> actual.edgeFlags }.isEqualTo(edgeFlags)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasEventTime]
 */
fun Assert<MotionEvent>.hasEventTime(eventTime: Long) = prop("eventTime") { actual -> actual.eventTime }.isEqualTo(eventTime)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasFlags]
 */
fun Assert<MotionEvent>.hasFlags(flags: Int) = prop("flags") { actual -> actual.flags }.isEqualTo(flags)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasHistorySize]
 */
fun Assert<MotionEvent>.hasHistorySize(historySize: Int) = prop("historySize") { actual -> actual.historySize }.isEqualTo(historySize)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalEventTime]
 */
fun Assert<MotionEvent>.historicalEventTime(pos: Int) = transform { actual -> actual.getHistoricalEventTime(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalPointerCoords]
 */
fun Assert<MotionEvent>.historicalPointerCoords(pointerIndex: Int, pos: Int) = transform { actual ->
    PointerCoords().apply { actual.getHistoricalPointerCoords(pointerIndex, pos, this) }
}

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalPressure]
 */
fun Assert<MotionEvent>.historicalPressure(pos: Int) = transform { actual -> actual.getHistoricalPressure(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalOrientation]
 */
fun Assert<MotionEvent>.historicalOrientation(pos: Int) = transform { actual -> actual.getHistoricalOrientation(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalSize]
 */
fun Assert<MotionEvent>.historicalSize(pos: Int) = transform { actual -> actual.getHistoricalSize(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalTouchMajor]
 */
fun Assert<MotionEvent>.historicalTouchMajor(pos: Int) = transform { actual -> actual.getHistoricalTouchMajor(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalTouchMinor]
 */
fun Assert<MotionEvent>.historicalTouchMinor(pos: Int) = transform { actual -> actual.getHistoricalTouchMinor(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalToolMajor]
 */
fun Assert<MotionEvent>.historicalToolMajor(pos: Int) = transform { actual -> actual.getHistoricalToolMajor(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalToolMinor]
 */
fun Assert<MotionEvent>.historicalToolMinor(pos: Int) = transform { actual -> actual.getHistoricalToolMinor(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalX]
 */
fun Assert<MotionEvent>.historicalX(pos: Int) = transform { actual -> actual.getHistoricalX(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.historicalY]
 */
fun Assert<MotionEvent>.historicalY(pos: Int) = transform { actual -> actual.getHistoricalY(pos) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasMetaState]
 */
fun Assert<MotionEvent>.hasMetaState(metaState: Int) = prop("metaState") { actual -> actual.metaState }.isEqualTo(metaState)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.orientation]
 */
fun Assert<MotionEvent>.orientation() = orientation(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.orientation]
 */
fun Assert<MotionEvent>.orientation(pointerIndex: Int) = prop("orientation") { actual -> actual.getOrientation(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.pointerCoords]
 */
fun Assert<MotionEvent>.pointerCoords(pointerIndex: Int) = transform { actual ->
    PointerCoords().apply { actual.getPointerCoords(pointerIndex, this) }
}

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.hasPointerCount]
 */
fun Assert<MotionEvent>.hasPointerCount(pointerCount: Int) = prop("pointerCount") { actual -> actual.pointerCount }.isEqualTo(pointerCount)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.pointerId]
 */
fun Assert<MotionEvent>.pointerId(pointerIndex: Int) = prop("pointerId") { actual -> actual.getPointerId(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.pointerProperties]
 */
fun Assert<MotionEvent>.pointerProperties(pointerIndex: Int) = transform { actual ->
    PointerProperties().apply { actual.getPointerProperties(pointerIndex, this) }
}

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.pressure]
 */
fun Assert<MotionEvent>.pressure() = pressure(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.pressure]
 */
fun Assert<MotionEvent>.pressure(pointerIndex: Int) = prop("pressure") { actual -> actual.getPressure(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.rawX]
 */
fun Assert<MotionEvent>.rawX() = rawX(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.rawX]
 */
fun Assert<MotionEvent>.rawX(pointerIndex: Int) = prop("rawX") { actual -> actual.getRawX(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.rawY]
 */
fun Assert<MotionEvent>.rawY() = rawY(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.rawY]
 */
fun Assert<MotionEvent>.rawY(pointerIndex: Int) = prop("rawY") { actual -> actual.getRawY(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.size]
 */
fun Assert<MotionEvent>.size() = size(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.size]
 */
fun Assert<MotionEvent>.size(pointerIndex: Int) = prop("size") { actual -> actual.getSize(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.toolMajor]
 */
fun Assert<MotionEvent>.toolMajor() = toolMajor(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.toolMajor]
 */
fun Assert<MotionEvent>.toolMajor(pointerIndex: Int) = prop("toolMajor") { actual -> actual.getToolMajor(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.toolMinor]
 */
fun Assert<MotionEvent>.toolMinor() = toolMinor(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.toolMinor]
 */
fun Assert<MotionEvent>.toolMinor(pointerIndex: Int) = prop("toolMinor") { actual -> actual.getToolMinor(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.touchMajor]
 */
fun Assert<MotionEvent>.touchMajor() = touchMajor(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.touchMajor]
 */
fun Assert<MotionEvent>.touchMajor(pointerIndex: Int) = prop("touchMajor") { actual -> actual.getTouchMajor(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.touchMinor]
 */
fun Assert<MotionEvent>.touchMinor() = touchMinor(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.touchMinor]
 */
fun Assert<MotionEvent>.touchMinor(pointerIndex: Int) = prop("touchMinor") { actual -> actual.getTouchMinor(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.x]
 */
fun Assert<MotionEvent>.x() = x(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.x]
 */
fun Assert<MotionEvent>.x(pointerIndex: Int) = prop("x") { actual -> actual.getX(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.xPrecision]
 */
fun Assert<MotionEvent>.xPrecision() = prop("xPrecision") { actual -> actual.xPrecision }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.y]
 */
fun Assert<MotionEvent>.y() = y(0)

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.y]
 */
fun Assert<MotionEvent>.y(pointerIndex: Int) = prop("y") { actual -> actual.getY(pointerIndex) }

/**
 * assertk extension of [androidx.test.ext.truth.view.MotionEventSubject.yPrecision]
 */
fun Assert<MotionEvent>.yPrecision() = prop("yPrecision") { actual -> actual.yPrecision }
