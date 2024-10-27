package io.github.ryunen344.suburi.ui.screen

import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.core.os.BundleCompat
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Serializable
sealed class Routes {
    @Serializable
    data object Top : Routes()

    @Serializable
    data class Uuid(val uuid: WrappedUuid) : Routes()

    @Serializable
    data class Structures(val structure: Structure) : Routes()
}

inline val <reified T : Routes> KClass<T>.typeMap: Map<KType, @JvmSuppressWildcards NavType<*>>
    get() = when (this) {
        Routes.Top::class -> emptyMap()
        Routes.Uuid::class -> WrappedUuid.typeMap
        Routes.Structures::class -> Structure.typeMap
        else -> error("unexpected type parameter")
    }

val <T : Routes> KClass<T>.deepLinks: List<NavDeepLink>
    get() = when (this) {
        Routes.Top::class -> emptyList()
        Routes.Uuid::class -> listOf(
            navDeepLink<Routes.Uuid>(basePath = "https://www.example.com/uuid", typeMap = WrappedUuid.typeMap),
        )

        Routes.Structures::class -> emptyList()
        else -> error("unexpected type parameter")
    }

@Serializable
data class WrappedUuid(@Serializable(with = UUIDSerializer::class) val value: UUID) : java.io.Serializable {
    companion object {
        val navType = object : NavType<WrappedUuid>(false) {
            override fun get(bundle: Bundle, key: String): WrappedUuid? {
                return BundleCompat.getSerializable(bundle, key, WrappedUuid::class.java)
            }

            override fun parseValue(value: String): WrappedUuid {
                Timber.d("parseValue $value")
                return runCatching {
                    UUID.fromString(value)
                        .let(::WrappedUuid)
                        .also { Timber.d("from Deeplink $it") }
                }.getOrElse {
                    Timber.d("from Navigation")
                    Uri.decode(value)
                        .hexToByteArray()
                        .inputStream()
                        .use(::ObjectInputStream)
                        .use(ObjectInputStream::readObject) as WrappedUuid
                }
            }

            override fun put(bundle: Bundle, key: String, value: WrappedUuid) {
                bundle.putSerializable(key, value)
            }

            override fun serializeAsValue(value: WrappedUuid): String {
                Timber.d("serializeAsValue $value")
                val hex = ByteArrayOutputStream().apply {
                    use(::ObjectOutputStream).use { it.writeObject(value) }
                }.toByteArray().toHexString()
                return Uri.encode(hex)
            }
        }

        val typeMap = mapOf(typeOf<WrappedUuid>() to navType)
    }
}

@Serializable
data class Structure(val value1: String, val value2: Long, val value3: String) : java.io.Serializable {
    companion object {
        val navType = object : NavType<Structure>(false) {
            override fun get(bundle: Bundle, key: String): Structure? {
                return BundleCompat.getSerializable(bundle, key, Structure::class.java)
            }

            override fun parseValue(value: String): Structure {
                Timber.d("parseValue $value")
                return Uri.decode(value)
                    .hexToByteArray()
                    .inputStream()
                    .use(::ObjectInputStream)
                    .use(ObjectInputStream::readObject) as Structure
            }

            override fun put(bundle: Bundle, key: String, value: Structure) {
                bundle.putSerializable(key, value)
            }

            override fun serializeAsValue(value: Structure): String {
                Timber.d("serializeAsValue $value")
                val hex = ByteArrayOutputStream().apply {
                    use(::ObjectOutputStream).use { it.writeObject(value) }
                }.toByteArray().toHexString()
                return Uri.encode(hex)
            }
        }

        val typeMap = mapOf(typeOf<Structure>() to navType)

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
