/**
 * File: OnDeviceGenAiInstrumentedTest.kt
 * Description: Instrumented smoke tests for on-device GenAI façade in Open Core (deterministic stub path).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Run `./gradlew :fluvian-sdk-core:connectedDebugAndroidTest` on an emulator or device.
 *
 * Usage example:
 *   ./gradlew :fluvian-sdk-core:connectedDebugAndroidTest --tests com.fluvian.sdk.core.aicore.OnDeviceGenAiInstrumentedTest
 */
package com.fluvian.sdk.core.aicore

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class OnDeviceGenAiInstrumentedTest {

    @get:Rule
    val globalTimeout: Timeout = Timeout(6L, TimeUnit.MINUTES)

    @Test
    fun warmup_returnsUnavailableInOpenCore() =
        runBlocking {
            assumeTrue("API 26+ required for GenAI façade tests", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            val status = OnDeviceGenAiWarmup.awaitFeatureReady(timeoutMs = 120_000L)
            assertEquals(OnDeviceGenAiReadiness.UNAVAILABLE, status)
        }

    @Test
    fun onDeviceProvider_returnsDeterministicDecision() =
        runBlocking {
            assumeTrue("API 26+ required for GenAI façade tests", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            val cfg = AIConfig(AIProviderType.ON_DEVICE_GENAI, modelName = "nano")
            val provider = OnDeviceGenAiProvider(app)
            provider.initialize(cfg)
            val qos =
                QoSData(
                    bitrate = 3_000_000,
                    bufferHealth = 8000L,
                    droppedFrames = 0,
                    networkSpeedKbps = 8000,
                )
            val decision = provider.predict(qos)
            assertEquals(OptimizationDecision.STABILIZE, decision)
            assertTrue(decision in OptimizationDecision.entries)
            provider.shutdown()
        }
}
