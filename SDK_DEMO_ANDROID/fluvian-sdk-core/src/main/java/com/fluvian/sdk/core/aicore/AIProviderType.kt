// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

/**
 * File: AIProviderType.kt
 * Description: Enumerates AI backends selectable through [AIConfig]; Open Core ships stubs / rules only.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Pass into [AIConfig.providerType]; resolved by [AIProviderFactory].
 *
 * Usage example:
 *   AIProviderType.LOCAL
 */
enum class AIProviderType {
    LOCAL,
    OPENAI,
    CUSTOM_ENTERPRISE,
    ON_DEVICE_GENAI,
}
