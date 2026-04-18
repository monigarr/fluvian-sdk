/**
 * File: StreamOrchestrator.kt
 * Description: Dedicated playback [android.os.HandlerThread] and looper — ExoPlayer construction and mutations are marshalled here.
 *
 * NOTE:
 * This file contains a simplified implementation for evaluation purposes.
 * Advanced optimization logic is part of the private enterprise layer.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Pass [playbackLooper] to [com.fluvian.sdk.core.player.ExoPlayerProvider.createPlayer]; use [post] for all player operations.
 *
 * Usage example:
 *   val orch = StreamOrchestrator(); orch.post { player.prepare() }
 */
package com.fluvian.sdk.core.performance

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import kotlin.jvm.JvmOverloads

/**
 * Owns Media3 / ExoPlayer threading boundaries. Open Core ships a single-thread executor model; enterprise builds may
 * add priority lanes or codec affinity without changing host call sites.
 */
class StreamOrchestrator
    @JvmOverloads
    constructor(
        threadName: String = "FluvianPlayback",
    ) {
        private val thread =
            HandlerThread(threadName).apply {
                isDaemon = true
                start()
            }

        /** Looper that must own the [androidx.media3.exoplayer.ExoPlayer] instance for this session. */
        val playbackLooper: Looper = thread.looper

        private val handler = Handler(playbackLooper)

        /** Queue work on the playback looper (never block the UI thread on player APIs). */
        fun post(block: () -> Unit) {
            handler.post { block() }
        }

        /**
         * Runs [done] on the playback thread, then stops the looper. Callers typically release the player inside [done].
         */
        fun shutdown(done: () -> Unit) {
            handler.post {
                try {
                    done()
                } finally {
                    thread.quitSafely()
                }
            }
        }
    }
