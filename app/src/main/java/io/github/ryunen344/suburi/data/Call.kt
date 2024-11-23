package io.github.ryunen344.suburi.data

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import kotlin.coroutines.resumeWithException

internal suspend fun Call.executeAsync(): Response {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    continuation.resume(response) { _, _, _ ->
                        response.closeQuietly()
                    }
                }
            },
        )
    }
}
