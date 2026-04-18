// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.qos

import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.Player
import com.fluvian.sdk.core.StreamingDiagnostics
import com.fluvian.sdk.core.abr.AbrHint
import com.fluvian.sdk.core.aicore.QoSData

/**
 * File: QoSMetrics.kt
 * Description: **Measure** — single canonical snapshot of playback QoS after correlating
 * [androidx.media3.common.Player.Listener], [androidx.media3.exoplayer.analytics.AnalyticsListener],
 * [androidx.media3.exoplayer.upstream.BandwidthMeter] estimates (via [AbrHint]), and buffer geometry.
 *
 * **M.I.L.E.:** built only in the signal layer; [QoSController] forwards this to **Interpret** / **Learn** / **Execute**.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
data class QoSMetrics(
    val playbackState: Int,
    val playWhenReady: Boolean,
    /** True while [Player.STATE_BUFFERING] is observed between READY play attempts (rebuffer). */
    val isRebuffering: Boolean,
    /** Buffered media ahead of the playhead (ms). */
    val bufferedAheadMs: Long,
    /** Last raw bandwidth estimate from [AnalyticsListener.onBandwidthEstimate] (bps). */
    val measuredBandwidthBps: Long,
    val estimatedThroughputBps: Long,
    val recommendedMaxVideoBitrate: Int,
    val droppedVideoFramesWindow: Int,
    val currentPositionMs: Long,
    val liveOffsetMs: Long,
    val wallClockElapsedMs: Long,
) {
    companion object {
        /**
         * Builds metrics when only the AI façade snapshot ([QoSData]) is available (no live [Player]).
         *
         * @param wallClockElapsedMs monotonic clock (ms); default 0 is safe for JVM unit tests. Live paths stamp via [fromPlayerAndDiagnostics].
         */
        fun fromAiFacade(
            input: QoSData,
            measuredBandwidthBps: Long = input.networkSpeedKbps.toLong() * 1000L,
            wallClockElapsedMs: Long = 0L,
        ): QoSMetrics =
            QoSMetrics(
                playbackState = Player.STATE_READY,
                playWhenReady = true,
                isRebuffering = false,
                bufferedAheadMs = input.bufferHealth,
                measuredBandwidthBps = measuredBandwidthBps.coerceAtLeast(1L),
                estimatedThroughputBps = input.networkSpeedKbps.toLong() * 1000L,
                recommendedMaxVideoBitrate = input.bitrate.coerceAtLeast(1),
                droppedVideoFramesWindow = input.droppedFrames,
                currentPositionMs = 0L,
                liveOffsetMs = C.TIME_UNSET,
                wallClockElapsedMs = wallClockElapsedMs,
            )

        fun fromPlayerAndDiagnostics(
            player: Player,
            diagnostics: StreamingDiagnostics,
            hint: AbrHint,
            isRebuffering: Boolean,
            measuredBandwidthBps: Long,
            droppedVideoFramesWindow: Int,
            wallClockElapsedMs: Long = SystemClock.elapsedRealtime(),
        ): QoSMetrics {
            val ahead = (diagnostics.bufferedPositionMs - diagnostics.currentPositionMs).coerceAtLeast(0L)
            return QoSMetrics(
                playbackState = diagnostics.playbackState,
                playWhenReady = player.playWhenReady,
                isRebuffering = isRebuffering,
                bufferedAheadMs = ahead,
                measuredBandwidthBps = measuredBandwidthBps.coerceAtLeast(1L),
                estimatedThroughputBps = hint.estimatedThroughputBps.coerceAtLeast(1L),
                recommendedMaxVideoBitrate = hint.recommendedMaxVideoBitrate.coerceAtLeast(1),
                droppedVideoFramesWindow = droppedVideoFramesWindow,
                currentPositionMs = diagnostics.currentPositionMs,
                liveOffsetMs = diagnostics.liveOffsetMs,
                wallClockElapsedMs = wallClockElapsedMs,
            )
        }
    }

    /** Bridge to the legacy AI façade for providers that still consume [QoSData]. */
    fun toQoSData(): QoSData =
        QoSData(
            bitrate = recommendedMaxVideoBitrate,
            bufferHealth = bufferedAheadMs,
            droppedFrames = droppedVideoFramesWindow,
            networkSpeedKbps = (estimatedThroughputBps / 1000L).toInt().coerceAtLeast(1),
        )
}
