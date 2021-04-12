package app.rive.runtime.kotlin

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

import app.rive.runtime.kotlin.test.R;


@RunWith(AndroidJUnit4::class)
class RiveFileLoadTest {

    @Test(expected = RiveException::class)
    fun loadFormat6() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("app.rive.runtime.kotlin.test", appContext.packageName)

        Rive.init()
        File(appContext.resources.openRawResource(R.raw.sample6).readBytes())
        assert(false);
    }

    @Test(expected = RiveException::class)
    fun loadJunk() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("app.rive.runtime.kotlin.test", appContext.packageName)

        Rive.init()

        File(appContext.resources.openRawResource(R.raw.junk).readBytes())
        assert(false);
    }

    @Test
    fun loadFormatFlux() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("app.rive.runtime.kotlin.test", appContext.packageName)

        Rive.init()
        var file = File(appContext.resources.openRawResource(R.raw.flux_capacitor).readBytes())
        assertEquals(file.artboard().animationCount(), 1);
    }

    @Test
    fun loadFormatBuggy() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("app.rive.runtime.kotlin.test", appContext.packageName)

        Rive.init()
        var file = File(appContext.resources.openRawResource(R.raw.off_road_car_blog).readBytes())
        assertEquals(file.artboard().animationCount(), 5);
    }
}