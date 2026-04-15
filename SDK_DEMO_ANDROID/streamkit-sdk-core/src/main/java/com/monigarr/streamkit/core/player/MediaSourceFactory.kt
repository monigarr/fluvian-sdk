package com.monigarr.streamkit.core.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import com.monigarr.streamkit.core.DrmConfig
import com.monigarr.streamkit.core.StreamConfig
import java.util.Locale

/**
 * File: MediaSourceFactory.kt
 * Description: Produces HLS or DASH [MediaSource] instances with optional Widevine configuration on the [MediaItem].
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.2.1
 *
 * Usage:
 *   Invoked from [StreamingClientImpl.play] after [initialize] supplies [StreamConfig]. DASH is selected when the path ends in `.mpd` (case-insensitive).
 *
 * Usage example:
 *   val factory = MediaSourceFactory(context, StreamConfig())
 *   val hls = factory.createMediaSource("https://example.com/master.m3u8", drmConfig = null)
 *   val dash = factory.createMediaSource("https://example.com/manifest.mpd", drmConfig = null)
 */

internal enum class StreamManifestKind {
    HLS,
    DASH,
}

internal fun streamManifestKindForUrl(url: String): StreamManifestKind {
    val pathLike = url.substringBefore('?').lowercase(Locale.US)
    val fileName = pathLike.substringAfterLast('/')
    return if (fileName.endsWith(".mpd")) StreamManifestKind.DASH else StreamManifestKind.HLS
}
class MediaSourceFactory(
    private val context: Context,
    private val streamConfig: StreamConfig,
) {

    fun createMediaSource(url: String, drmConfig: DrmConfig?): MediaSource {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(streamConfig.userAgent ?: Util.getUserAgent(context, "StreamKit"))
        if (streamConfig.httpHeaders.isNotEmpty()) {
            httpFactory.setDefaultRequestProperties(streamConfig.httpHeaders)
        }
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
        val mediaItemBuilder = MediaItem.Builder().setUri(url)

        drmConfig?.let { drm ->
            val drmBuilder = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(drm.licenseUrl)
                .setMultiSession(true)
            if (drm.headers.isNotEmpty()) {
                drmBuilder.setLicenseRequestHeaders(drm.headers)
            }
            mediaItemBuilder.setDrmConfiguration(drmBuilder.build())
        }

        streamConfig.liveTargetOffsetMs?.let { targetMs ->
            val liveConfig =
                MediaItem.LiveConfiguration.Builder()
                    .setTargetOffsetMs(targetMs)
                    .build()
            mediaItemBuilder.setLiveConfiguration(liveConfig)
        }

        val mediaItem = mediaItemBuilder.build()
        return when (streamManifestKindForUrl(url)) {
            StreamManifestKind.DASH ->
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            StreamManifestKind.HLS ->
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
    }
}
