/**
 * File: OnDeviceGenAiWarmupContractTest.kt
 * Description: Documents Open Core warm-up behavior — conservative status without device downloads (CI-safe).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   `./gradlew :fluvian-sdk-core:testDebugUnitTest`
 *
 * Usage example:
 *   ./gradlew :fluvian-sdk-core:testDebugUnitTest --tests com.fluvian.sdk.core.aicore.OnDeviceGenAiWarmupContractTest
 */
package com.fluvian.sdk.core.aicore

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OnDeviceGenAiWarmupContractTest {

    @Test
    fun awaitFeatureReady_returnsUnavailableWithoutNetworkOrDevice() =
        runBlocking {
            val status = OnDeviceGenAiWarmup.awaitFeatureReady(timeoutMs = 5_000L)
            assertEquals(OnDeviceGenAiReadiness.UNAVAILABLE, status)
        }
}
