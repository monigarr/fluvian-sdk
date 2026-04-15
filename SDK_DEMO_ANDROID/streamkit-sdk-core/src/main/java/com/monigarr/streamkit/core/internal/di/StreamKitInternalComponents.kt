/**
 * File: StreamKitInternalComponents.kt
 * Description: Internal composition root for StreamKit playback helpers (not exported to host DI graphs).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.2.0
 *
 * Usage:
 *   Constructed only inside `streamkit-sdk-core`; keeps factories off the public surface area.
 *
 * Usage example:
 *   val components = StreamKitInternalComponents()
 */
package com.monigarr.streamkit.core.internal.di

import com.monigarr.streamkit.core.abr.BandwidthPredictor
import com.monigarr.streamkit.core.network.NetworkHealthMonitor

internal class StreamKitInternalComponents(
    val networkHealthMonitor: NetworkHealthMonitor = NetworkHealthMonitor(),
) {
    val bandwidthPredictor: BandwidthPredictor =
        BandwidthPredictor(
            networkHealthMonitor = networkHealthMonitor,
        )
}
