// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: FluvianThreading.kt
 * Description: Canonical threading contract for Fluvian playback — read before calling [com.fluvian.sdk.core.StreamingClient] APIs from Compose, Rx, or services.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Reference from KDoc on [com.fluvian.sdk.core.StreamingClient]; do not reflect into host apps.
 *
 * Usage example:
 *   See [com.fluvian.sdk.core.StreamingClient.postPlayerOperation].
 */
package com.fluvian.sdk.core.internal

/**
 * ## Threads in Fluvian Open Core
 *
 * **Main (UI) thread**
 * - Safe: [com.fluvian.sdk.core.StreamingClient.initialize], [com.fluvian.sdk.core.StreamingClient.play],
 *   [com.fluvian.sdk.core.StreamingClient.pause], [com.fluvian.sdk.core.StreamingClient.stop],
 *   [com.fluvian.sdk.core.StreamingClient.release], [com.fluvian.sdk.core.StreamingClient.bindVideoSurface],
 *   [com.fluvian.sdk.core.StreamingClient.refreshDiagnostics], [com.fluvian.sdk.core.StreamingClient.postPlayerOperation].
 * - [com.fluvian.sdk.core.AnalyticsTracker] callbacks invoked from the main thread unless otherwise documented on the tracker.
 * - [com.fluvian.sdk.core.StreamingClient.initialize]’s `onPlayerReady` runs on the main thread after the engine is constructed.
 *
 * **Playback thread** ([com.fluvian.sdk.core.performance.StreamOrchestrator] looper = ExoPlayer’s looper)
 * - All [androidx.media3.common.Player] reads/writes for the instance owned by [com.fluvian.sdk.core.player.StreamingClientImpl]
 *   must happen on this looper. The SDK marshals work via [com.fluvian.sdk.core.StreamingClient.postPlayerOperation].
 * - Do **not** capture [com.fluvian.sdk.core.StreamingClient.player] and touch it from the UI thread; that handle is for
 *   advanced integrations that install their own posting discipline (e.g. `PlayerView` after correct binding).
 *
 * **Background (Default / IO)**
 * - Always-on AI sampling and media-source construction may use coroutine dispatchers; they never mutate
 *   [androidx.media3.common.Player] directly—they finish on the playback looper via
 *   [com.fluvian.sdk.core.performance.StreamOrchestrator.post].
 */
internal object FluvianThreading
