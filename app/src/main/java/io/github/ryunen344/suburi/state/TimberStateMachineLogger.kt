package io.github.ryunen344.suburi.state

import io.github.ryunen344.mutton.log.Logger
import timber.log.Timber

internal val TimberStateMachineLogger = object : Logger() {
    override fun log(
        tag: String,
        level: Level,
        throwable: Throwable?,
        message: (() -> String)?,
    ) {
        message?.let {
            Timber.tag(tag).log(level.ordinal, throwable, it())
        }
    }
}
