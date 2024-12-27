package io.github.ryunen344.suburi

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.ryunen344.suburi.test.rules.TimberTestRule
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {

    @get:Rule
    val timberRule = TimberTestRule()

    @Test
    fun addition_isCorrect() {
        Timber.d("start")
        assertEquals(4, 2 + 2)
        Timber.d("end")
    }

    @Test
    fun useDebugProbes() = runTest {
        DebugProbes.withDebugProbes {
            DebugProbes.printScope(this)
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            backgroundScope.launch(testDispatcher) {
                // Context of the app under test.
                val appContext = InstrumentationRegistry.getInstrumentation().targetContext
                assertEquals("io.github.ryunen344.suburi", appContext.packageName)
                DebugProbes.printScope(this)
            }
            DebugProbes.printScope(backgroundScope)
            val coroutineInfo = DebugProbes.dumpCoroutinesInfo()
            if (coroutineInfo.isNotEmpty()) {
                coroutineInfo.forEach(::println)
            } else {
                println("coroutineInfo is empty")
            }
        }
    }
}
