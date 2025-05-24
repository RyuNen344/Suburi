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

import android.content.Context
import android.media.MediaDataSource
import android.os.Build
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.CacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import coil3.svg.SvgDecoder
import coil3.video.MediaDataSourceFetcher
import coil3.video.VideoFrameDecoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.ryunen344.suburi.util.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import okio.FileSystem
import javax.inject.Singleton

typealias CoilUri = coil3.Uri

@Module
@InstallIn(SingletonComponent::class)
class CoilModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // disable service loader due to organized by dagger
            .serviceLoaderEnabled(false)
            .components {
                addFetcherFactories {
                    buildList {
                        // network
                        add(
                            OkHttpNetworkFetcherFactory(
                                callFactory = { okHttpClient.newBuilder().build() },
                                cacheStrategy = { CacheStrategy.DEFAULT },
                            ) to CoilUri::class,
                        )

                        // media
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            add(MediaDataSourceFetcher.Factory() to MediaDataSource::class)
                        }
                    }
                }

                addDecoderFactories {
                    buildList {
                        // svg
                        add(SvgDecoder.Factory())

                        // gif
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            add(AnimatedImageDecoder.Factory())
                        } else {
                            add(GifDecoder.Factory())
                        }

                        // video
                        add(VideoFrameDecoder.Factory())
                    }
                }
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
                    .build()
            }
            .coroutineContext(dispatcher)
            .logger(TimberCoilLogger())
            .build()
    }
}
