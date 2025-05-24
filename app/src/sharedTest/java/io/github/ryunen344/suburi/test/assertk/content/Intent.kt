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

package io.github.ryunen344.suburi.test.assertk.content

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import assertk.Assert
import assertk.all
import assertk.assertions.containsAtLeast
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import io.github.ryunen344.suburi.test.assertk.internal.FlagUtil
import kotlin.reflect.KClass

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.extras]
 */
fun Assert<Intent>.extras() = prop("extras", Intent::getExtras)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.categories]
 */
fun Assert<Intent>.categories() = prop("categories", Intent::getCategories)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasComponentClass]
 */
fun Assert<Intent>.hasComponentClass(componentClass: Class<*>) = hasComponentClass(componentClass.name)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasComponentClass]
 */
fun Assert<Intent>.hasComponentClass(componentClass: KClass<*>) = hasComponentClass(componentClass.java)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasComponent]
 */
fun Assert<Intent>.hasComponent(packageName: String, className: String) = all {
    hasComponentPackage(packageName)
    hasComponentClass(className)
}

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasComponent]
 */
fun Assert<Intent>.hasComponent(component: ComponentName) = hasComponent(component.packageName, component.className)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasComponentClass]
 */
fun Assert<Intent>.hasComponentClass(className: String) = prop("className") { it.component?.className }.isEqualTo(className)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasComponentPackage]
 */
fun Assert<Intent>.hasComponentPackage(packageName: String) = prop("packageName") { it.component?.packageName }.isEqualTo(packageName)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasPackage]
 */
fun Assert<Intent>.hasPackage(packageName: String?) = prop("package") { it.`package` }.isEqualTo(packageName)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasAction]
 */
fun Assert<Intent>.hasAction(action: String?) = prop("action") { it.action }.isEqualTo(action)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasNoAction]
 */
fun Assert<Intent>.hasNoAction() = prop("action") { it.action }.isNull()

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasData]
 */
fun Assert<Intent>.hasData(uri: Uri) = prop("data") { it.data }.isEqualTo(uri)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasNoData]
 */
fun Assert<Intent>.hasNoData() = prop("data") { it.data }.isNull()

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasType]
 */
fun Assert<Intent>.hasType(type: String) = prop("type") { it.type }.isEqualTo(type)

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasNoType]
 */
fun Assert<Intent>.hasNoType() = prop("type") { it.type }.isNull()

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.hasFlags]
 */
fun Assert<Intent>.hasFlags(flag: Int) = prop("flags") { FlagUtil.flagNames(it.flags) }.containsAtLeast(FlagUtil.flagNames(flag))

/**
 * assertk extension of [androidx.test.ext.truth.content.IntentSubject.filtersEquallyTo]
 */
fun Assert<Intent>.filtersEquallyTo(intent: Intent?) = transform { actual -> actual.filterEquals(intent) }.isTrue()
