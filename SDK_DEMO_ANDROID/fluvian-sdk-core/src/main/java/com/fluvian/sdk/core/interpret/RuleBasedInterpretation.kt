// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: RuleBasedInterpretation.kt
 * Description: Single implementation of Open Core rule-based **Interpret** — avoids divergent QoS vs AI paths.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   [com.fluvian.sdk.core.qos.RuleBasedQoSDecisionEngine] and [com.fluvian.sdk.core.aicore.RuleBasedAIProvider] map the outcome to their enums.
 *
 * Usage example:
 *   val outcome = RuleBasedInterpretation.recommend(InterpretationInputs.fromFacade(4_000_000, 800L, 0, 5_000))
 */
package com.fluvian.sdk.core.interpret

import androidx.media3.common.Player

internal enum class RuleBasedOutcome {
    INCREASE_QUALITY,
    REDUCE_QUALITY,
    STABILIZE,
}

internal object RuleBasedInterpretation {
    fun recommend(inputs: InterpretationInputs): RuleBasedOutcome {
        if (inputs.isRebuffering ||
            (inputs.playbackState == Player.STATE_BUFFERING && inputs.bufferedAheadMs < 900L)
        ) {
            return RuleBasedOutcome.REDUCE_QUALITY
        }
        if (inputs.bufferedAheadMs < 1_200L) {
            return RuleBasedOutcome.REDUCE_QUALITY
        }
        if (inputs.droppedVideoFramesWindow >= 12) {
            return RuleBasedOutcome.STABILIZE
        }
        val headroom =
            inputs.estimatedThroughputBps >
                (inputs.recommendedMaxVideoBitrate.coerceAtLeast(1) * 1.25)
        if (!inputs.isRebuffering &&
            inputs.bufferedAheadMs > 5_000L &&
            inputs.droppedVideoFramesWindow == 0 &&
            headroom
        ) {
            return RuleBasedOutcome.INCREASE_QUALITY
        }
        return RuleBasedOutcome.STABILIZE
    }
}
