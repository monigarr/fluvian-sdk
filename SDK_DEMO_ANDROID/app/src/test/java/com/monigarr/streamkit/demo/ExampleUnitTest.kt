package com.monigarr.streamkit.demo

import com.monigarr.streamkit.core.EchelonProgramInfo
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * File: ExampleUnitTest.kt
 * Description: JVM unit tests for the demo application module (sanity and regression smoke).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.4
 *
 * Usage:
 *   Run via `./gradlew :app:testDebugUnitTest` from SDK_DEMO_ANDROID or the Echelon CI workflow.
 *
 * Usage example:
 *   ./gradlew :app:testDebugUnitTest
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    /**
     * The SDK FEATURES badge uses [BuildConfig.VERSION_NAME]. It must match [EchelonProgramInfo.DOCUMENT_VERSION]
     * (README / PRD / ARCHITECTURE / publishing sketch) or releases drift. Echelon CI runs this module's unit tests.
     */
    @Test
    fun demoVersionName_matchesEchelonDocumentVersion() {
        assertEquals(
            "app versionName (BuildConfig) must equal EchelonProgramInfo.DOCUMENT_VERSION; bump both together.",
            EchelonProgramInfo.DOCUMENT_VERSION,
            BuildConfig.VERSION_NAME,
        )
    }
}
