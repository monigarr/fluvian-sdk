package com.monigarr.streamkit.core

import androidx.media3.common.C
import com.monigarr.streamkit.core.aicore.AIConfig
import com.monigarr.streamkit.core.aicore.AIProviderFactory
import com.monigarr.streamkit.core.aicore.AIProviderType
import com.monigarr.streamkit.core.aicore.OpenAIProvider
import com.monigarr.streamkit.core.internal.di.StreamKitInternalComponents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * File: CoreContractsAndInternalTest.kt
 * Description: Data classes, internal composition root, and factory routing for regression safety.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 */
class CoreContractsAndInternalTest {

    @Test
    fun drmConfig_roundTripsHeaders() {
        val d = DrmConfig("https://lic.example/widevine", mapOf("X-Token" to "abc"))
        assertEquals("https://lic.example/widevine", d.licenseUrl)
        assertEquals("abc", d.headers["X-Token"])
    }

    @Test
    fun streamingDiagnostics_defaultsAndCopy() {
        val d =
            StreamingDiagnostics(
                isCurrentMediaItemLive = true,
                playbackState = 3,
                bufferedPositionMs = 5000,
                currentPositionMs = 1000,
                liveOffsetMs = 2000,
            )
        assertEquals(0, d.textTrackCount)
        assertNull(d.selectedTextTrackSummary)
        assertEquals(C.TIME_UNSET, d.durationMs)
        assertEquals(false, d.isSeekable)
        val d2 = d.copy(durationMs = 99_000L, isSeekable = true)
        assertEquals(99_000L, d2.durationMs)
        assertTrue(d2.isSeekable)
    }

    @Test
    fun streamConfig_aiAndLiveOffsets() {
        val ai = AIConfig(AIProviderType.LOCAL, modelName = "rules")
        val c =
            StreamConfig(
                userAgent = "ua",
                httpHeaders = mapOf("a" to "b"),
                enableBandwidthPredictorHints = true,
                enableAlwaysOnAiOptimization = true,
                aiOptimizationIntervalMs = 500L,
                aiConfig = ai,
                liveTargetOffsetMs = 3000L,
            )
        assertEquals("ua", c.userAgent)
        assertEquals(3000L, c.liveTargetOffsetMs)
        assertEquals(ai, c.aiConfig)
    }

    @Test
    fun streamKitInternalComponents_wiresPredictor() {
        val c = StreamKitInternalComponents()
        assertNotNull(c.bandwidthPredictor)
        assertNotNull(c.networkHealthMonitor)
    }

    @Test
    fun aiProviderFactory_customEnterpriseUsesOpenAiSurface() {
        val p = AIProviderFactory.create(AIConfig(AIProviderType.CUSTOM_ENTERPRISE, modelName = "gateway"))
        assertTrue(p is OpenAIProvider)
    }
}
