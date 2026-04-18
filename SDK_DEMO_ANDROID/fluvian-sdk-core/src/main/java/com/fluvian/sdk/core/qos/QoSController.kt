// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.qos

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.fluvian.sdk.core.AnalyticsTracker
import com.fluvian.sdk.core.StreamingDiagnostics
import com.fluvian.sdk.core.abr.BandwidthPredictor
import com.fluvian.sdk.core.aicore.QoSData
import com.fluvian.sdk.core.player.PlayerOptimizer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * File: QoSController.kt
 * Description: Formal **M.I.L.E.** façade over Media3:
 * - **Measure:** [playerListener] ([Player.Listener]) + [analyticsListener] ([AnalyticsListener]) + buffer heuristics.
 * - **Interpret:** [interpret] → [RuleBasedQoSDecisionEngine].
 * - **Learn:** [learn] — Open Core is telemetry-ready (fleet learning lives in PRO services).
 * - **Execute:** [execute] applies [androidx.media3.common.TrackSelectionParameters] via [PlayerOptimizer].
 *
 * [createInitialLoadControl] centralizes [LoadControl] / [DefaultLoadControl] policy for new player instances.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
class QoSController(
    private val bandwidthPredictor: BandwidthPredictor,
    private val analytics: AnalyticsTracker,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val droppedFramesTotal = AtomicInteger(0)
    private var droppedFramesAtLastTick = 0

    private val lastBandwidthEstimateBps = AtomicLong(0L)

    @Volatile private var sawReadyWhileTryingToPlay: Boolean = false

    /** True after first post-ready stall while the user still intends playback (rebuffer QoS). */
    @Volatile private var rebufferingActive: Boolean = false

    @Volatile private var bufferingAnalyticsActive: Boolean = false

    @Volatile private var playWhenReadySnapshot: Boolean = false

    private fun forwardToMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    val playerListener: Player.Listener =
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                forwardToMain {
                    analytics.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_READY -> {
                            sawReadyWhileTryingToPlay = true
                            rebufferingActive = false
                            if (bufferingAnalyticsActive) {
                                bufferingAnalyticsActive = false
                                analytics.onBufferEnd()
                            }
                        }
                        Player.STATE_BUFFERING -> {
                            if (sawReadyWhileTryingToPlay && playWhenReadySnapshot) {
                                rebufferingActive = true
                            }
                            if (!bufferingAnalyticsActive) {
                                bufferingAnalyticsActive = true
                                analytics.onBufferStart()
                            }
                        }
                        Player.STATE_IDLE, Player.STATE_ENDED -> {
                            sawReadyWhileTryingToPlay = false
                            rebufferingActive = false
                            if (bufferingAnalyticsActive) {
                                bufferingAnalyticsActive = false
                                analytics.onBufferEnd()
                            }
                        }
                    }
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                playWhenReadySnapshot = playWhenReady
                if (!playWhenReady) {
                    forwardToMain {
                        sawReadyWhileTryingToPlay = false
                        rebufferingActive = false
                        if (bufferingAnalyticsActive) {
                            bufferingAnalyticsActive = false
                            analytics.onBufferEnd()
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                forwardToMain { analytics.onError(error) }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int,
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    forwardToMain {
                        analytics.onSeek(oldPosition.positionMs, newPosition.positionMs)
                    }
                }
            }
        }

    val analyticsListener: AnalyticsListener =
        object : AnalyticsListener {
            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long,
            ) {
                if (bitrateEstimate > 0L) {
                    lastBandwidthEstimateBps.set(bitrateEstimate)
                }
                bandwidthPredictor.onPlayerBandwidthEstimate(bitrateEstimate)
            }

            override fun onDroppedVideoFrames(
                eventTime: AnalyticsListener.EventTime,
                droppedFrames: Int,
                elapsedMs: Long,
            ) {
                if (droppedFrames > 0) {
                    droppedFramesTotal.addAndGet(droppedFrames)
                }
            }
        }

    fun resetSession() {
        droppedFramesTotal.set(0)
        droppedFramesAtLastTick = 0
        lastBandwidthEstimateBps.set(0L)
        sawReadyWhileTryingToPlay = false
        rebufferingActive = false
        bufferingAnalyticsActive = false
        playWhenReadySnapshot = false
    }

    fun droppedFramesDeltaForTick(): Int {
        val total = droppedFramesTotal.get()
        val delta = (total - droppedFramesAtLastTick).coerceAtLeast(0)
        droppedFramesAtLastTick = total
        return delta.coerceIn(0, 10_000)
    }

    fun buildMetrics(
        player: Player,
        diagnostics: StreamingDiagnostics,
    ): QoSMetrics {
        val hint = bandwidthPredictor.hints.value
        val bw = lastBandwidthEstimateBps.get().takeIf { it > 0L } ?: hint.estimatedThroughputBps
        return QoSMetrics.fromPlayerAndDiagnostics(
            player = player,
            diagnostics = diagnostics,
            hint = hint,
            isRebuffering = rebufferingActive,
            measuredBandwidthBps = bw,
            droppedVideoFramesWindow = droppedFramesDeltaForTick(),
            wallClockElapsedMs = SystemClock.elapsedRealtime(),
        )
    }

    fun interpret(metrics: QoSMetrics): QoSDecision = RuleBasedQoSDecisionEngine.recommend(metrics)

    /**
     * Projects [QoSMetrics] into the canonical **Measure** DTO consumed by analytics exporters and optional **Learn** sinks.
     *
     * **M.I.L.E.:** strictly **Measure** — interpretation remains [interpret].
     */
    fun buildPlaybackQoSSignals(metrics: QoSMetrics): PlaybackQoSSignals =
        PlaybackQoSSignals(
            measuredBandwidthBps = metrics.measuredBandwidthBps,
            rebuffering = metrics.isRebuffering,
            droppedVideoFramesDelta = metrics.droppedVideoFramesWindow,
            playbackLatencyMs = metrics.bufferedAheadMs,
        )

    /**
     * **Learn** — hook point for fleet telemetry / offline policy training. Open Core is a no-op beyond predictor inputs.
     */
    @Suppress("UNUSED_PARAMETER")
    fun learn(
        metrics: QoSMetrics,
        decision: QoSDecision,
    ) {
        // Predictor already ingests bandwidth samples in Measure; PRO pipelines attach exporters here.
    }

    fun execute(
        player: ExoPlayer,
        metrics: QoSMetrics,
        decision: QoSDecision,
    ) {
        PlayerOptimizer(player).optimize(metrics.toQoSData(), decision.toOptimizationDecision())
    }

    /**
     * Policy for [androidx.media3.exoplayer.ExoPlayer.Builder.setLoadControl]. Tunable from one place per session.
     */
    fun createInitialLoadControl(): LoadControl =
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 3000,
                /* maxBufferMs = */ 12000,
                /* bufferForPlaybackMs = */ 1500,
                /* bufferForPlaybackAfterRebufferMs = */ 2500,
            ).build()
}
