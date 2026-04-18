/**
 * File: StreamingDiagnostics.kt
 * Description: Immutable snapshot of playback telemetry copied off the playback looper for UI consumption.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.6
 *
 * Usage:
 *   Populate via [StreamingClient.refreshDiagnostics] so Compose layers never touch [androidx.media3.common.Player] off-thread.
 *
 * Usage example:
 *   client.refreshDiagnostics { diag -> state.value = diag }
 */
package com.fluvian.sdk.core

import androidx.media3.common.C

data class StreamingDiagnostics(
    val isCurrentMediaItemLive: Boolean,
    val playbackState: Int,
    val bufferedPositionMs: Long,
    val currentPositionMs: Long,
    val liveOffsetMs: Long,
    /** Number of text / subtitle renditions in the current manifest (caption-ready observability). */
    val textTrackCount: Int = 0,
    /** Human-readable summary of the selected text track, or null when captions are off or unknown. */
    val selectedTextTrackSummary: String? = null,
    /** [androidx.media3.common.Player.getDuration]; [C.TIME_UNSET] when unknown (e.g. some live windows). */
    val durationMs: Long = C.TIME_UNSET,
    /** Whether the current item supports seeking ([androidx.media3.common.Player.isCurrentMediaItemSeekable]). */
    val isSeekable: Boolean = false,
)
