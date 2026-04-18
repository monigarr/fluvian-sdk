// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

/**
 * File: OptimizationDecision.kt
 * Description: Discrete execution intents produced by the decision layer (rules in Open Core; ML in PRO).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Returned from [AIProvider.predict] and applied by [com.fluvian.sdk.core.player.PlayerOptimizer] on the playback looper.
 *
 * Usage example:
 *   OptimizationDecision.STABILIZE
 */
enum class OptimizationDecision {
    INCREASE_QUALITY,
    REDUCE_QUALITY,
    STABILIZE,
}
