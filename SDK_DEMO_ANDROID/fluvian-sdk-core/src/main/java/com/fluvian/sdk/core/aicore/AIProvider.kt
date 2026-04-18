// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

/**
 * File: AIProvider.kt
 * Description: Pluggable AI surface for bitrate / stability decisions without coupling playback to a vendor SDK.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.6
 *
 * Usage:
 *   Implement for cloud or on-device models; default rule path is [RuleBasedAIProvider].
 *
 * Usage example:
 *   val provider: AIProvider = RuleBasedAIProvider()
 *   provider.initialize(AIConfig(AIProviderType.LOCAL, modelName = "rules-v1"))
 *
 * **Enterprise:** wire custom implementations through [AIProviderResolver] on [com.fluvian.sdk.core.StreamConfig]
 * so hosts can swap models without forking [AIProviderFactory].
 */
interface AIProvider {
    fun initialize(config: AIConfig)
    fun predict(input: QoSData): OptimizationDecision
    fun shutdown()
}
