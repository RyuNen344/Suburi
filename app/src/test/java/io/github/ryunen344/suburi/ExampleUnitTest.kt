package io.github.ryunen344.suburi

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ryunen344.suburi.test.rules.TimberTestRule
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
}
