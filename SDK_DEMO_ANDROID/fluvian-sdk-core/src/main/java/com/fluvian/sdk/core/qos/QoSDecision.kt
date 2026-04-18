// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.qos

import com.fluvian.sdk.core.aicore.OptimizationDecision

/**
 * File: QoSDecision.kt
 * Description: **Interpret** output — streaming-domain decision before **Execute** mutates
 * [androidx.media3.common.TrackSelectionParameters] / policy. Kept in the `qos` package so signal and AI layers stay separate.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
enum class QoSDecision {
    INCREASE_QUALITY,
    REDUCE_QUALITY,
    STABILIZE,
    ;

    fun toOptimizationDecision(): OptimizationDecision =
        when (this) {
            INCREASE_QUALITY -> OptimizationDecision.INCREASE_QUALITY
            REDUCE_QUALITY -> OptimizationDecision.REDUCE_QUALITY
            STABILIZE -> OptimizationDecision.STABILIZE
        }

    companion object {
        fun fromOptimizationDecision(decision: OptimizationDecision): QoSDecision =
            when (decision) {
                OptimizationDecision.INCREASE_QUALITY -> INCREASE_QUALITY
                OptimizationDecision.REDUCE_QUALITY -> REDUCE_QUALITY
                OptimizationDecision.STABILIZE -> STABILIZE
            }
    }
}
