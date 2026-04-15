package com.monigarr.streamkit.core

import com.monigarr.streamkit.core.network.NetworkHealthMonitor
import com.monigarr.streamkit.core.network.SimulatedNetworkProfile
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * File: NetworkHealthMonitorThroughputTest.kt
 * Description: Deterministic throughput caps for simulated network profiles (ABR lab paths).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 */
class NetworkHealthMonitorThroughputTest {

    @Test
    fun adjustedThroughput_nonPositive_passesThrough() {
        val m = NetworkHealthMonitor()
        assertEquals(-1L, m.adjustedThroughput(-1L))
        assertEquals(0L, m.adjustedThroughput(0L))
        m.resetForTests()
    }

    @Test
    fun adjustedThroughput_none_doesNotCap() {
        val m = NetworkHealthMonitor()
        m.setSimulatedProfile(SimulatedNetworkProfile.NONE)
        assertEquals(12_000_000L, m.adjustedThroughput(12_000_000L))
        m.resetForTests()
    }

    @Test
    fun adjustedThroughput_poorCell_capsAt900k() {
        val m = NetworkHealthMonitor()
        m.setSimulatedProfile(SimulatedNetworkProfile.POOR_CELL)
        assertEquals(900_000L, m.adjustedThroughput(50_000_000L))
        m.resetForTests()
    }

    @Test
    fun adjustedThroughput_congestedWifi_capsAt450k() {
        val m = NetworkHealthMonitor()
        m.setSimulatedProfile(SimulatedNetworkProfile.CONGESTED_WIFI)
        assertEquals(450_000L, m.adjustedThroughput(10_000_000L))
        m.resetForTests()
    }

    @Test
    fun label_reflectsCurrentProfile() {
        val m = NetworkHealthMonitor()
        m.setSimulatedProfile(SimulatedNetworkProfile.POOR_CELL)
        assertEquals("POOR_CELL", m.label())
        m.resetForTests()
    }
}
