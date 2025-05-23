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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.ryunen344.suburi.ui.theme.SuburiTheme

@Composable
internal fun TopScreen(onClickUuid: () -> Unit, onClickStructure: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            Greeting(
                name = "Android",
            )
            Greeting(
                name = "😀",
            )
            Greeting(
                name = "😭",
            )
            Button(onClickUuid) {
                Text("navigate uuid")
            }
            Button(onClickStructure) {
                Text("navigate structure")
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
