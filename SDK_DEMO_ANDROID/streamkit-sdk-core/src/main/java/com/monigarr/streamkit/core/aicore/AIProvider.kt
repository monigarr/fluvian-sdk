package com.monigarr.streamkit.core.aicore

/**
 * File: AIProvider.kt
 * Description: Pluggable AI surface for bitrate / stability decisions without coupling playback to a vendor SDK.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.1.0
 *
 * Usage:
 *   Implement for cloud or on-device models; default rule path is [RuleBasedAIProvider].
 *
 * Usage example:
 *   val provider: AIProvider = RuleBasedAIProvider()
 *   provider.initialize(AIConfig(AIProviderType.LOCAL, modelName = "rules-v1"))
 */
interface AIProvider {
    fun initialize(config: AIConfig)
    fun predict(input: QoSData): OptimizationDecision
    fun shutdown()
}
