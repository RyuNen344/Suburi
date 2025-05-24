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

import android.view.MotionEvent.PointerCoords
import assertk.Assert
import assertk.assertions.prop

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.x]
 */
fun Assert<PointerCoords>.x() = prop("x") { it.x }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.y]
 */
fun Assert<PointerCoords>.y() = prop("y") { it.y }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.orientation]
 */
fun Assert<PointerCoords>.orientation() = prop("orientation") { it.orientation }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.pressure]
 */
fun Assert<PointerCoords>.pressure() = prop("pressure") { it.pressure }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.size]
 */
fun Assert<PointerCoords>.size() = prop("size") { it.size }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.toolMajor]
 */
fun Assert<PointerCoords>.toolMajor() = prop("toolMajor") { it.toolMajor }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.toolMinor]
 */
fun Assert<PointerCoords>.toolMinor() = prop("toolMinor") { it.toolMinor }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.touchMinor]
 */
fun Assert<PointerCoords>.touchMinor() = prop("touchMinor") { it.touchMinor }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.touchMajor]
 */
fun Assert<PointerCoords>.touchMajor() = prop("touchMajor") { it.touchMajor }

/**
 * assertk extension of [androidx.test.ext.truth.view.PointerCoordsSubject.axisValue]
 */
fun Assert<PointerCoords>.axisValue(axis: Int) = prop("axisValue") { it.getAxisValue(axis) }
