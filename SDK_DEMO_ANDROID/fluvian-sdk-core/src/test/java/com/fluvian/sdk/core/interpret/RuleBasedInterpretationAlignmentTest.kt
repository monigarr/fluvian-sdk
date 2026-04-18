// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: RuleBasedInterpretationAlignmentTest.kt
 * Description: Ensures façade rule path and QoS metrics path stay aligned after refactors to [RuleBasedInterpretation].
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
package com.fluvian.sdk.core.interpret

import com.fluvian.sdk.core.aicore.QoSData
import com.fluvian.sdk.core.aicore.RuleBasedAIProvider
import com.fluvian.sdk.core.qos.QoSMetrics
import com.fluvian.sdk.core.qos.RuleBasedQoSDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleBasedInterpretationAlignmentTest {

    @Test
    fun ruleBasedAI_matches_qosEngine_for_facade_inputs() {
        val cases =
            listOf(
                QoSData(bitrate = 5_000_000, bufferHealth = 6_000L, droppedFrames = 0, networkSpeedKbps = 12_000),
                QoSData(bitrate = 3_000_000, bufferHealth = 500L, droppedFrames = 0, networkSpeedKbps = 4_000),
                QoSData(bitrate = 4_000_000, bufferHealth = 8_000L, droppedFrames = 20, networkSpeedKbps = 20_000),
            )
        val provider = RuleBasedAIProvider()
        for (input in cases) {
            val viaMetrics =
                RuleBasedQoSDecisionEngine.recommend(QoSMetrics.fromAiFacade(input))
                    .toOptimizationDecision()
            assertEquals(viaMetrics, provider.predict(input))
        }
    }
}
