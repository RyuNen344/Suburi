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

package io.github.ryunen344.suburi.ui.screen.mutton

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ryunen344.mutton.compose.rememberStateMachine
import io.github.ryunen344.suburi.ui.screen.top.Greeting

@Composable
internal fun MuttonScreen() {
    val coroutineScope = rememberCoroutineScope()
    val stateMachine = rememberStateMachine<MatterStateMachine, MatterState>(initialState = MatterState.Solid) { state ->
        MatterStateMachine(initialState = state, context = coroutineScope.coroutineContext)
    }
    val state by stateMachine.state.collectAsStateWithLifecycle()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Greeting(name = "state $state")

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Melt)
                },
            ) {
                Text("do Melt")
            }

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Freeze)
                },
            ) {
                Text("do Freeze")
            }

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Vaporize)
                },
            ) {
                Text("do Vaporize")
            }

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Condense)
                },
            ) {
                Text("do Condense")
            }
        }
    }
}
