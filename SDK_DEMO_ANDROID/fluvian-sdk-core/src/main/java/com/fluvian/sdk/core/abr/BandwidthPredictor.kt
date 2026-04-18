/**
 * File: BandwidthPredictor.kt
 * Description: Open Core **Measure** stage for throughput — exposes conservative [AbrHint] values for integrators and [com.fluvian.sdk.core.qos.QoSController].
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
 *   Construct with [com.fluvian.sdk.core.network.NetworkHealthMonitor]; feed estimates from [androidx.media3.exoplayer.analytics.AnalyticsListener.onBandwidthEstimate].
 *
 * Usage example:
 *   val predictor = BandwidthPredictor(monitor); predictor.onPlayerBandwidthEstimate(estimateBps)
 */
package com.fluvian.sdk.core.abr

import com.fluvian.sdk.core.network.NetworkHealthMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.jvm.JvmOverloads
import kotlin.math.min

/**
 * Open Core bandwidth façade: correlates player estimates with [NetworkHealthMonitor.adjustedThroughput]
 * and publishes a stable [hints] stream for ABR caps.
 *
 * **M.I.L.E.:** **Measure** only — interpretation of policy is downstream ([com.fluvian.sdk.core.qos.QoSController]).
 */
class BandwidthPredictor
    @JvmOverloads
    constructor(
        private val networkHealthMonitor: NetworkHealthMonitor,
        @Suppress("UNUSED_PARAMETER") private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) {
        private val _hints =
            MutableStateFlow(
                AbrHint(
                    recommendedMaxVideoBitrate = Int.MAX_VALUE,
                    estimatedThroughputBps = 1_000_000L,
                    reason = "bootstrap",
                ),
            )

        /** Latest smoothed hint for UI, diagnostics, and optional track caps. */
        val hints: StateFlow<AbrHint> = _hints.asStateFlow()

        /**
         * Ingests raw Media3 bandwidth estimates (bps). Non-positive values are ignored so cold-start caps stay unlimited.
         */
        fun onPlayerBandwidthEstimate(bitrateEstimate: Long) {
            if (bitrateEstimate <= 0L) return
            val adjusted = networkHealthMonitor.adjustedThroughput(bitrateEstimate)
            if (adjusted <= 0L) return
            val cappedVideo = min(adjusted, Int.MAX_VALUE.toLong()).toInt().coerceAtLeast(1)
            _hints.value =
                AbrHint(
                    recommendedMaxVideoBitrate = cappedVideo,
                    estimatedThroughputBps = adjusted,
                    reason = "open-core-measure",
                )
        }

        /** Clears session state; restores bootstrap caps until new valid samples arrive. */
        fun resetSession() {
            _hints.value =
                AbrHint(
                    recommendedMaxVideoBitrate = Int.MAX_VALUE,
                    estimatedThroughputBps = 1_000_000L,
                    reason = "session-reset",
                )
        }
    }
