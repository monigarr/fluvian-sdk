// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

/**
 * File: AIConfig.kt
 * Description: Serializable AI wiring for demos and integrators — **never** embed production secrets in source control.
 *
 * **OPEN CORE:** schema + routing only. Cloud keys, enterprise gateways, and tuned prompts belong to host secure storage.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Attach to [com.fluvian.sdk.core.StreamConfig.aiConfig] when enabling always-on optimization.
 *
 * Usage example:
 *   AIConfig(AIProviderType.LOCAL, modelName = "rules-v1")
 */
data class AIConfig(
    val providerType: AIProviderType,
    val modelName: String = "rules-v1",
    val endpoint: String? = null,
    val apiKey: String? = null,
    val systemPrompt: String? = null,
    val useAzureStyleApiKeyHeader: Boolean = false,
    val structuredOutputJsonSchema: String? = null,
    val structuredOutputSchemaName: String = "FluvianOptimizationDecision",
    val localModelPath: String? = null,
    val structuredOutputStrict: Boolean = true,
)
