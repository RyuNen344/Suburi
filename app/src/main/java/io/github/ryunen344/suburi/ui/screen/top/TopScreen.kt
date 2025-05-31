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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.ryunen344.suburi.coil.LocalImageLoader
import io.github.ryunen344.suburi.coil.PreviewImage
import io.github.ryunen344.suburi.ui.theme.SuburiTheme

@Composable
internal fun TopScreen(
    onClickCube: () -> Unit,
    onClickMutton: () -> Unit,
    onClickStructure: () -> Unit,
    onClickUuid: () -> Unit,
    onClickWebView: () -> Unit,
) {
    val imageLoader = LocalImageLoader.current
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Greeting(name = "Android")
            Greeting(name = "😀")
            Greeting(name = "😭")
            Button(onClickCube) {
                Text("navigate cube")
            }
            Button(onClickMutton) {
                Text("navigate mutton")
            }
            Button(onClickStructure) {
                Text("navigate structure")
            }
            Button(onClickUuid) {
                Text("navigate uuid")
            }
            Button(onClickWebView) {
                Text("navigate webview")
            }

            AsyncImage(
                model = "https://lgtm-images.lgtmeow.com/2024/08/28/15/cc3ffe5c-bccc-4f72-8df2-9e3eb244f896.webp",
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            AsyncImage(
                model = "https://lgtm-images.lgtmeow.com/2024/08/28/15/5215c1fb-c596-46eb-bb5b-53de314dd7b4.webp",
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            AsyncImage(
                model = "https://lgtm-images.lgtmeow.com/2024/08/28/15/cfe8eb1f-1743-4bc4-a09a-4b01ebf0cc31.webp",
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            AsyncImage(
                model = "https://lgtm-images.lgtmeow.com/2024/08/28/16/53f6121d-62b3-45f0-adcc-1fbf3c76640e.webp",
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            AsyncImage(
                model = "https://lgtm-images.lgtmeow.com/2022/08/17/19/0bd6a3f6-9077-4236-8cb2-8fb0eaf9d5d5.webp",
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
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

@Preview(showBackground = true)
@Composable
private fun TopScreenPreview() {
    PreviewImage {
        SuburiTheme {
            TopScreen(
                onClickCube = {},
                onClickMutton = {},
                onClickStructure = {},
                onClickUuid = {},
                onClickWebView = {},
            )
        }
    }
}
