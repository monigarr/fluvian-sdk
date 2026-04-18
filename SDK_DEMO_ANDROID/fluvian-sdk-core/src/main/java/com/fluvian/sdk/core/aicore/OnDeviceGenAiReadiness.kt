// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: OnDeviceGenAiReadiness.kt
 * Description: Open Core readiness enum for on-device GenAI hooks (no vendor SDK types on the public surface).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
package com.fluvian.sdk.core.aicore

/**
 * Open Core readiness for on-device GenAI hooks. Enterprise builds map this to vendor-specific status codes.
 */
enum class OnDeviceGenAiReadiness {
    UNAVAILABLE,
    AVAILABLE,
}
