// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.qos

import com.fluvian.sdk.core.interpret.InterpretationInputs
import com.fluvian.sdk.core.interpret.RuleBasedInterpretation
import com.fluvian.sdk.core.interpret.RuleBasedOutcome

/**
 * File: RuleBasedQoSDecisionEngine.kt
 * Description: **Interpret** — deterministic mapping from [QoSMetrics] to [QoSDecision]. Auditable QoS arbitration
 * extends in Fluvian **PRO** (private artifacts). Delegates predicates to [RuleBasedInterpretation] so AI rule path stays aligned.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
object RuleBasedQoSDecisionEngine {
    fun recommend(metrics: QoSMetrics): QoSDecision =
        RuleBasedInterpretation.recommend(metrics.toInterpretationInputs()).toQoSDecision()
}

private fun QoSMetrics.toInterpretationInputs(): InterpretationInputs =
    InterpretationInputs(
        playbackState = playbackState,
        playWhenReady = playWhenReady,
        isRebuffering = isRebuffering,
        bufferedAheadMs = bufferedAheadMs,
        estimatedThroughputBps = estimatedThroughputBps,
        recommendedMaxVideoBitrate = recommendedMaxVideoBitrate,
        droppedVideoFramesWindow = droppedVideoFramesWindow,
    )

private fun RuleBasedOutcome.toQoSDecision(): QoSDecision =
    when (this) {
        RuleBasedOutcome.INCREASE_QUALITY -> QoSDecision.INCREASE_QUALITY
        RuleBasedOutcome.REDUCE_QUALITY -> QoSDecision.REDUCE_QUALITY
        RuleBasedOutcome.STABILIZE -> QoSDecision.STABILIZE
    }
