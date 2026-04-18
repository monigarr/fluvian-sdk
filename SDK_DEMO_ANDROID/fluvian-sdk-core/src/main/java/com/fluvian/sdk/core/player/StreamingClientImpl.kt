package com.fluvian.sdk.core.player

/**
 * File: StreamingClientImpl.kt
 * Description: Media3-backed [StreamingClient] with analytics forwarding, background playback threading, and HLS/DASH + optional Widevine media items.
 *
 * **M.I.L.E. (Measure → Interpret → Learn → Execute)** is centralized in [com.fluvian.sdk.core.qos.QoSController]:
 * - **Measure:** [androidx.media3.exoplayer.analytics.AnalyticsListener] + [Player.Listener] + [StreamingDiagnostics] snapshots.
 * - **Interpret:** [com.fluvian.sdk.core.qos.RuleBasedQoSDecisionEngine] / [com.fluvian.sdk.core.aicore.AILayerInference].
 * - **Learn:** [com.fluvian.sdk.core.qos.QoSController.learn] (Open Core hook; fleet learning in PRO).
 * - **Execute:** [androidx.media3.common.TrackSelectionParameters] via [com.fluvian.sdk.core.player.PlayerOptimizer];
 *   [androidx.media3.exoplayer.LoadControl] from [com.fluvian.sdk.core.qos.QoSController.createInitialLoadControl] + [ExoPlayerProvider].
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.6
 *
 * Usage:
 *   Construct with context, analytics, and optional [DrmConfig] (license URL and headers from your secure channel only;
 *   never hard-coded production DRM endpoints in a public tree); call [initialize] before [play].
 *
 * Usage example:
 *   val client = StreamingClientImpl(context, NoOpAnalyticsTracker, drmConfig = null)
 *   client.initialize(StreamConfig()) { client.play("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8") }
 *
 * **Tests (same module):** use the internal four-argument constructor to supply a custom [FluvianInternalComponents]
 * graph (fakes / shared monitors) without changing the public three-argument API.
 *
 */
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import com.fluvian.sdk.core.AnalyticsStreamConfigSummary
import com.fluvian.sdk.core.AnalyticsTracker
import com.fluvian.sdk.core.DrmConfig
import com.fluvian.sdk.core.StreamConfig
import com.fluvian.sdk.core.StreamingClient
import com.fluvian.sdk.core.StreamingDiagnostics
import com.fluvian.sdk.core.abr.BandwidthPredictor
import com.fluvian.sdk.core.aicore.AILayerInference
import com.fluvian.sdk.core.assets.AssetManager3D
import com.fluvian.sdk.core.internal.di.FluvianInternalComponents
import com.fluvian.sdk.core.network.NetworkHealthMonitor
import com.fluvian.sdk.core.performance.StreamOrchestrator
import com.fluvian.sdk.core.qos.QoSController
import com.fluvian.sdk.core.qos.QoSDecision
import com.fluvian.sdk.core.qos.QoSMetrics
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
import kotlin.coroutines.resume

class StreamingClientImpl private constructor(
    context: Context,
    private val analytics: AnalyticsTracker,
    private val drmConfig: DrmConfig?,
    private val internalComponents: FluvianInternalComponents,
    private val playerProvider: ExoPlayerProvider,
    private val assetManager: AssetManager3D,
) : StreamingClient {

    /**
     * Public host entry point — uses [FluvianInternalComponents.createDefault] for the internal graph.
     */
    constructor(
        context: Context,
        analytics: AnalyticsTracker,
        drmConfig: DrmConfig?,
    ) : this(
        context = context,
        analytics = analytics,
        drmConfig = drmConfig,
        internalComponents = FluvianInternalComponents.createDefault(),
        playerProvider = ExoPlayerProvider(context.applicationContext),
        assetManager = AssetManager3D(),
    )

    /**
     * Same-module tests only: inject a custom [FluvianInternalComponents] (e.g. shared [NetworkHealthMonitor] / predictor fakes).
     */
    internal constructor(
        context: Context,
        analytics: AnalyticsTracker,
        drmConfig: DrmConfig?,
        internalComponents: FluvianInternalComponents,
    ) : this(
        context = context,
        analytics = analytics,
        drmConfig = drmConfig,
        internalComponents = internalComponents,
        playerProvider = ExoPlayerProvider(context.applicationContext),
        assetManager = AssetManager3D(),
    )

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val sdkJob = SupervisorJob()
    private val sdkScope = CoroutineScope(sdkJob + Dispatchers.Main.immediate)

    private var orchestrator: StreamOrchestrator? = null
    private var mediaSourceFactory: MediaSourceFactory? = null
    private var streamConfig: StreamConfig = StreamConfig()
    @Volatile private var player: ExoPlayer? = null
    private var bandwidthMeter: DefaultBandwidthMeter? = null
    private var hintsJob: Job? = null
    private var aiOptimizationJob: Job? = null
    private var aiLayer: AILayerInference? = null
    private var qosController: QoSController? = null
    private val sessionLock = Any()
    private var sessionId: String? = null

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
        val qc = QoSController(internalComponents.bandwidthPredictor, analytics)
        qosController = qc
        forwardToMain {
            analytics.onStreamInitialized(
                AnalyticsStreamConfigSummary(
                    bandwidthPredictorHints = config.enableBandwidthPredictorHints,
                    alwaysOnAiOptimization = config.enableAlwaysOnAiOptimization,
                    aiProviderTypeName = config.aiConfig?.providerType?.name,
                    analyticsTenantKey = config.sdkBrand?.analyticsTenantKey,
                    clientMetadataEntryCount = config.clientMetadata.size,
                ),
            )
        }
        val orch = StreamOrchestrator()
        orchestrator = orch
        orch.post {
            val meter =
                DefaultBandwidthMeter.Builder(appContext)
                    .build()
            bandwidthMeter = meter
            qc.resetSession()
            val loadControl = qc.createInitialLoadControl()
            val created = playerProvider.createPlayer(drmConfig, orch.playbackLooper, meter, loadControl)
            created.addListener(qc.playerListener)
            created.addAnalyticsListener(qc.analyticsListener)
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

    private fun startAiOptimizationIfNeeded(config: StreamConfig): Job? {
        if (!config.enableAlwaysOnAiOptimization) return null
        val aiCfg = config.aiConfig ?: return null
        val layer = AILayerInference()
        layer.configure(aiCfg, appContext, streamConfig.aiProviderResolver)
        aiLayer = layer
        val interval = config.aiOptimizationIntervalMs.coerceIn(1_000L, 60_000L)
        return sdkScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(interval)
                val orch = orchestrator ?: continue
                val qc = qosController ?: continue
                val metrics: QoSMetrics? =
                    runCatching {
                        suspendCancellableCoroutine { cont ->
                            orch.post {
                                val p = player
                                if (p == null) {
                                    cont.resume(null)
                                    return@post
                                }
                                val diag = buildDiagnosticsSnapshot(p)
                                cont.resume(qc.buildMetrics(p, diag))
                            }
                        }
                    }.getOrNull()
                val m = metrics ?: continue
                val decision =
                    try {
                        QoSDecision.fromOptimizationDecision(layer.infer(m.toQoSData()))
                    } catch (_: Throwable) {
                        qc.interpret(m)
                    }
                qc.learn(m, decision)
                orch.post {
                    val p = player ?: return@post
                    qc.execute(p, m, decision)
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
        val qc = qosController
        orchestrator = null
        qosController = null
        if (orch == null) {
            player = null
            bandwidthMeter = null
            return
        }
        orch.shutdown {
            val p = player
            if (p != null && qc != null) {
                p.removeListener(qc.playerListener)
                p.removeAnalyticsListener(qc.analyticsListener)
            }
            p?.release()
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
