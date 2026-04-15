/**
 * File: AssetManager3D.kt
 * Description: Reference-counted GPU asset registry to prevent texture/model leaks when overlays attach/detach from the video pipeline.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.2.0
 *
 * Usage:
 *   Call [retain]/[release] from a background dispatcher; observe [pressure] to throttle uploads when VRAM pressure is high.
 *
 * Usage example:
 *   assetManager.retain("hud-atlas")
 */
package com.monigarr.streamkit.core.assets

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class GpuAssetPressure(
    val retainedAssets: Int,
    val estimatedBytes: Long,
)

class AssetManager3D(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + ioDispatcher)

    private val mutex = Mutex()
    private val refCounts = mutableMapOf<String, Int>()
    private val byteWeights = mutableMapOf<String, Long>()

    private val _pressure = MutableStateFlow(GpuAssetPressure(0, 0L))
    val pressure: StateFlow<GpuAssetPressure> = _pressure.asStateFlow()

    fun retain(assetId: String, estimatedBytes: Long = 0L) {
        scope.launch(ioDispatcher) {
            mutex.withLock {
                refCounts[assetId] = (refCounts[assetId] ?: 0) + 1
                if (estimatedBytes > 0L) {
                    byteWeights[assetId] = estimatedBytes
                }
                publishLocked()
            }
        }
    }

    fun release(assetId: String) {
        scope.launch(ioDispatcher) {
            mutex.withLock {
                val next = (refCounts[assetId] ?: 0) - 1
                if (next <= 0) {
                    refCounts.remove(assetId)
                    byteWeights.remove(assetId)
                } else {
                    refCounts[assetId] = next
                }
                publishLocked()
            }
        }
    }

    private fun publishLocked() {
        val totalBytes = byteWeights.values.sum()
        _pressure.value = GpuAssetPressure(retainedAssets = refCounts.size, estimatedBytes = totalBytes)
    }

    fun cancel() {
        job.cancel()
    }

    internal suspend fun resetForTests() {
        mutex.withLock {
            refCounts.clear()
            byteWeights.clear()
            publishLocked()
        }
    }
}
