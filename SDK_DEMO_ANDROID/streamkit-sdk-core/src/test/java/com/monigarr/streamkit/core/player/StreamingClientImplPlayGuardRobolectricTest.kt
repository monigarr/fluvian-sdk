package com.monigarr.streamkit.core.player

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.monigarr.streamkit.core.AnalyticsTracker
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
 * Version: 1.3.4
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
}
