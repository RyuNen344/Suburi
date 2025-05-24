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

package io.github.ryunen344.suburi.coil

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import coil3.ColorImage
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.size.Dimension
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

val LocalImageLoader = staticCompositionLocalOf<ImageLoader> { error("CompositionLocal LocalImageLoader not present") }

@Composable
fun PreviewImage(imageLoader: ImageLoader = PreviewImageLoader, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        content = content,
    )
}

@Stable
object PreviewImageLoader : ImageLoader {

    val colors = listOf(
        Color.DKGRAY,
        Color.GRAY,
        Color.LTGRAY,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA,
    )

    var imageFunction: suspend (request: ImageRequest) -> ImageResult = { request ->
        val size = request.sizeResolver.size()
        val width = when (val d = size.width) {
            is Dimension.Pixels -> d.px
            Dimension.Undefined -> 0
        }
        val height = when (val d = size.height) {
            is Dimension.Pixels -> d.px
            Dimension.Undefined -> 0
        }
        SuccessResult(
            image = ColorImage(
                color = colors.random(),
                width = width,
                height = height,
            ),
            request = request,
        )
    }

    private val scope = CoroutineScope(CoroutineName("PreviewImageLoader") + Dispatchers.Default + SupervisorJob())

    override val defaults: ImageRequest.Defaults = ImageRequest.Defaults()

    override val components: ComponentRegistry = ComponentRegistry()

    override val diskCache: DiskCache?
        get() = null

    override val memoryCache: MemoryCache?
        get() = null

    override fun enqueue(request: ImageRequest): Disposable {
        val job = scope.async { result(request) }
        return getDisposable(job)
    }

    override suspend fun execute(request: ImageRequest): ImageResult {
        val job = scope.async {
            result(request)
        }
        return getDisposable(job).job.await()
    }

    override fun newBuilder(): ImageLoader.Builder {
        throw UnsupportedOperationException()
    }

    override fun shutdown() {
        // noop
    }

    private suspend fun result(
        imageRequest: ImageRequest,
    ): ImageResult {
        return imageFunction(imageRequest)
    }

    fun getDisposable(
        job: Deferred<ImageResult>,
    ): Disposable {
        return object : Disposable {
            override val job: Deferred<ImageResult>
                get() = job

            override val isDisposed: Boolean
                get() = !job.isActive

            override fun dispose() {
                if (isDisposed) return
                job.cancel()
            }
        }
    }
}
