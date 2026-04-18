// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

/**
 * File: QoSData.kt
 * Description: M.I.L.E. **Measure** snapshot for the public AI façade — maps to ExoPlayer analytics + buffer telemetry.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Built by [com.fluvian.sdk.core.player.StreamingClientImpl] from [androidx.media3.exoplayer.analytics.AnalyticsListener]
 * signals and player positions.
 *
 * Usage example:
 *   QoSData(bitrate = 5_000_000, bufferHealth = 2_000L, droppedFrames = 0, networkSpeedKbps = 8_000)
 */
data class QoSData(
    val bitrate: Int,
    val bufferHealth: Long,
    val droppedFrames: Int,
    val networkSpeedKbps: Int,
)
