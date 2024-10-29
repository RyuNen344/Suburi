package io.github.ryunen344.suburi.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import androidx.navigation.NavType
import io.github.ryunen344.suburi.util.parcel
import io.github.ryunen344.suburi.util.unparcel
import timber.log.Timber
import java.util.Base64
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("FunctionName")
inline fun <reified T : Parcelable> ParcelableNavTypeMap(): Map<KType, ParcelableNavType<T>> =
    mapOf(typeOf<T>() to ParcelableNavType<T>())

@Suppress("FunctionName")
inline fun <reified T : Parcelable> ParcelableNavTypeMap(
    noinline onParseValue: ((String) -> T?),
): Map<KType, ParcelableNavType<T>> = mapOf(typeOf<T>() to ParcelableNavType<T>(onParseValue))

inline fun <reified T : Parcelable> ParcelableNavType() = ParcelableNavType(T::class.java)

inline fun <reified T : Parcelable> ParcelableNavType(noinline onParseValue: ((String) -> T?)) =
    ParcelableNavType(T::class.java, onParseValue)

class ParcelableNavType<T : Parcelable>(
    val clazz: Class<T>,
    private val onParseValue: ((String) -> T?)? = null,
) : NavType<T>(false) {

    override val name: String = clazz.name

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): T? {
        return BundleCompat.getParcelable(bundle, key, clazz)
    }

    override fun parseValue(value: String): T {
        Timber.d("parseValue $value")
        return onParseValue?.invoke(value) ?: Base64.getUrlDecoder().decode(value).unparcel(clazz)
    }

    override fun serializeAsValue(value: T): String {
        Timber.d("serializeAsValue $value")
        return Base64.getUrlEncoder().encodeToString(value.parcel())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableNavType<*>

        if (clazz != other.clazz) return false
        if (onParseValue != other.onParseValue) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + (onParseValue?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        return result
    }
}

@Suppress("FunctionName")
inline fun <reified T : Parcelable> NullableParcelableNavTypeMap(): Map<KType, NullableParcelableNavType<T>> =
    mapOf(typeOf<T>() to NullableParcelableNavType<T>())

@Suppress("FunctionName")
inline fun <reified T : Parcelable> NullableParcelableNavTypeMap(
    noinline onParseValue: ((String) -> T?),
): Map<KType, NullableParcelableNavType<T>> = mapOf(typeOf<T>() to NullableParcelableNavType<T>(onParseValue))

inline fun <reified T : Parcelable> NullableParcelableNavType() = NullableParcelableNavType(T::class.java)

inline fun <reified T : Parcelable> NullableParcelableNavType(noinline onParseValue: ((String) -> T?)) =
    NullableParcelableNavType(T::class.java, onParseValue)

class NullableParcelableNavType<T : Parcelable>(
    val clazz: Class<T>,
    private val onParseValue: ((String) -> T?)? = null,
) : NavType<T?>(true) {

    override val name: String = clazz.name

    override fun put(bundle: Bundle, key: String, value: T?) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): T? {
        return BundleCompat.getParcelable(bundle, key, clazz)
    }

    override fun parseValue(value: String): T? {
        Timber.d("parseValue $value")
        return if (value == "null") null else onParseValue?.invoke(value) ?: Base64.getUrlDecoder().decode(value).unparcel(clazz)
    }

    override fun serializeAsValue(value: T?): String {
        Timber.d("serializeAsValue $value")
        return if (value == null) {
            "null"
        } else {
            Base64.getUrlEncoder().encodeToString(value.parcel())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NullableParcelableNavType<*>

        if (clazz != other.clazz) return false
        if (onParseValue != other.onParseValue) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + (onParseValue?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        return result
    }
}
