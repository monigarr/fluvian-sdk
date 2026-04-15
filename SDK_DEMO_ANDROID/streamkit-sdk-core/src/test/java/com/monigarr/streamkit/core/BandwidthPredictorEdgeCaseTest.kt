package com.monigarr.streamkit.core

import com.monigarr.streamkit.core.abr.BandwidthPredictor
import com.monigarr.streamkit.core.network.NetworkHealthMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * File: BandwidthPredictorEdgeCaseTest.kt
 * Description: ABR smoothing edge cases (invalid estimates, session reset).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 */
class BandwidthPredictorEdgeCaseTest {

    @Test
    fun onPlayerBandwidthEstimate_ignoresNonPositive() =
        runBlocking {
            val monitor = NetworkHealthMonitor()
            val predictor = BandwidthPredictor(monitor)
            predictor.onPlayerBandwidthEstimate(0L)
            predictor.onPlayerBandwidthEstimate(-100L)
            delay(30)
            assertEquals(Int.MAX_VALUE, predictor.hints.value.recommendedMaxVideoBitrate)
            predictor.resetSession()
            monitor.resetForTests()
        }

    @Test
    fun resetSession_restoresBootstrapHints() =
        runBlocking {
            val monitor = NetworkHealthMonitor()
            val predictor = BandwidthPredictor(monitor)
            monitor.setSimulatedProfile(com.monigarr.streamkit.core.network.SimulatedNetworkProfile.POOR_CELL)
            predictor.onPlayerBandwidthEstimate(8_000_000L)
            delay(80)
            assertTrue(predictor.hints.value.recommendedMaxVideoBitrate < Int.MAX_VALUE)
            predictor.resetSession()
            assertEquals("session-reset", predictor.hints.value.reason)
            assertEquals(Int.MAX_VALUE, predictor.hints.value.recommendedMaxVideoBitrate)
            monitor.resetForTests()
        }
}
