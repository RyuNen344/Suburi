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

package io.github.ryunen344.suburi.ui.screen.cube

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Suppress("ModifierWithoutDefault", "LambdaParameterEventTrailing")
@Composable
internal fun CachedCanvas(modifier: Modifier, onBuildDrawCache: CacheDrawScope.() -> DrawResult) =
    Spacer(modifier = modifier.drawWithCache(onBuildDrawCache))

@Suppress("ModifierWithoutDefault", "LambdaParameterEventTrailing")
@Composable
internal fun CachedCanvas(modifier: Modifier, contentDescription: String, onBuildDrawCache: CacheDrawScope.() -> DrawResult) =
    Spacer(
        modifier = modifier
            .drawWithCache(onBuildDrawCache)
            .semantics { this.contentDescription = contentDescription },
    )
