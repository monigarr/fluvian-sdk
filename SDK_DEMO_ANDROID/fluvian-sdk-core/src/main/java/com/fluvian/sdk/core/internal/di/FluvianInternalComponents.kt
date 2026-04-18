// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: FluvianInternalComponents.kt
 * Description: Internal composition root for Fluvian SDK playback helpers (not exported to host DI graphs).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.6
 *
 * Usage:
 *   Constructed only inside `fluvian-sdk-core`; keeps factories off the public surface area.
 *
 * Usage example:
 *   val components = FluvianInternalComponents.createDefault()
 */
package com.fluvian.sdk.core.internal.di

import com.fluvian.sdk.core.abr.BandwidthPredictor
import com.fluvian.sdk.core.network.NetworkHealthMonitor

/**
 * Internal composition root for networking + ABR helpers used by [com.fluvian.sdk.core.player.StreamingClientImpl].
 *
 * All dependencies are **explicit** in the primary constructor; use [createDefault] for production wiring
 * where the default [NetworkHealthMonitor] / [BandwidthPredictor] pair is appropriate.
 */
internal class FluvianInternalComponents(
    val networkHealthMonitor: NetworkHealthMonitor,
    val bandwidthPredictor: BandwidthPredictor,
) {
    companion object {
        fun createDefault(): FluvianInternalComponents {
            val networkHealthMonitor = NetworkHealthMonitor()
            return FluvianInternalComponents(
                networkHealthMonitor = networkHealthMonitor,
                bandwidthPredictor =
                    BandwidthPredictor(
                        networkHealthMonitor = networkHealthMonitor,
                    ),
            )
        }
    }
}
