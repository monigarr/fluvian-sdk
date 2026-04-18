// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

import android.content.Context

/**
 * File: AIProviderFactory.kt
 * Description: Central factory mapping [AIConfig.providerType] to concrete [AIProvider] implementations.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.6
 *
 * Usage:
 *   Called from [AIManager.configure] when switching models or vendors at runtime.
 *
 * Usage example:
 *   val p = AIProviderFactory.create(AIConfig(AIProviderType.LOCAL, modelName = "rules-v1"))
 */
object AIProviderFactory {

    fun create(config: AIConfig, applicationContext: Context? = null): AIProvider =
        when (config.providerType) {
            AIProviderType.LOCAL -> RuleBasedAIProvider()
            AIProviderType.OPENAI, AIProviderType.CUSTOM_ENTERPRISE -> OpenAIProvider()
            AIProviderType.ON_DEVICE_GENAI -> OnDeviceGenAiProvider(applicationContext?.applicationContext)
        }
}
