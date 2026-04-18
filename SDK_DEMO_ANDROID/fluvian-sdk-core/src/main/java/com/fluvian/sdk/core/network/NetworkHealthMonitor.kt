// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: NetworkHealthMonitor.kt
 * Description: Simulated network profiles for demos and labs; adjusts measured throughput without touching the UI thread.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.6
 *
 * Usage:
 *   Host apps toggle [SimulatedNetworkProfile] to stress ABR/AI paths; production builds keep [SimulatedNetworkProfile.NONE].
 *
 * Usage example:
 *   monitor.setSimulatedProfile(SimulatedNetworkProfile.POOR_CELL)
 */
package com.fluvian.sdk.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SimulatedNetworkProfile {
    NONE,
    POOR_CELL,
    CONGESTED_WIFI,
}

class NetworkHealthMonitor {

    private val _profile = MutableStateFlow(SimulatedNetworkProfile.NONE)
    val profile: StateFlow<SimulatedNetworkProfile> = _profile.asStateFlow()

    fun setSimulatedProfile(profile: SimulatedNetworkProfile) {
        _profile.value = profile
    }

    /**
     * Applies deterministic caps so [BandwidthPredictor] can react without parsing manifests on the UI thread.
     */
    fun adjustedThroughput(measuredBps: Long): Long {
        if (measuredBps <= 0L) return measuredBps
        val cap = when (_profile.value) {
            SimulatedNetworkProfile.NONE -> Long.MAX_VALUE
            SimulatedNetworkProfile.POOR_CELL -> 900_000L
            SimulatedNetworkProfile.CONGESTED_WIFI -> 450_000L
        }
        return minOf(measuredBps, cap)
    }

    fun label(): String = _profile.value.name

    internal fun resetForTests() {
        _profile.update { SimulatedNetworkProfile.NONE }
    }
}
