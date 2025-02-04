package app.rive.runtime.kotlin.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.rive.runtime.kotlin.controllers.RiveFileController
import app.rive.runtime.kotlin.core.errors.RiveException
import app.rive.runtime.kotlin.test.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RiveControllerTest {
    private val testUtils = TestUtils()
    private val appContext = testUtils.context

    @Test
    fun initEmpty() {
        val fileController = RiveFileController()
        assert(!fileController.hasPlayingAnimations)
        fileController.play()
        // Without file nor artboard there's nothing to play...
        assert(!fileController.hasPlayingAnimations)
    }

    @Test
    fun initEmptyAddFile() {
        val fileController = RiveFileController()
        assertFalse(fileController.hasPlayingAnimations)
        val bytes = appContext.resources
            .openRawResource(R.raw.off_road_car_blog)
            .use {
                it.readBytes()
            }
        val file = File(bytes)
        assertNull(fileController.file)
        assertNull(fileController.activeArtboard)

        fileController.setRiveFile(file)
        assertNotNull(fileController.activeArtboard)
        // Setting the File with autoplay (default) starts the controller.
        assertTrue(fileController.hasPlayingAnimations)
    }

    @Test
    fun initEmptyAddFilePlayNoArtboard() {
        val file = appContext.resources
            .openRawResource(R.raw.off_road_car_blog)
            .use {
                File(it.readBytes())
            }
        val fileController = RiveFileController(file = file, autoplay = false)
        assertNull(fileController.activeArtboard)
        assertFalse(fileController.hasPlayingAnimations)
        // Cannot play without an active artboard.
        fileController.play()
        assertFalse(fileController.hasPlayingAnimations)
    }

    @Test
    fun initEmptyAddFilePlayAnimation() {
        val file = appContext.resources
            .openRawResource(R.raw.off_road_car_blog)
            .use {
                File(it.readBytes())
            }
        val fileController = RiveFileController(file = file, autoplay = false)
        assertNull(fileController.activeArtboard)
        assertFalse(fileController.hasPlayingAnimations)

        fileController.apply {
            // Select the first artboard.
            selectArtboard()
            // Play the first animation
            play()
        }
        assertTrue(fileController.hasPlayingAnimations)
        assertEquals("idle", fileController.animations.first().name)
    }

    @Test
    fun controllerOnStart() {
        var hasStarted = false
        val controller = RiveFileController {
            // onStart() callback
            hasStarted = true
        }
        val file = appContext.resources
            .openRawResource(R.raw.off_road_car_blog)
            .use {
                File(it.readBytes())
            }
        assertFalse(hasStarted)
        controller.setRiveFile(file)
        assertTrue(hasStarted)
    }

    @Test
    fun multipleControllersSameFile() {
        val file = appContext.resources
            .openRawResource(R.raw.off_road_car_blog)
            .use {
                File(it.readBytes())
            }
        val firstController = RiveFileController()
        assertNull(firstController.activeArtboard)
        assertFalse(firstController.hasPlayingAnimations)

        firstController.setRiveFile(file)
        assertNotNull(firstController.activeArtboard)
        assertTrue(firstController.hasPlayingAnimations)
        assertEquals(1, firstController.animations.size)

        val secondController = RiveFileController()
        assertNull(secondController.activeArtboard)
        assertFalse(secondController.hasPlayingAnimations)
        // Setting the *same* file.
        secondController.setRiveFile(file)
        assertNotNull(secondController.activeArtboard)
        assertTrue(secondController.hasPlayingAnimations)
        assertEquals(1, secondController.animations.size)

        // Different Artboard instances.
        assertNotEquals(firstController.activeArtboard, secondController.activeArtboard)
        // Different Animation instances.
        assertNotEquals(
            firstController.animations.first(),
            secondController.animations.first(),
        )
    }
}
