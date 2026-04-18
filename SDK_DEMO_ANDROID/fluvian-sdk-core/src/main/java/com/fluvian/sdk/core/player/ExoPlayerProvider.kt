package com.fluvian.sdk.core.player

import android.content.Context
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import com.fluvian.sdk.core.DrmConfig

/**
 * File: ExoPlayerProvider.kt
 * Description: Builds a configured Media3 [ExoPlayer] for adaptive HLS/DASH with optional DRM-aware downstream wiring.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.6
 *
 * Usage:
 *   Used by [StreamingClientImpl]; host apps normally do not construct this directly.
 *
 * Usage example:
 *   val player = ExoPlayerProvider(context).createPlayer(drmConfig = null, playbackLooper, bandwidthMeter, loadControl)
 */
class ExoPlayerProvider(private val context: Context) {

    fun createPlayer(
        @Suppress("UNUSED_PARAMETER") drmConfig: DrmConfig?,
        playbackLooper: Looper,
        bandwidthMeter: BandwidthMeter,
        loadControl: LoadControl,
    ): ExoPlayer {
        // Do not cap video to SD (1279×719): many ladders start at 720p; that cap can yield audio-only
        // playback (black PlayerView while audio still decodes).
        val trackSelector = DefaultTrackSelector(context)

        val audioAttributes =
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()

        return ExoPlayer.Builder(context)
            .setLooper(playbackLooper)
            .setBandwidthMeter(bandwidthMeter)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .build()
    }
}
