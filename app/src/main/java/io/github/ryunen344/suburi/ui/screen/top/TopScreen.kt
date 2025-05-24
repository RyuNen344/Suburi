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

package io.github.ryunen344.suburi.ui.screen.top

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ryunen344.mutton.compose.rememberStateMachine
import io.github.ryunen344.suburi.state.MatterAction
import io.github.ryunen344.suburi.state.MatterState
import io.github.ryunen344.suburi.state.MatterStateMachine
import io.github.ryunen344.suburi.ui.theme.SuburiTheme
import timber.log.Timber

@Composable
internal fun TopScreen(savedStateHandle: SavedStateHandle, onClickUuid: () -> Unit, onClickStructure: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val stateMachine = rememberStateMachine<MatterStateMachine, MatterState>(initialState = MatterState.Solid) { state ->
        MatterStateMachine(initialState = state, context = coroutineScope.coroutineContext)
    }
    val state by stateMachine.state.collectAsStateWithLifecycle()

    var strings1 by remember { mutableStateOf("") }
    var strings2 by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        Timber.wtf("TopScreen savedStateHandle $savedStateHandle")
        Timber.wtf("TopScreen savedStateHandle ${savedStateHandle.get<Bundle>("TopScreen")}")
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            when (state) {
                MatterState.Gas -> Greeting(name = "state $state")
                MatterState.Liquid -> Greeting(name = "state $state")
                MatterState.Solid -> Greeting(name = "state $state")
            }
            TextField(
                value = strings1,
                onValueChange = { strings1 = it },
                label = { Text("strings1") },
            )
            TextField(
                value = strings2,
                onValueChange = { strings2 = it },
                label = { Text("strings2") },
            )
            Greeting(name = "Android")
            Greeting(name = "😀")
            Greeting(name = "😭")
            Button(onClickUuid) {
                Text("navigate uuid")
            }
            Button(onClickStructure) {
                Text("navigate structure")
            }

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Melt)
                }
            ) {
                Text("do Melt")
            }

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Freeze)
                }
            ) {
                Text("do Freeze")
            }

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Vaporize)
                }
            ) {
                Text("do Vaporize")
            }

            Button(
                onClick = {
                    stateMachine.dispatch(MatterAction.Condense)
                }
            ) {
                Text("do Condense")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    SuburiTheme {
        Greeting("Android")
    }
}
