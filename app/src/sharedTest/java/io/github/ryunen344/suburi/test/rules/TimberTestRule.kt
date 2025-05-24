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

package io.github.ryunen344.suburi.test.rules

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import timber.log.Timber

class TimberTestRule : TestWatcher() {

    private var tree: Timber.DebugTree? = null

    override fun starting(description: Description?) {
        Timber.DebugTree().also {
            tree = it
            Timber.plant(it)
        }
    }

    override fun finished(description: Description?) {
        tree?.let(Timber::uproot)
        tree = null
    }
}
