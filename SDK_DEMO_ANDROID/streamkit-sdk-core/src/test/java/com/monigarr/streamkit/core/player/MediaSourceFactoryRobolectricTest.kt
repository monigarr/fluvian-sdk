package com.monigarr.streamkit.core.player

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.monigarr.streamkit.core.DrmConfig
import com.monigarr.streamkit.core.StreamConfig
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * File: MediaSourceFactoryRobolectricTest.kt
 * Description: JVM integration tests for HLS/DASH [androidx.media3.exoplayer.source.MediaSource] wiring.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MediaSourceFactoryRobolectricTest {

    @Test
    fun createMediaSource_buildsHlsAndDash() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val factory = MediaSourceFactory(ctx, StreamConfig(userAgent = "streamkit-unit-test"))
        val hls = factory.createMediaSource("https://example.com/path/master.m3u8", drmConfig = null)
        val dash = factory.createMediaSource("https://example.com/path/manifest.mpd", drmConfig = null)
        assertNotNull(hls)
        assertNotNull(dash)
    }

    @Test
    fun createMediaSource_acceptsDrmConfig() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val factory = MediaSourceFactory(ctx, StreamConfig())
        val drm = DrmConfig(licenseUrl = "https://license.example.com", headers = mapOf("Authorization" to "Bearer x"))
        val dash = factory.createMediaSource("https://example.com/secure.mpd", drmConfig = drm)
        assertNotNull(dash)
    }
}
