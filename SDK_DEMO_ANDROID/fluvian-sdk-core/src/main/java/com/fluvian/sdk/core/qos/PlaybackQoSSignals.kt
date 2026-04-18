// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.qos

/**
 * File: PlaybackQoSSignals.kt
 * Description: Canonical QoS signal names aligned with Media3 hooks ([androidx.media3.common.Player.Listener],
 * [androidx.media3.exoplayer.analytics.AnalyticsListener], bandwidth meter, buffer positions).
 *
 * **M.I.L.E. mapping:** these fields are the **Measure** layer; [com.fluvian.sdk.core.aicore.AIProvider] is **Interpret**;
 * [com.fluvian.sdk.core.player.PlayerOptimizer] is **Execute** (Open Core uses conservative track caps).
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Construct inside the SDK when correlating analytics callbacks before invoking AI facades.
 *
 * Usage example:
 *   PlaybackQoSSignals(measuredBandwidthBps = 6_000_000L, rebuffering = false, droppedVideoFramesDelta = 2, playbackLatencyMs = 1800L)
 *
 * @see com.fluvian.sdk.core.qos.QoSController.buildPlaybackQoSSignals Canonical construction from live [QoSMetrics].
 */
data class PlaybackQoSSignals(
    val measuredBandwidthBps: Long,
    val rebuffering: Boolean,
    val droppedVideoFramesDelta: Int,
    val playbackLatencyMs: Long,
)
