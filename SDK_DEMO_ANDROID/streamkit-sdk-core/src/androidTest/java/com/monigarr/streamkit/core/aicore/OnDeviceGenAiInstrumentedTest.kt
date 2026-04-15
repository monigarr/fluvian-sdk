/**
 * File: OnDeviceGenAiInstrumentedTest.kt
 * Description: Instrumented tests for ML Kit on-device GenAI path; requires a physical device with AICore for full coverage.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 *
 * Usage:
 *   Run `./gradlew :streamkit-sdk-core:connectedDebugAndroidTest` on a supported Pixel (or OEM device with GenAI).
 *
 * Usage example:
 *   ./gradlew :streamkit-sdk-core:connectedDebugAndroidTest --tests com.monigarr.streamkit.core.aicore.OnDeviceGenAiInstrumentedTest
 */
package com.monigarr.streamkit.core.aicore

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.mlkit.genai.common.FeatureStatus
import kotlinx.coroutines.runBlocking
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
    fun warmup_returnsAvailableOrUnavailable() =
        runBlocking {
            assumeTrue("API 26+ required for ML Kit GenAI", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            val status = OnDeviceGenAiWarmup.awaitFeatureReady(timeoutMs = 120_000L)
            assertTrue(
                status == FeatureStatus.AVAILABLE ||
                    status == FeatureStatus.UNAVAILABLE,
            )
        }

    @Test
    fun whenFeatureAvailable_predictUsesOnDeviceModelPath() =
        runBlocking {
            assumeTrue("API 26+ required for ML Kit GenAI", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            val status = OnDeviceGenAiWarmup.awaitFeatureReady(timeoutMs = 180_000L)
            assumeTrue(
                "Skip when Gemini Nano / AICore is not AVAILABLE on this device (use a supported Pixel with network).",
                status == FeatureStatus.AVAILABLE,
            )

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
            var usedOnDevice = false
            repeat(5) {
                val decision = provider.predict(qos)
                assertTrue(decision in OptimizationDecision.entries)
                if (OnDeviceGenAiDiagnostics.lastPredictionUsedOnDeviceModel) {
                    usedOnDevice = true
                    return@repeat
                }
            }
            assertTrue(
                "Expected on-device parse path after retries; lastPredictionUsedOnDeviceModel=" +
                    OnDeviceGenAiDiagnostics.lastPredictionUsedOnDeviceModel,
                usedOnDevice,
            )
            provider.shutdown()
        }
}
