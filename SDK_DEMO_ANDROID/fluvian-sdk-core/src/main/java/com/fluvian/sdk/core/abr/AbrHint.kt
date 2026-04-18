// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.abr

/**
 * File: AbrHint.kt
 * Description: Public ABR hint snapshot emitted by the Open Core [BandwidthPredictor] (throughput + recommended cap).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Collect from [BandwidthPredictor.hints]; apply on the playback looper via [androidx.media3.common.Player.trackSelectionParameters].
 *
 * Usage example:
 *   val hint = client.bandwidthPredictor()?.hints?.value
 */
data class AbrHint(
    val recommendedMaxVideoBitrate: Int,
    val estimatedThroughputBps: Long,
    val reason: String,
)
