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

package io.github.ryunen344.suburi.ui.screen.uuid

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ryunen344.suburi.ui.screen.Routes
import io.github.ryunen344.suburi.ui.screen.toRoutes
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UuidViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    fun hoge() {
        runCatching {
            savedStateHandle.toRoutes<Routes.Uuid>().uuid
        }.onSuccess {
            Timber.d("$it")
        }.onFailure(Timber::e)
    }
}
