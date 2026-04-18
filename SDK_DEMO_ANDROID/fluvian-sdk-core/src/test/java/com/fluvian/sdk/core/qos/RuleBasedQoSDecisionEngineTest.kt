package com.fluvian.sdk.core.qos

/**
 * File: RuleBasedQoSDecisionEngineTest.kt
 * Description: JVM unit tests for [RuleBasedQoSDecisionEngine] recommendation paths (buffer, rebuffer, throughput).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Run via `./gradlew :fluvian-sdk-core:testDebugUnitTest` or Fluvian SDK CI.
 *
 * Usage example:
 *   `./gradlew :fluvian-sdk-core:testDebugUnitTest --tests com.fluvian.sdk.core.qos.RuleBasedQoSDecisionEngineTest`
 */
import androidx.media3.common.C
import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleBasedQoSDecisionEngineTest {

    private fun metrics(
        playbackState: Int = Player.STATE_READY,
        isRebuffering: Boolean = false,
        bufferedAheadMs: Long = 3_000L,
        dropped: Int = 0,
        throughputBps: Long = 10_000_000L,
        recommendedMaxBr: Int = 4_000_000,
    ): QoSMetrics =
        QoSMetrics(
            playbackState = playbackState,
            playWhenReady = true,
            isRebuffering = isRebuffering,
            bufferedAheadMs = bufferedAheadMs,
            measuredBandwidthBps = throughputBps,
            estimatedThroughputBps = throughputBps,
            recommendedMaxVideoBitrate = recommendedMaxBr,
            droppedVideoFramesWindow = dropped,
            currentPositionMs = 0L,
            liveOffsetMs = C.TIME_UNSET,
            wallClockElapsedMs = 0L,
        )

    @Test
    fun rebuffering_reduces() {
        val d = RuleBasedQoSDecisionEngine.recommend(metrics(isRebuffering = true, bufferedAheadMs = 5_000L))
        assertEquals(QoSDecision.REDUCE_QUALITY, d)
    }

    @Test
    fun low_buffer_reduces() {
        val d = RuleBasedQoSDecisionEngine.recommend(metrics(bufferedAheadMs = 500L))
        assertEquals(QoSDecision.REDUCE_QUALITY, d)
    }

    @Test
    fun healthy_buffer_and_headroom_increases() {
        val d =
            RuleBasedQoSDecisionEngine.recommend(
                metrics(
                    bufferedAheadMs = 6_000L,
                    dropped = 0,
                    throughputBps = 20_000_000L,
                    recommendedMaxBr = 5_000_000,
                ),
            )
        assertEquals(QoSDecision.INCREASE_QUALITY, d)
    }

    @Test
    fun fromAiFacade_bridges_to_rules() {
        val m =
            QoSMetrics.fromAiFacade(
                com.fluvian.sdk.core.aicore.QoSData(
                    bitrate = 5_000_000,
                    bufferHealth = 6_000L,
                    droppedFrames = 0,
                    networkSpeedKbps = 25_000,
                ),
            )
        assertEquals(QoSDecision.INCREASE_QUALITY, RuleBasedQoSDecisionEngine.recommend(m))
    }
}
