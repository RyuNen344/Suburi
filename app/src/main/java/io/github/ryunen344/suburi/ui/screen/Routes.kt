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

package io.github.ryunen344.suburi.ui.screen

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.ComposeNavigatorDestinationBuilder
import androidx.navigation.get
import androidx.navigation.internalToRoute
import androidx.navigation.navDeepLink
import androidx.navigation.serialization.decodeArguments
import io.github.ryunen344.suburi.navigation.ParcelableNavTypeMap
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import timber.log.Timber
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType

val onWrappedUuidParse: ((String) -> WrappedUuid?) = { value ->
    runCatching {
        UUID.fromString(value)
            .let(::WrappedUuid)
            .also { Timber.d("from Deeplink $it") }
    }.getOrElse {
        Timber.d("from Navigation")
        null
    }
}

@Serializable
sealed class Routes {
    @Serializable
    data object Cube : Routes()

    @Serializable
    data object Mutton : Routes()

    @Serializable
    data class Structures(val structure: Structure) : Routes()

    @Serializable
    data object Top : Routes()

    @Serializable
    data class Uuid(val uuid: WrappedUuid) : Routes()
}

inline val <reified T : Routes> KClass<T>.typeMap: Map<KType, @JvmSuppressWildcards NavType<*>>
    get() = when (this) {
        Routes.Cube::class -> emptyMap()
        Routes.Mutton::class -> emptyMap()
        Routes.Structures::class -> ParcelableNavTypeMap<Structure>()
        Routes.Top::class -> emptyMap()
        Routes.Uuid::class -> ParcelableNavTypeMap<WrappedUuid>(onParseValue = onWrappedUuidParse)
        else -> error("unexpected type parameter")
    }

val <T : Routes> KClass<T>.deepLinks: List<NavDeepLink>
    get() = when (this) {
        Routes.Cube::class -> emptyList()
        Routes.Mutton::class -> emptyList()
        Routes.Structures::class -> emptyList()
        Routes.Top::class -> emptyList()
        Routes.Uuid::class -> listOf(
            navDeepLink<Routes.Uuid>(
                basePath = "https://www.example.com/uuid",
                typeMap = ParcelableNavTypeMap<WrappedUuid>(onParseValue = onWrappedUuidParse),
            ),
        )

        else -> error("unexpected type parameter")
    }

@Parcelize
@Serializable
class WrappedUuid(@Serializable(with = UUIDSerializer::class) val value: UUID) : java.io.Serializable, Parcelable {
    companion object {
        private const val serialVersionUID: Long = -1L
    }
}

@Parcelize
@Serializable
class Structure(val value1: String, val value2: Long, val value3: String) : java.io.Serializable, Parcelable {
    companion object {
        private const val serialVersionUID: Long = -1L

        fun random(): Structure {
            return Structure(
                value1 = UUID.randomUUID().toString(),
                value2 = UUID.randomUUID().mostSignificantBits,
                value3 = UUID.randomUUID().toString(),
            )
        }
    }
}

class UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.util.UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

@Suppress("UNCHECKED_CAST", "MaxLineLength")
inline fun <reified T : Routes> NavGraphBuilder.routes(
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?)? = null,
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?)? = null,
    noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?)? = enterTransition,
    noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?)? = exitTransition,
    noinline sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards SizeTransform?)? = null,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    routes(
        route = T::class as KClass<Routes>,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform,
        content = content,
    )
}

fun NavGraphBuilder.routes(
    route: KClass<Routes>,
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?)? = null,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?)? = exitTransition,
    sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards SizeTransform?)? = null,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    destination(
        ComposeNavigatorDestinationBuilder(
            provider[ComposeNavigator::class],
            route,
            route.typeMap,
            content,
        ).apply {
            route.deepLinks.forEach { deepLink -> deepLink(deepLink) }
            this.enterTransition = enterTransition
            this.exitTransition = exitTransition
            this.popEnterTransition = popEnterTransition
            this.popExitTransition = popExitTransition
            this.sizeTransform = sizeTransform
        },
    )
}

@Suppress("RestrictedApi")
inline fun <reified T : Routes> SavedStateHandle.toRoutes(): T {
    return internalToRoute(T::class, T::class.typeMap)
}

@Suppress("RestrictedApi")
inline fun <reified T : Routes> NavBackStackEntry.toRoutes(): T {
    val bundle = arguments ?: Bundle()
    val typeMap = destination.arguments.mapValues { it.value.type }
    return serializer<T>().decodeArguments(bundle, typeMap)
}
