/**
 * File: PlayerOptimizer.kt
 * Description: **Execute** stage — applies discrete [com.fluvian.sdk.core.aicore.OptimizationDecision] intents to [androidx.media3.common.Player.trackSelectionParameters].
 *
 * NOTE:
 * This file contains a simplified implementation for evaluation purposes.
 * Advanced optimization logic is part of the private enterprise layer.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Always invoke from the player’s looper (see [com.fluvian.sdk.core.performance.StreamOrchestrator]).
 *
 * Usage example:
 *   PlayerOptimizer(player).optimize(qosData, OptimizationDecision.REDUCE_QUALITY)
 */
package com.fluvian.sdk.core.player

import androidx.media3.common.Player
import com.fluvian.sdk.core.aicore.OptimizationDecision
import com.fluvian.sdk.core.aicore.QoSData
import kotlin.math.max
import kotlin.math.min

/**
 * Maps optimization decisions to conservative track-selection caps. Enterprise builds replace heuristics with
 * learned policies while preserving this call shape.
 */
class PlayerOptimizer(
    private val player: Player,
) {
    /**
     * Applies [decision] using the latest [qos] snapshot. Open Core uses deterministic, non-destructive caps only.
     */
    fun optimize(
        qos: QoSData,
        decision: OptimizationDecision,
    ) {
        val current = player.trackSelectionParameters
        val priorMax = current.maxVideoBitrate
        val base = qos.bitrate.coerceAtLeast(1)
        val targetMax =
            when (decision) {
                OptimizationDecision.REDUCE_QUALITY ->
                    max((base * 0.65f).toInt(), 400_000)
                OptimizationDecision.INCREASE_QUALITY -> {
                    val uplifted =
                        if (priorMax == Int.MAX_VALUE) {
                            min(Int.MAX_VALUE - 1, (base * 1.25f).toInt().coerceAtLeast(1))
                        } else {
                            min(Int.MAX_VALUE - 1, (priorMax * 1.2f).toInt().coerceAtLeast(base))
                        }
                    uplifted
                }
                OptimizationDecision.STABILIZE -> {
                    val p = priorMax
                    if (p == Int.MAX_VALUE) base else p
                }
            }
        player.trackSelectionParameters =
            current
                .buildUpon()
                .setMaxVideoBitrate(targetMax.coerceAtLeast(1))
                .build()
    }
}
