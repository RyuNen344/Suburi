package io.github.ryunen344.suburi.test.rules

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import timber.log.Timber

class TimberTestRule : TestWatcher() {

    private var tree: Timber.DebugTree? = null

    override fun starting(description: Description?) {
        Timber.DebugTree().also {
            tree = it
            Timber.plant(it)
        }
    }

    override fun finished(description: Description?) {
        tree?.let(Timber::uproot)
        tree = null
    }
}
