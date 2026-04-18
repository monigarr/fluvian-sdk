/**
 * File: OnDeviceGenAiWarmup.kt
 * Description: Suspend entry for on-device model readiness checks — Open Core always reports [OnDeviceGenAiReadiness.UNAVAILABLE].
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
 *   Call from a coroutine before attempting on-device inference in integrator shells; expect UNAVAILABLE unless a PRO provider is wired.
 *
 * Usage example:
 *   OnDeviceGenAiWarmup.awaitFeatureReady(timeoutMs = 5_000L)
 */
package com.fluvian.sdk.core.aicore

import kotlinx.coroutines.delay

/**
 * Warm-up façade. Public Open Core cannot bundle proprietary downloaders; commercial modules replace this object via
 * classpath overrides or alternate artifacts where contracts allow.
 */
object OnDeviceGenAiWarmup {
    /**
     * @param timeoutMs reserved for parity with enterprise implementations; honored as a cooperative upper bound only.
     */
    suspend fun awaitFeatureReady(timeoutMs: Long = 60_000L): OnDeviceGenAiReadiness {
        delay(1L.coerceAtMost(timeoutMs))
        return OnDeviceGenAiReadiness.UNAVAILABLE
    }
}
