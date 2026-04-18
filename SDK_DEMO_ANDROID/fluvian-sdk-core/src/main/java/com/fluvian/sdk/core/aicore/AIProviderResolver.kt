// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

import android.content.Context

/**
 * File: AIProviderResolver.kt
 * Description: Enterprise hook to supply a bespoke [AIProvider] (private gateway, on-prem LLM, air-gapped stack)
 * while keeping [AIProviderFactory] as the default Open Core router.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Set on [com.fluvian.sdk.core.StreamConfig.aiProviderResolver]. Return null to fall back to the built-in factory map.
 *
 * Usage example:
 *   AIProviderResolver { cfg, ctx -> if (cfg.providerType == AIProviderType.CUSTOM_ENTERPRISE) MyAi(cfg, ctx) else null }
 */
fun interface AIProviderResolver {

    /**
     * @return A fully constructible provider, or null to delegate to [AIProviderFactory].
     */
    fun resolve(
        config: AIConfig,
        applicationContext: Context?,
    ): AIProvider?
}
