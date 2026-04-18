/**
 * File: OnDeviceGenAiDiagnostics.kt
 * Description: Optional diagnostics hooks for on-device GenAI pipelines (Open Core exposes stable strings only).
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
 *   Host telemetry may log [describe] during bring-up; never log prompts or user content from this path in production.
 *
 * Usage example:
 *   OnDeviceGenAiDiagnostics.describe()
 */
package com.fluvian.sdk.core.aicore

/** Lightweight status surface for dashboards; enterprise builds attach exporters here. */
object OnDeviceGenAiDiagnostics {
    fun describe(): String = "open-core:on-device-genai-diagnostics-disabled"
}
