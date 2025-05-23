package io.github.ryunen344.suburi.util.timber

import timber.log.Timber

class NoopTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // noop
    }
}
