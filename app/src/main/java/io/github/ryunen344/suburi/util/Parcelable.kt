package io.github.ryunen344.suburi.util

import android.os.Parcel
import android.os.Parcelable
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Suppress("TooGenericExceptionCaught")
inline fun <T : Parcel?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.recycleFinally(exception)
    }
}

@Suppress("TooGenericExceptionCaught")
fun Parcel?.recycleFinally(cause: Throwable?): Unit = when {
    this == null -> {}
    cause == null -> recycle()
    else ->
        try {
            recycle()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}

fun Parcelable.parcel(): ByteArray {
    return Parcel.obtain().use { parcel ->
        writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        parcel.marshall()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Parcelable> ByteArray.unparcel(clazz: Class<T>): T {
    val creator = clazz.getDeclaredField("CREATOR").get(null) as? Parcelable.Creator<T>
        ?: throw IllegalArgumentException("Could not access CREATOR field in class ${clazz.simpleName}")
    return Parcel.obtain().use { parcel ->
        parcel.unmarshall(this, 0, this.size)
        parcel.setDataPosition(0)
        creator.createFromParcel(parcel)
    }
}
