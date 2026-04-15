package com.monigarr.streamkit.core

import android.view.Surface
import androidx.media3.common.Player
import com.monigarr.streamkit.core.abr.BandwidthPredictor
import com.monigarr.streamkit.core.aicore.AIConfig
import com.monigarr.streamkit.core.assets.AssetManager3D
import com.monigarr.streamkit.core.network.NetworkHealthMonitor

/**
 * File: StreamingClient.kt
 * Description: Public playback lifecycle contract plus stream bootstrap configuration for the StreamKit SDK.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.4
 *
 * Usage:
 *   Obtain [StreamingClientImpl] (or a test double), call [initialize], then [play] with an HTTPS HLS or DASH (.mpd) URL.
 *
 * Usage example:
 *   val client = StreamingClientImpl(context, analytics, drmConfig = null)
 *   client.initialize(StreamConfig()) { client.play("https://example.com/live/playlist.m3u8") }
 */
interface StreamingClient {
    /**
     * Replaces the underlying player. Callers that bind [player] to a `PlayerView`
     * (or any video surface) must set that view's player to `null` first so the surface is released
     * before the previous player instance is torn down; otherwise rapid re-init can race EGL teardown
     * (for example `eglQueryContext` failures).
     *
     * @param onPlayerReady invoked on the main thread after the playback engine is constructed on the dedicated playback thread.
     */
    fun initialize(config: StreamConfig, onPlayerReady: (() -> Unit)? = null)

    fun play(streamUrl: String)
    fun pause()
    fun stop()
    fun release()

    /**
     * Returns the Media3 [Player] handle for advanced integrations.
     *
     * **Threading:** StreamKit constructs ExoPlayer on a dedicated playback [android.os.Looper]; callers must not read or
     * mutate the instance from the UI thread. Prefer [refreshDiagnostics] for UI telemetry and [postPlayerOperation] for
     * mutations such as track overrides.
     */
    fun player(): Player?

    /** Binds or clears the decoder surface; safe to call from the UI thread. */
    fun bindVideoSurface(surface: Surface?) {
        // Default no-op for test doubles.
    }

    /** Copies a telemetry snapshot onto the main thread via [consumer]. Safe to call from UI code. */
    fun refreshDiagnostics(consumer: (StreamingDiagnostics) -> Unit) {
        // Default no-op for test doubles.
    }

    /** Optional lab hook for simulated network stress (demo / integration tests). */
    fun networkHealthMonitor(): NetworkHealthMonitor? = null

    /** ABR hint stream driven off the UI thread; null when disabled for a stub client. */
    fun bandwidthPredictor(): BandwidthPredictor? = null

    /** Reference-counted GPU asset helper for AR/overlay pipelines. */
    fun assetManager3D(): AssetManager3D? = null

    /** Executes [operation] on the playback looper when a player exists. */
    fun postPlayerOperation(operation: (Player) -> Unit) {
        // Default no-op for test doubles.
    }
}

/**
 * Bootstrap options applied to HTTP stack and player wiring (no secrets; keep tokens in [DrmConfig]).
 */
data class StreamConfig(
    val userAgent: String? = null,
    val httpHeaders: Map<String, String> = emptyMap(),
    /** When true, applies [BandwidthPredictor] hints to ExoPlayer track selection on the playback thread. */
    val enableBandwidthPredictorHints: Boolean = false,
    /**
     * When true with a non-null [aiConfig], runs [com.monigarr.streamkit.core.aicore.AILayerInference] on a background
     * cadence and applies [com.monigarr.streamkit.core.aicore.PlayerOptimizer] on the playback looper.
     * Automatic ABR hint application is suppressed while this loop is active to avoid fighting [PlayerOptimizer].
     */
    val enableAlwaysOnAiOptimization: Boolean = false,
    /** Interval between AI optimization passes; clamped between 1s and 60s inside the SDK. */
    val aiOptimizationIntervalMs: Long = 4_000L,
    /** Provider configuration for always-on optimization; ignored when [enableAlwaysOnAiOptimization] is false. */
    val aiConfig: AIConfig? = null,
    /**
     * When non-null, sets [androidx.media3.common.MediaItem.LiveConfiguration.Builder.setTargetOffsetMs] for live
     * playback so the player aims for this distance behind the live edge. Null keeps manifest/player defaults.
     * Has no effect on plain VOD streams.
     */
    val liveTargetOffsetMs: Long? = null,
)
