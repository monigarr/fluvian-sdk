/**
 * File: OnDeviceGenAiProvider.kt
 * Description: Open Core on-device GenAI [AIProvider] — deterministic stub suitable for CI and fresh clones.
 *
 * NOTE:
 * This file contains a simplified implementation for evaluation purposes.
 * Advanced optimization logic is part of the private enterprise layer.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Select [AIProviderType.ON_DEVICE_GENAI] in [AIConfig]; licensed builds swap in a private provider via [AIProviderResolver].
 *
 * Usage example:
 *   OnDeviceGenAiProvider(context).initialize(config); provider.predict(qosData)
 */
package com.fluvian.sdk.core.aicore

import android.content.Context

/**
 * Placeholder on-device stack: no weights, no remote calls, no ML Kit dependency inside `:fluvian-sdk-core`.
 * **PRO / ENTERPRISE** artifacts supply real Prompt API / Nano routing under separate distribution.
 */
class OnDeviceGenAiProvider(
    @Suppress("UNUSED_PARAMETER") context: Context?,
) : AIProvider {
    override fun initialize(config: AIConfig) {
    }

    override fun predict(input: QoSData): OptimizationDecision {
        // Open Core returns a single conservative intent so downstream optimizers never fight the rules engine blindly.
        return OptimizationDecision.STABILIZE
    }

    override fun shutdown() {
    }
}
