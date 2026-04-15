package com.monigarr.streamkit.core.player

/**
 * File: StreamingClientImpl.kt
 * Description: Media3-backed [StreamingClient] with analytics forwarding, background playback threading, and HLS/DASH + optional Widevine media items.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.4
 *
 * Usage:
 *   Construct with context, analytics, and optional [DrmConfig]; call [initialize] before [play].
 *
 * Usage example:
 *   val client = StreamingClientImpl(context, NoOpAnalyticsTracker, drmConfig = null)
 *   client.initialize(StreamConfig()) { client.play("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8") }
 */
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.monigarr.streamkit.core.AnalyticsTracker
import com.monigarr.streamkit.core.DrmConfig
import com.monigarr.streamkit.core.StreamConfig
import com.monigarr.streamkit.core.StreamingClient
import com.monigarr.streamkit.core.StreamingDiagnostics
import com.monigarr.streamkit.core.abr.BandwidthPredictor
import com.monigarr.streamkit.core.aicore.AILayerInference
import com.monigarr.streamkit.core.aicore.OptimizationDecision
import com.monigarr.streamkit.core.aicore.PlayerOptimizer
import com.monigarr.streamkit.core.aicore.QoSData
import com.monigarr.streamkit.core.assets.AssetManager3D
import com.monigarr.streamkit.core.internal.di.StreamKitInternalComponents
import com.monigarr.streamkit.core.network.NetworkHealthMonitor
import com.monigarr.streamkit.core.performance.StreamOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

class StreamingClientImpl(
    private val context: Context,
    private val analytics: AnalyticsTracker,
    private val drmConfig: DrmConfig?,
) : StreamingClient {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val internalComponents = StreamKitInternalComponents()
    private val playerProvider = ExoPlayerProvider(appContext)
    private val sdkJob = SupervisorJob()
    private val sdkScope = CoroutineScope(sdkJob + Dispatchers.Main.immediate)
    private val assetManager = AssetManager3D()

    private var orchestrator: StreamOrchestrator? = null
    private var mediaSourceFactory: MediaSourceFactory? = null
    private var streamConfig: StreamConfig = StreamConfig()
    @Volatile private var player: ExoPlayer? = null
    private var bandwidthMeter: DefaultBandwidthMeter? = null
    private var bufferingActive: Boolean = false
    private var hintsJob: Job? = null
    private var aiOptimizationJob: Job? = null
    private var aiLayer: AILayerInference? = null
    private val droppedFramesTotal = AtomicInteger(0)
    private var droppedFramesAtLastAiTick = 0
    private val sessionLock = Any()
    private var sessionId: String? = null

    private val listener =
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                forwardToMain {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            if (!bufferingActive) {
                                bufferingActive = true
                                analytics.onBufferStart()
                            }
                        }
                        Player.STATE_READY, Player.STATE_IDLE, Player.STATE_ENDED -> {
                            if (bufferingActive) {
                                bufferingActive = false
                                analytics.onBufferEnd()
                            }
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                forwardToMain { analytics.onError(error) }
            }
        }

    private val playerAnalyticsListener =
        object : AnalyticsListener {
            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long,
            ) {
                internalComponents.bandwidthPredictor.onPlayerBandwidthEstimate(bitrateEstimate)
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

    private fun forwardToMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    private fun endSessionIfAny() {
        val id =
            synchronized(sessionLock) {
                val s = sessionId
                sessionId = null
                s
            }
        if (id != null) {
            forwardToMain { analytics.onSessionEnd(id) }
        }
    }

    private fun beginSession() {
        val newId = UUID.randomUUID().toString()
        synchronized(sessionLock) {
            sessionId = newId
        }
        forwardToMain { analytics.onSessionStart(newId) }
    }

    private fun buildDiagnosticsSnapshot(p: ExoPlayer): StreamingDiagnostics {
        val liveOffset = p.currentLiveOffset
        val (textTrackCount, selectedTextTrackSummary) = textTrackTelemetry(p)
        return StreamingDiagnostics(
            isCurrentMediaItemLive = p.isCurrentMediaItemLive,
            playbackState = p.playbackState,
            bufferedPositionMs = p.bufferedPosition,
            currentPositionMs = p.currentPosition,
            liveOffsetMs = liveOffset,
            textTrackCount = textTrackCount,
            selectedTextTrackSummary = selectedTextTrackSummary,
            durationMs = p.duration,
            isSeekable = p.isCurrentMediaItemSeekable,
        )
    }

    private fun textTrackTelemetry(p: ExoPlayer): Pair<Int, String?> {
        var total = 0
        var summary: String? = null
        for (group in p.currentTracks.groups) {
            if (group.type != C.TRACK_TYPE_TEXT) continue
            for (i in 0 until group.length) {
                total++
                if (group.isTrackSelected(i)) {
                    val fmt = group.getTrackFormat(i)
                    val lang = fmt.language ?: "und"
                    summary =
                        if (!fmt.label.isNullOrBlank()) {
                            "$lang (${fmt.label})"
                        } else {
                            lang
                        }
                }
            }
        }
        return total to summary
    }

    override fun initialize(config: StreamConfig, onPlayerReady: (() -> Unit)?) {
        release()
        streamConfig = config
        mediaSourceFactory = MediaSourceFactory(appContext, streamConfig)
        internalComponents.bandwidthPredictor.resetSession()
        val orch = StreamOrchestrator()
        orchestrator = orch
        orch.post {
            val meter =
                DefaultBandwidthMeter.Builder(appContext)
                    .build()
            bandwidthMeter = meter
            resetDroppedFrameTelemetry()
            val created = playerProvider.createPlayer(drmConfig, orch.playbackLooper, meter)
            created.addListener(listener)
            created.addAnalyticsListener(playerAnalyticsListener)
            player = created
            hintsJob = startHintCollectionIfNeeded(config)
            aiOptimizationJob = startAiOptimizationIfNeeded(config)
            if (onPlayerReady != null) {
                mainHandler.post { onPlayerReady.invoke() }
            }
        }
        beginSession()
    }

    private fun startHintCollectionIfNeeded(config: StreamConfig): Job? {
        if (!config.enableBandwidthPredictorHints) return null
        if (config.enableAlwaysOnAiOptimization && config.aiConfig != null) {
            return null
        }
        return sdkScope.launch {
            internalComponents.bandwidthPredictor.hints.collectLatest { hint ->
                val orch = orchestrator ?: return@collectLatest
                orch.post {
                    val p = player ?: return@post
                    p.trackSelectionParameters =
                        p.trackSelectionParameters
                            .buildUpon()
                            .setMaxVideoBitrate(hint.recommendedMaxVideoBitrate)
                            .build()
                }
            }
        }
    }

    private fun resetDroppedFrameTelemetry() {
        droppedFramesTotal.set(0)
        droppedFramesAtLastAiTick = 0
    }

    private fun droppedFramesDeltaForQoS(): Int {
        val total = droppedFramesTotal.get()
        val delta = (total - droppedFramesAtLastAiTick).coerceAtLeast(0)
        droppedFramesAtLastAiTick = total
        return delta.coerceIn(0, 10_000)
    }

    private suspend fun snapshotDiagnosticsSuspended(orch: StreamOrchestrator): StreamingDiagnostics? =
        suspendCancellableCoroutine { cont ->
            orch.post {
                val p = player
                if (p == null) {
                    cont.resume(null)
                    return@post
                }
                cont.resume(buildDiagnosticsSnapshot(p))
            }
        }

    private fun startAiOptimizationIfNeeded(config: StreamConfig): Job? {
        if (!config.enableAlwaysOnAiOptimization) return null
        val aiCfg = config.aiConfig ?: return null
        val layer = AILayerInference()
        layer.configure(aiCfg, appContext)
        aiLayer = layer
        val interval = config.aiOptimizationIntervalMs.coerceIn(1_000L, 60_000L)
        return sdkScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(interval)
                val orch = orchestrator ?: continue
                val hint = internalComponents.bandwidthPredictor.hints.value
                val diag =
                    runCatching {
                        snapshotDiagnosticsSuspended(orch)
                    }.getOrNull() ?: continue
                val qos =
                    QoSData(
                        bitrate = hint.recommendedMaxVideoBitrate.coerceAtLeast(1),
                        bufferHealth = (diag.bufferedPositionMs - diag.currentPositionMs).coerceAtLeast(0L),
                        droppedFrames = droppedFramesDeltaForQoS(),
                        networkSpeedKbps = (hint.estimatedThroughputBps / 1000L).toInt().coerceAtLeast(1),
                    )
                val decision =
                    runCatching {
                        layer.infer(qos)
                    }.getOrElse {
                        OptimizationDecision.STABILIZE
                    }
                orch.post {
                    val p = player ?: return@post
                    PlayerOptimizer(p).optimize(qos, decision)
                }
            }
        }
    }

    override fun play(streamUrl: String) {
        val orch = orchestrator
        val factory = mediaSourceFactory
        if (orch == null || factory == null) {
            forwardToMain { analytics.onError(IllegalStateException("initialize(StreamConfig) must be called before play()")) }
            return
        }
        sdkScope.launch(Dispatchers.IO) {
            val mediaSource =
                try {
                    factory.createMediaSource(streamUrl, drmConfig)
                } catch (t: Throwable) {
                    forwardToMain { analytics.onError(t) }
                    return@launch
                }
            orch.post {
                val p = player ?: return@post
                p.setMediaSource(mediaSource)
                p.prepare()
                p.playWhenReady = true
                forwardToMain { analytics.onPlay() }
            }
        }
    }

    override fun pause() {
        orchestrator?.post {
            player?.playWhenReady = false
        }
    }

    override fun stop() {
        orchestrator?.post {
            val p = player ?: return@post
            p.pause()
            p.stop()
            p.clearMediaItems()
        }
    }

    override fun release() {
        endSessionIfAny()
        hintsJob?.cancel()
        hintsJob = null
        aiOptimizationJob?.cancel()
        aiOptimizationJob = null
        aiLayer?.shutdown()
        aiLayer = null
        mediaSourceFactory = null
        val orch = orchestrator
        orchestrator = null
        if (orch == null) {
            player = null
            bandwidthMeter = null
            return
        }
        orch.shutdown {
            player?.removeListener(listener)
            player?.removeAnalyticsListener(playerAnalyticsListener)
            player?.release()
            player = null
            bandwidthMeter = null
        }
    }

    override fun player(): Player? = player

    override fun bindVideoSurface(surface: Surface?) {
        orchestrator?.post {
            player?.setVideoSurface(surface)
        }
    }

    override fun refreshDiagnostics(consumer: (StreamingDiagnostics) -> Unit) {
        orchestrator?.post {
            val p = player ?: return@post
            val snapshot = buildDiagnosticsSnapshot(p)
            forwardToMain { consumer(snapshot) }
        }
    }

    override fun networkHealthMonitor(): NetworkHealthMonitor = internalComponents.networkHealthMonitor

    override fun bandwidthPredictor(): BandwidthPredictor = internalComponents.bandwidthPredictor

    override fun assetManager3D(): AssetManager3D = assetManager

    override fun postPlayerOperation(operation: (Player) -> Unit) {
        orchestrator?.post {
            val p = player ?: return@post
            operation(p)
        }
    }
}
