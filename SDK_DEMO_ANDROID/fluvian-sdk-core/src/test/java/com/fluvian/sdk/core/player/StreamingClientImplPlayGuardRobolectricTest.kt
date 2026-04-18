package com.fluvian.sdk.core.player

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.fluvian.sdk.core.AnalyticsTracker
import com.fluvian.sdk.core.NoOpAnalyticsTracker
import com.fluvian.sdk.core.internal.di.FluvianInternalComponents
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.atomic.AtomicReference

/**
 * File: StreamingClientImplPlayGuardRobolectricTest.kt
 * Description: Validates guard rails without constructing ExoPlayer (no [initialize] call).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.6
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class StreamingClientImplPlayGuardRobolectricTest {

    @Test
    fun playWithoutInitialize_surfacesErrorToAnalytics() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val err = AtomicReference<Throwable?>(null)
        val tracker =
            object : AnalyticsTracker {
                override fun onPlay() = Unit

                override fun onBufferStart() = Unit

                override fun onBufferEnd() = Unit

                override fun onError(error: Throwable) {
                    err.set(error)
                }
            }
        val client = StreamingClientImpl(ctx, tracker, drmConfig = null)
        client.play("https://example.com/stream.m3u8")
        ShadowLooper.shadowMainLooper().idle()
        assertTrue(err.get() is IllegalStateException)
    }

    @Test
    fun internalConstructor_wiresProvidedFluvianInternalComponents() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val components = FluvianInternalComponents.createDefault()
        val client = StreamingClientImpl(ctx, NoOpAnalyticsTracker, drmConfig = null, components)
        assertSame(components.networkHealthMonitor, client.networkHealthMonitor())
        assertSame(components.bandwidthPredictor, client.bandwidthPredictor())
    }
}
