package com.monigarr.streamkit.core

import androidx.media3.common.Player
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * File: StreamingClientStubTest.kt
 * Description: Default interface methods on [StreamingClient] for test doubles.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.0.0
 */
class StreamingClientStubTest {

    @Test
    fun streamingClient_defaults_areNoOpOrNull() {
        val stub =
            object : StreamingClient {
                override fun initialize(config: StreamConfig, onPlayerReady: (() -> Unit)?) = Unit

                override fun play(streamUrl: String) = Unit

                override fun pause() = Unit

                override fun stop() = Unit

                override fun release() = Unit

                override fun player(): Player? = null
            }
        var sawDiag = false
        stub.refreshDiagnostics { sawDiag = true }
        assertFalse(sawDiag)
        assertNull(stub.networkHealthMonitor())
        assertNull(stub.bandwidthPredictor())
        assertNull(stub.assetManager3D())
        assertNull(stub.player())
    }
}
