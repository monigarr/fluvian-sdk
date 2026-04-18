/**
 * File: AIOrchestrator.kt
 * Description: Thin bridge from [AIProvider] outputs to [PlayerOptimizer] for manual **Interpret → Execute** demos.
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
 *   Construct with a configured provider and a [PlayerOptimizer] bound to the active [androidx.media3.common.Player].
 *
 * Usage example:
 *   AIOrchestrator(provider, PlayerOptimizer(player)).process(qosSnapshot)
 */
package com.fluvian.sdk.core.aicore

import com.fluvian.sdk.core.player.PlayerOptimizer

/**
 * Coordinates AI inference with player mutation. Always-on optimization in production typically flows through
 * [com.fluvian.sdk.core.qos.QoSController] instead of calling this directly.
 */
class AIOrchestrator(
    private val aiProvider: AIProvider,
    private val playerOptimizer: PlayerOptimizer,
) {
    /** Runs [AIProvider.predict] then [PlayerOptimizer.optimize] on the caller’s thread (must be the playback looper). */
    fun process(qos: QoSData) {
        val decision = aiProvider.predict(qos)
        playerOptimizer.optimize(qos, decision)
    }
}
