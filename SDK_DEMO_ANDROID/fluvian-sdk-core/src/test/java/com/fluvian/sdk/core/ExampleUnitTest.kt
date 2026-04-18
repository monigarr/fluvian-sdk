package com.fluvian.sdk.core

import com.fluvian.sdk.core.aicore.AIConfig
import com.fluvian.sdk.core.aicore.AIManager
import com.fluvian.sdk.core.aicore.DEFAULT_OPTIMIZATION_JSON_SCHEMA
import com.fluvian.sdk.core.aicore.AIProviderFactory
import com.fluvian.sdk.core.aicore.AIProviderResolver
import com.fluvian.sdk.core.aicore.AIProviderType
import com.fluvian.sdk.core.aicore.LlmDecisionParser
import com.fluvian.sdk.core.aicore.OnDeviceGenAiProvider
import com.fluvian.sdk.core.aicore.OpenAIProvider
import com.fluvian.sdk.core.aicore.OptimizationDecision
import com.fluvian.sdk.core.aicore.QoSData
import com.fluvian.sdk.core.abr.BandwidthPredictor
import com.fluvian.sdk.core.aicore.RuleBasedAIProvider
import com.fluvian.sdk.core.network.NetworkHealthMonitor
import com.fluvian.sdk.core.network.SimulatedNetworkProfile
import com.fluvian.sdk.core.player.StreamManifestKind
import com.fluvian.sdk.core.player.streamManifestKindForUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * File: ExampleUnitTest.kt
 * Description: JVM unit tests for fluvian-sdk-core (metadata and pure Kotlin contracts).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.6
 *
 * Usage:
 *   Run via `./gradlew :fluvian-sdk-core:testDebugUnitTest` or Fluvian SDK CI before JaCoCo aggregation.
 *
 * Usage example:
 *   ./gradlew :fluvian-sdk-core:testDebugUnitTest
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun programInfo_matchesDocumentVersion() {
        assertEquals("1.3.6", EchelonProgramInfo.DOCUMENT_VERSION)
        assertEquals("Fluvian SDK", EchelonProgramInfo.PROGRAM_NAME)
        assertEquals("Fluvian SDK 1.3.6", EchelonProgramInfo.describe())
    }

    @Test
    fun streamManifestKind_selectsDashWhenPathEndsWithMpd() {
        assertEquals(StreamManifestKind.DASH, streamManifestKindForUrl("https://cdn.example.com/live/manifest.mpd"))
        assertEquals(StreamManifestKind.DASH, streamManifestKindForUrl("https://cdn.example.com/live/low.mpd?format=dash"))
        assertEquals(StreamManifestKind.HLS, streamManifestKindForUrl("https://cdn.example.com/master.m3u8"))
    }

    @Test
    fun bandwidthPredictor_appliesSimulatedThrottle() =
        runBlocking {
            val monitor = NetworkHealthMonitor()
            val predictor = BandwidthPredictor(monitor)
            monitor.setSimulatedProfile(SimulatedNetworkProfile.POOR_CELL)
            predictor.onPlayerBandwidthEstimate(8_000_000L)
            delay(50)
            assertTrue(predictor.hints.value.recommendedMaxVideoBitrate < 8_000_000)
            predictor.resetSession()
            monitor.resetForTests()
        }

    @Test
    fun ruleBasedAi_reducesWhenBufferLow() {
        val engine = RuleBasedAIProvider().apply {
            initialize(AIConfig(AIProviderType.LOCAL, modelName = "unit"))
        }
        val qos = QoSData(bitrate = 5_000_000, bufferHealth = 500L, droppedFrames = 0, networkSpeedKbps = 10_000)
        assertEquals(OptimizationDecision.REDUCE_QUALITY, engine.predict(qos))
    }

    @Test
    fun ruleBasedAi_stabilizesWhenDroppedFramesHigh() {
        val engine = RuleBasedAIProvider().apply {
            initialize(AIConfig(AIProviderType.LOCAL, modelName = "unit"))
        }
        val qos = QoSData(bitrate = 5_000_000, bufferHealth = 5_000L, droppedFrames = 20, networkSpeedKbps = 10_000)
        assertEquals(OptimizationDecision.STABILIZE, engine.predict(qos))
    }

    @Test
    fun aiProviderFactory_localReturnsRuleBasedInstance() {
        val cfg = AIConfig(AIProviderType.LOCAL, modelName = "x")
        val provider = AIProviderFactory.create(cfg)
        assertTrue(provider is RuleBasedAIProvider)
    }

    @Test
    fun aiManager_customResolverWinsOverFactory() {
        val engine = RuleBasedAIProvider()
        val mgr = AIManager()
        mgr.configure(
            AIConfig(AIProviderType.OPENAI, modelName = "cloud"),
            providerResolver = AIProviderResolver { _, _ -> engine },
        )
        val qos = QoSData(bitrate = 5_000_000, bufferHealth = 500L, droppedFrames = 0, networkSpeedKbps = 10_000)
        assertEquals(OptimizationDecision.REDUCE_QUALITY, mgr.predict(qos))
        mgr.shutdown()
    }

    @Test
    fun aiManager_nullResolverUsesFactory() {
        val mgr = AIManager()
        mgr.configure(
            AIConfig(AIProviderType.LOCAL, modelName = "rules"),
            providerResolver = AIProviderResolver { _, _ -> null },
        )
        val qos = QoSData(bitrate = 5_000_000, bufferHealth = 500L, droppedFrames = 0, networkSpeedKbps = 10_000)
        assertEquals(OptimizationDecision.REDUCE_QUALITY, mgr.predict(qos))
        mgr.shutdown()
    }

    @Test
    fun aiProviderFactory_onDeviceGenAi_withoutContextUsesRuleFallback() {
        val cfg = AIConfig(AIProviderType.ON_DEVICE_GENAI, modelName = "nano")
        val provider = AIProviderFactory.create(cfg, applicationContext = null)
        assertTrue(provider is OnDeviceGenAiProvider)
        provider.initialize(cfg)
        val qos = QoSData(bitrate = 5_000_000, bufferHealth = 500L, droppedFrames = 0, networkSpeedKbps = 10_000)
        assertEquals(OptimizationDecision.STABILIZE, provider.predict(qos))
        provider.shutdown()
    }

    @Test
    fun llmDecisionParser_parsesTokens() {
        assertEquals(OptimizationDecision.REDUCE_QUALITY, LlmDecisionParser.parseLlmDecisionText(" REDUCE_QUALITY "))
        assertEquals(OptimizationDecision.INCREASE_QUALITY, LlmDecisionParser.parseLlmDecisionText("ok: INCREASE_QUALITY"))
        assertEquals(OptimizationDecision.STABILIZE, LlmDecisionParser.parseLlmDecisionText("STABILIZE\n"))
    }

    @Test
    fun llmDecisionParser_parsesJsonDecisionField() {
        assertEquals(
            OptimizationDecision.REDUCE_QUALITY,
            LlmDecisionParser.parseOptimizationDecisionFromModelContent("""{"decision":"REDUCE_QUALITY"}"""),
        )
    }

    @Test
    fun llmDecisionParser_parsesNestedJson() {
        assertEquals(
            OptimizationDecision.INCREASE_QUALITY,
            LlmDecisionParser.parseOptimizationDecisionFromModelContent("""{"meta":{},"payload":{"result":"INCREASE_QUALITY"}}"""),
        )
    }

    @Test
    fun llmDecisionParser_jsonThenPlainFallback() {
        assertEquals(
            OptimizationDecision.STABILIZE,
            LlmDecisionParser.parseOptimizationDecisionFromModelContent("STABILIZE"),
        )
    }

    @Test
    fun defaultOptimizationJsonSchema_isValidJson() {
        JSONObject(DEFAULT_OPTIMIZATION_JSON_SCHEMA)
    }

    @Test
    fun openAiProvider_stubAlwaysStabilizes() {
        val p = OpenAIProvider()
        p.initialize(AIConfig(AIProviderType.OPENAI, modelName = "gpt-4o-mini", apiKey = null))
        val qos = QoSData(bitrate = 5_000_000, bufferHealth = 500L, droppedFrames = 0, networkSpeedKbps = 10_000)
        assertEquals(OptimizationDecision.STABILIZE, p.predict(qos))
        p.shutdown()
    }
}
