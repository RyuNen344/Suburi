package io.github.ryunen344.suburi.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

fun Serializable.serialize(): ByteArray {
    return ByteArrayOutputStream(DEFAULT_BUFFER_SIZE).use { byteArrayOutputStream ->
        ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(this) }
        byteArrayOutputStream.toByteArray()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> ByteArray.deserialize(): T {
    return ObjectInputStream(ByteArrayInputStream(this)).use(ObjectInputStream::readObject) as T
}
