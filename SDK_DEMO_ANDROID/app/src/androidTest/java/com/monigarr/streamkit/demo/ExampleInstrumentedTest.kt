package com.monigarr.streamkit.demo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * File: ExampleInstrumentedTest.kt
 * Description: On-device instrumentation smoke tests for the demo application package identity.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.1.0
 *
 * Usage:
 *   Run via `./gradlew :app:connectedDebugAndroidTest` with a device or emulator available.
 *
 * Usage example:
 *   ./gradlew :app:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.monigarr.streamkit.demo", appContext.packageName)
    }
}
