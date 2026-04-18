// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: InterpretationInputs.kt
 * Description: Media-free snapshot for deterministic **Interpret** (M.I.L.E.) — shared by QoS and AI rule paths.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Built from [com.fluvian.sdk.core.qos.QoSMetrics] or façade fields; consumed only by [RuleBasedInterpretation].
 *
 * Usage example:
 *   val inputs = InterpretationInputs.fromFacade(5_000_000, 2_000L, 0, 8_000)
 */
package com.fluvian.sdk.core.interpret

import androidx.media3.common.Player

/**
 * Normalized inputs for [RuleBasedInterpretation]. Lives in `interpret` so neither `qos` nor `aicore`
 * owns duplicated rule predicates.
 */
internal data class InterpretationInputs(
    val playbackState: Int,
    val playWhenReady: Boolean,
    val isRebuffering: Boolean,
    val bufferedAheadMs: Long,
    val estimatedThroughputBps: Long,
    val recommendedMaxVideoBitrate: Int,
    val droppedVideoFramesWindow: Int,
) {
    companion object {
        /**
         * Same geometry as [com.fluvian.sdk.core.qos.QoSMetrics.fromAiFacade] when only the public AI façade is available.
         */
        fun fromFacade(
            bitrate: Int,
            bufferHealth: Long,
            droppedFrames: Int,
            networkSpeedKbps: Int,
        ): InterpretationInputs =
            InterpretationInputs(
                playbackState = Player.STATE_READY,
                playWhenReady = true,
                isRebuffering = false,
                bufferedAheadMs = bufferHealth,
                estimatedThroughputBps = networkSpeedKbps.toLong() * 1000L,
                recommendedMaxVideoBitrate = bitrate.coerceAtLeast(1),
                droppedVideoFramesWindow = droppedFrames,
            )
    }
}
