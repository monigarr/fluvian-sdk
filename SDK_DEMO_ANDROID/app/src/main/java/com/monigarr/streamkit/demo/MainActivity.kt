/**
 * File: MainActivity.kt
 * Description: Reference Compose shell: binds [StreamingClientImpl] to a TextureView surface with demo chrome and Echelon threading.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.4
 *
 * Usage:
 *   Declared as the launcher activity in AndroidManifest.xml; hosts Compose content for the reference app.
 *
 * Usage example:
 *   startActivity(Intent(this, MainActivity::class.java))
 */
package com.monigarr.streamkit.demo

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import com.monigarr.streamkit.core.AnalyticsTracker
import com.monigarr.streamkit.core.StreamConfig
import com.monigarr.streamkit.core.StreamingDiagnostics
import com.monigarr.streamkit.core.aicore.AIConfig
import com.monigarr.streamkit.core.aicore.AIProviderType
import com.monigarr.streamkit.core.aicore.DEFAULT_OPTIMIZATION_JSON_SCHEMA
import com.monigarr.streamkit.core.aicore.AILayerInference
import com.monigarr.streamkit.core.aicore.OnDeviceGenAiWarmup
import com.monigarr.streamkit.core.aicore.OptimizationDecision
import com.monigarr.streamkit.core.aicore.PlayerOptimizer
import com.monigarr.streamkit.core.aicore.QoSData
import com.google.mlkit.genai.common.FeatureStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.monigarr.streamkit.core.assets.GpuAssetPressure
import com.monigarr.streamkit.core.network.SimulatedNetworkProfile
import com.monigarr.streamkit.core.player.StreamingClientImpl
import com.monigarr.streamkit.core.security.StreamKitSecretStore
import com.monigarr.streamkit.demo.ui.theme.BrandAccentGreen
import com.monigarr.streamkit.demo.ui.theme.BrandAccentPurple
import com.monigarr.streamkit.demo.ui.theme.BrandAccentRed
import com.monigarr.streamkit.demo.ui.theme.BrandBackground
import com.monigarr.streamkit.demo.ui.theme.BrandCardIconBg
import com.monigarr.streamkit.demo.ui.theme.BrandSurface
import com.monigarr.streamkit.demo.ui.theme.BrandTextPrimary
import com.monigarr.streamkit.demo.ui.theme.BrandTextSecondary
import com.monigarr.streamkit.demo.ui.theme.LVSPOCStreamKitTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LVSPOCStreamKitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StreamKitDemoScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private data class DemoStream(val label: String, val url: String, val description: String)

/** Preference key for the encrypted demo store only; the secret value is never logged. */
private const val DEMO_ENCRYPTED_SECRET_PREF_KEY = "demo_integrator_placeholder"

/** Encrypted store key for OpenAI / enterprise API keys (demo). */
private const val DEMO_AI_API_KEY_PREF = "demo_openai_api_key"

private fun demoAiConfig(
    provider: AIProviderType,
    model: String,
    enterpriseEndpoint: String,
    apiKey: String?,
    useAzureApiKeyHeader: Boolean,
    systemPrompt: String?,
    structuredOutputJsonSchema: String?,
    structuredOutputSchemaName: String,
    structuredOutputStrict: Boolean,
): AIConfig {
    val trimmedKey = apiKey?.trim().orEmpty()
    val m = model.trim().ifBlank { if (provider == AIProviderType.LOCAL) "rules-v1" else "gpt-4o-mini" }
    return when (provider) {
        AIProviderType.LOCAL -> AIConfig(AIProviderType.LOCAL, modelName = m)
        AIProviderType.OPENAI ->
            AIConfig(
                providerType = AIProviderType.OPENAI,
                modelName = m,
                endpoint = null,
                apiKey = trimmedKey.takeIf { it.isNotEmpty() },
                systemPrompt = systemPrompt,
                structuredOutputJsonSchema = structuredOutputJsonSchema,
                structuredOutputSchemaName = structuredOutputSchemaName,
                structuredOutputStrict = structuredOutputStrict,
            )
        AIProviderType.CUSTOM_ENTERPRISE ->
            AIConfig(
                providerType = AIProviderType.CUSTOM_ENTERPRISE,
                modelName = m,
                endpoint = enterpriseEndpoint.trim().takeIf { it.startsWith("https://") },
                apiKey = trimmedKey.takeIf { it.isNotEmpty() },
                useAzureStyleApiKeyHeader = useAzureApiKeyHeader,
                systemPrompt = systemPrompt,
                structuredOutputJsonSchema = structuredOutputJsonSchema,
                structuredOutputSchemaName = structuredOutputSchemaName,
                structuredOutputStrict = structuredOutputStrict,
            )
        AIProviderType.ON_DEVICE_GENAI ->
            AIConfig(
                providerType = AIProviderType.ON_DEVICE_GENAI,
                modelName = m,
                systemPrompt = systemPrompt,
                structuredOutputJsonSchema = structuredOutputJsonSchema,
                structuredOutputSchemaName = structuredOutputSchemaName,
                structuredOutputStrict = structuredOutputStrict,
            )
    }
}

private val DEMO_STREAMS =
    listOf(
        DemoStream(
            "Big Buck Bunny",
            "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            "Mux Test Stream • 1080p HLS"
        ),
        DemoStream(
            "Apple Advanced HDR",
            "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
            "fMP4 • 60fps • Dolby Atmos"
        ),
        DemoStream(
            "Tears of Steel",
            "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            "Unified Streaming • 4K VOD"
        ),
        DemoStream(
            "Unified Live (SCTE35)",
            "https://demo.unified-streaming.com/k8s/live/scte35.isml/.m3u8",
            "Live HLS • latency / ON AIR demo"
        ),
    )

private fun playbackContext(base: Context): Context =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        base.createAttributionContext("streamkitPlayback")
    } else {
        base
    }

private fun playbackStateLabel(state: Int): String =
    when (state) {
        Player.STATE_IDLE -> "IDLE"
        Player.STATE_BUFFERING -> "BUFFERING"
        Player.STATE_READY -> "READY"
        Player.STATE_ENDED -> "ENDED"
        else -> "?($state)"
    }

@Composable
private fun StreamKitDemoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var playEvents by remember { mutableIntStateOf(0) }
    var bufferStarts by remember { mutableIntStateOf(0) }
    var sessionStarts by remember { mutableIntStateOf(0) }
    var sessionEnds by remember { mutableIntStateOf(0) }
    var sportsMarkerEvents by remember { mutableIntStateOf(0) }
    var lastError by remember { mutableStateOf<String?>(null) }

    val analytics = remember {
        object : AnalyticsTracker {
            override fun onPlay() {
                playEvents++
            }

            override fun onBufferStart() {
                bufferStarts++
            }

            override fun onBufferEnd() = Unit

            override fun onError(error: Throwable) {
                lastError = error.message ?: error.javaClass.simpleName
            }

            override fun onSessionStart(@Suppress("UNUSED_PARAMETER") sessionId: String) {
                sessionStarts++
            }

            override fun onSessionEnd(@Suppress("UNUSED_PARAMETER") sessionId: String) {
                sessionEnds++
            }

            override fun onSportsEventMarker(markerId: String) {
                sportsMarkerEvents++
            }
        }
    }

    val client = remember {
        StreamingClientImpl(
            context = playbackContext(context.applicationContext),
            analytics = analytics,
            drmConfig = null,
        )
    }

    val aiLayer = remember { AILayerInference() }
    val scope = rememberCoroutineScope()

    var selectedStreamIndex by remember { mutableIntStateOf(0) }
    val streamUrl = DEMO_STREAMS[selectedStreamIndex].url
    var streamMenuExpanded by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }
    var showComposePlayer by remember { mutableStateOf(false) }
    var lastPlayedStreamIndex by remember { mutableIntStateOf(-1) }
    var diagnostics by remember { mutableStateOf<StreamingDiagnostics?>(null) }
    var lastSportsMarkerLine by remember { mutableStateOf<String?>(null) }
    var lastAiDecision by remember { mutableStateOf<OptimizationDecision?>(null) }
    var aiInferenceBusy by remember { mutableStateOf(false) }
    var aiProvider by remember { mutableStateOf(AIProviderType.LOCAL) }
    var aiModelName by remember { mutableStateOf("gpt-4o-mini") }
    var enterpriseEndpoint by remember { mutableStateOf("") }
    var useAzureApiKeyHeader by remember { mutableStateOf(false) }
    var alwaysOnAiOptimization by remember { mutableStateOf(false) }
    /** Seconds behind live edge; 0 = use manifest default ([StreamConfig.liveTargetOffsetMs] null). */
    var liveTargetSeconds by remember { mutableIntStateOf(3) }
    var aiApiKeyDraft by remember { mutableStateOf("") }
    var aiSecretRevision by remember { mutableIntStateOf(0) }
    var aiSystemPromptDraft by remember { mutableStateOf("") }
    var aiJsonSchemaDraft by remember { mutableStateOf("") }
    var aiSchemaNameDraft by remember { mutableStateOf("streamkit_optimization") }
    var aiJsonStrict by remember { mutableStateOf(true) }

    val networkMonitor = remember(client) { client.networkHealthMonitor()!! }
    val networkProfile by networkMonitor.profile.collectAsState()
    val abrHint by client.bandwidthPredictor()!!.hints.collectAsState()
    val assetManager3d = remember(client) { client.assetManager3D()!! }
    val gpuPressure by assetManager3d.pressure.collectAsState(
        initial = GpuAssetPressure(retainedAssets = 0, estimatedBytes = 0L),
    )

    val secretStore = remember(context.applicationContext) { StreamKitSecretStore.create(context.applicationContext) }
    var demoSecretDraft by remember { mutableStateOf("") }
    var demoSecretStored by remember {
        mutableStateOf(secretStore.getSecret(DEMO_ENCRYPTED_SECRET_PREF_KEY) != null)
    }

    LaunchedEffect(
        aiProvider,
        aiModelName,
        enterpriseEndpoint,
        useAzureApiKeyHeader,
        aiApiKeyDraft,
        aiSecretRevision,
        aiSystemPromptDraft,
        aiJsonSchemaDraft,
        aiSchemaNameDraft,
        aiJsonStrict,
    ) {
        val key = secretStore.getSecret(DEMO_AI_API_KEY_PREF) ?: aiApiKeyDraft
        aiLayer.configure(
            demoAiConfig(
                aiProvider,
                aiModelName,
                enterpriseEndpoint,
                key,
                useAzureApiKeyHeader,
                systemPrompt = aiSystemPromptDraft.trim().takeIf { it.isNotEmpty() },
                structuredOutputJsonSchema = aiJsonSchemaDraft.trim().takeIf { it.isNotEmpty() },
                structuredOutputSchemaName = aiSchemaNameDraft.trim().ifBlank { "streamkit_optimization" },
                structuredOutputStrict = aiJsonStrict,
            ),
            context.applicationContext,
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            aiLayer.shutdown()
            client.release()
        }
    }

    LaunchedEffect(initialized, showComposePlayer) {
        while (true) {
            delay(250)
            if (initialized && showComposePlayer) {
                client.refreshDiagnostics { snapshot -> diagnostics = snapshot }
            }
        }
    }

    LaunchedEffect(selectedStreamIndex, initialized, showComposePlayer) {
        if (!initialized || !showComposePlayer) return@LaunchedEffect
        if (lastPlayedStreamIndex != selectedStreamIndex) {
            client.play(DEMO_STREAMS[selectedStreamIndex].url)
            lastPlayedStreamIndex = selectedStreamIndex
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .verticalScroll(scrollState)
            .padding(top = 16.dp, bottom = 32.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "StreamKit SDK Demo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Video Player Card Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(300.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (showComposePlayer) {
                    StreamKitVideoSurface(
                        modifier = Modifier.fillMaxSize(),
                        onSurfaceChanged = client::bindVideoSurface
                    )
                } else {
                    // Placeholder background
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF334155), Color(0xFF0F172A))
                            )
                        )
                    )
                }

                // Overlays
                // Top Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //Badge(text = "LIVE", color = BrandAccentRed, showDot = true)
                    //Badge(text = "400ms", color = Color.Black.copy(alpha = 0.6f))
                    //Spacer(modifier = Modifier.weight(1f))
                    //Badge(text = "18.4k", color = Color.Black.copy(alpha = 0.6f), showDot = true, dotColor = BrandAccentGreen)
                }

                // Bottom Info and Tabs
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        /*Column(modifier = Modifier.weight(1f)) {
                            Text(
                                DEMO_STREAMS[selectedStreamIndex].label,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                DEMO_STREAMS[selectedStreamIndex].description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }*/
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BrandAccentGreen.copy(alpha = 0.8f))
                                    .clickable {
                                        if (!initialized) {
                                            val aiKey = secretStore.getSecret(DEMO_AI_API_KEY_PREF) ?: aiApiKeyDraft
                                            val aiCfg =
                                                demoAiConfig(
                                                    aiProvider,
                                                    aiModelName,
                                                    enterpriseEndpoint,
                                                    aiKey,
                                                    useAzureApiKeyHeader,
                                                    systemPrompt = aiSystemPromptDraft.trim().takeIf { it.isNotEmpty() },
                                                    structuredOutputJsonSchema = aiJsonSchemaDraft.trim().takeIf { it.isNotEmpty() },
                                                    structuredOutputSchemaName = aiSchemaNameDraft.trim().ifBlank { "streamkit_optimization" },
                                                    structuredOutputStrict = aiJsonStrict,
                                                )
                                            client.initialize(
                                                StreamConfig(
                                                    enableBandwidthPredictorHints = true,
                                                    enableAlwaysOnAiOptimization = alwaysOnAiOptimization,
                                                    aiOptimizationIntervalMs = 5_000L,
                                                    aiConfig = if (alwaysOnAiOptimization) aiCfg else null,
                                                    liveTargetOffsetMs =
                                                        liveTargetSeconds.takeIf { it > 0 }?.times(1000L),
                                                ),
                                            ) {
                                                showComposePlayer = true
                                                lastPlayedStreamIndex = selectedStreamIndex
                                                client.play(streamUrl)
                                            }
                                            initialized = true
                                        } else {
                                            client.play(streamUrl)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { client.pause() },
                                enabled = initialized,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Pause,
                                    contentDescription = "Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    showComposePlayer = false
                                    initialized = false
                                    client.stop()
                                },
                                enabled = initialized,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        //TabItem("MAIN", selected = true)
                        //TabItem("TACTICAL")
                        //TabItem("GOAL")
                        //TabItem("DRONE")
                    }

                    StreamKitDefaultTimeBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        client = client,
                        diagnostics = diagnostics,
                        initialized = initialized && showComposePlayer,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SDK FEATURES Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SDK FEATURES",
                style = MaterialTheme.typography.labelLarge,
                color = BrandTextPrimary
            )
            Badge(text = "v${BuildConfig.VERSION_NAME} • Enterprise", color = Color(0xFF1E293B))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SdkDevToolCard(
                title = "Video Source",
                subtitle = "Choose a sample HLS manifest",
                icon = Icons.Filled.DeveloperMode,
                iconTint = BrandAccentPurple,
            ) {
                Text("Sample stream", style = MaterialTheme.typography.labelLarge, color = BrandTextPrimary)
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { streamMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(DEMO_STREAMS[selectedStreamIndex].label)
                    }
                    DropdownMenu(expanded = streamMenuExpanded, onDismissRequest = { streamMenuExpanded = false }) {
                        DEMO_STREAMS.forEachIndexed { index, stream ->
                            DropdownMenuItem(text = { Text(stream.label) }, onClick = {
                                selectedStreamIndex = index
                                streamMenuExpanded = false
                            })
                        }
                    }
                }
            }

            SdkDevToolCard(
                title = "Simulated Network (ABR Stress)",
                subtitle = "Throttle throughput for ABR and lab demos",
                icon = Icons.Filled.Wifi,
                iconTint = Color(0xFF4DB6AC),
            ) {
                Text(
                    "Active profile: ${networkProfile.name.replace('_', ' ')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(onClick = { networkMonitor.setSimulatedProfile(SimulatedNetworkProfile.NONE) }, label = { Text("None") })
                    AssistChip(onClick = { networkMonitor.setSimulatedProfile(SimulatedNetworkProfile.POOR_CELL) }, label = { Text("Poor cell") })
                    AssistChip(onClick = { networkMonitor.setSimulatedProfile(SimulatedNetworkProfile.CONGESTED_WIFI) }, label = { Text("Congested Wi‑Fi") })
                }
            }

            SdkDevToolCard(
                title = "Bandwidth Predictor / ABR Hints",
                subtitle = "Smoothed throughput and track-selection caps",
                icon = Icons.Filled.Speed,
                iconTint = Color(0xFFFFD54F),
            ) {
                Text(
                    "Recommended max video bitrate: ${abrHint.recommendedMaxVideoBitrate} bps\n" +
                        "Estimated throughput (adj.): ${abrHint.estimatedThroughputBps} bps · ${abrHint.reason}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
            }

            SdkDevToolCard(
                title = "Live, Latency & Captions",
                subtitle = "Edge status, live offset, text renditions",
                icon = Icons.Filled.Subtitles,
                iconTint = Color(0xFFF06292),
            ) {
                Text(
                    "Target latency (behind live edge)",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrandTextPrimary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (liveTargetSeconds == 0) "Manifest default" else "${liveTargetSeconds}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandTextSecondary,
                    )
                }
                Slider(
                    value = liveTargetSeconds.toFloat(),
                    onValueChange = { liveTargetSeconds = it.roundToInt().coerceIn(0, 30) },
                    valueRange = 0f..30f,
                    steps = 29,
                    enabled = !initialized,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Applies on Play (0 = Media3 manifest default). Stop to change.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary,
                )
                LiveAndLatencyRow(
                    diagnostics = diagnostics,
                    initialized = initialized,
                    targetLiveOffsetMs = liveTargetSeconds.takeIf { it > 0 }?.times(1000L),
                )
                val d = diagnostics
                if (d != null && initialized) {
                    val bufferAhead = (d.bufferedPositionMs - d.currentPositionMs).coerceAtLeast(0L)
                    Text(
                        "Buffer ahead: ${bufferAhead}ms · Playback: ${playbackStateLabel(d.playbackState)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandTextSecondary
                    )
                }
            }

            SdkDevToolCard(
                title = "AI Layer (M.I.L.E.)",
                subtitle = "Raise, Lower or Hold Steady Video Quality",
                icon = Icons.Filled.AutoAwesome,
                iconTint = BrandAccentPurple,
            ) {
                Text("Provider", style = MaterialTheme.typography.labelLarge, color = BrandTextPrimary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = { aiProvider = AIProviderType.LOCAL },
                        label = { Text("Local rules") },
                    )
                    AssistChip(
                        onClick = { aiProvider = AIProviderType.CUSTOM_ENTERPRISE },
                        label = { Text("Enterprise") },
                    )
                    AssistChip(
                        onClick = { aiProvider = AIProviderType.ON_DEVICE_GENAI },
                        label = { Text("On-device") },
                    )
                }
                if (aiProvider == AIProviderType.ON_DEVICE_GENAI) {
                    val scope = rememberCoroutineScope()
                    var onDeviceWarmupStatus by remember { mutableStateOf<String?>(null) }
                    var onDeviceWarmupBusy by remember { mutableStateOf(false) }
                    Text(
                        "Uses ML Kit Prompt API (Gemini Nano via AICore) on supported devices (API 26+). " +
                            "If the model is unavailable, decisions fall back to local rules.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandTextSecondary,
                    )
                    OutlinedButton(
                        enabled = !onDeviceWarmupBusy && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
                        onClick = {
                            onDeviceWarmupBusy = true
                            onDeviceWarmupStatus = "Pre-download / warm-up running…"
                            scope.launch {
                                val label =
                                    try {
                                        withContext(Dispatchers.IO) {
                                            val s = OnDeviceGenAiWarmup.awaitFeatureReady(timeoutMs = 180_000L)
                                            "Warm-up finished: status=$s (AVAILABLE=${FeatureStatus.AVAILABLE})"
                                        }
                                    } catch (t: Throwable) {
                                        "Warm-up failed: ${t.message}"
                                    } finally {
                                        onDeviceWarmupBusy = false
                                    }
                                onDeviceWarmupStatus = label
                            }
                        },
                    ) {
                        Text("Pre-download Gemini Nano")
                    }
                    onDeviceWarmupStatus?.let { st ->
                        Text(
                            st,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandTextSecondary,
                        )
                    }
                    OutlinedTextField(
                        value = aiSystemPromptDraft,
                        onValueChange = { aiSystemPromptDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Custom system prompt (optional)") },
                        placeholder = { Text("Leave empty for SDK default on-device instructions") },
                        minLines = 2,
                        maxLines = 6,
                    )
                }
                if (aiProvider == AIProviderType.CUSTOM_ENTERPRISE) {
                    OutlinedTextField(
                        value = enterpriseEndpoint,
                        onValueChange = { enterpriseEndpoint = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("HTTPS chat completions URL") },
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Azure-style api-key header", style = MaterialTheme.typography.bodyMedium, color = BrandTextPrimary)
                        Switch(checked = useAzureApiKeyHeader, onCheckedChange = { useAzureApiKeyHeader = it })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Always-on AI (SDK loop)", style = MaterialTheme.typography.bodyMedium, color = BrandTextPrimary)
                        Text(
                            "Set before first Play. Uses M.I.L.E. cadence on the playback thread.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandTextSecondary,
                        )
                    }
                    Switch(
                        checked = alwaysOnAiOptimization,
                        onCheckedChange = { alwaysOnAiOptimization = it },
                        enabled = !initialized,
                    )
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            aiInferenceBusy = true
                            try {
                                val snap = diagnostics
                                val qos =
                                    QoSData(
                                        bitrate = abrHint.recommendedMaxVideoBitrate.coerceAtLeast(1),
                                        bufferHealth = snap?.let { (it.bufferedPositionMs - it.currentPositionMs).coerceAtLeast(0L) } ?: 0L,
                                        droppedFrames = 0,
                                        networkSpeedKbps =
                                            (abrHint.estimatedThroughputBps / 1000L).toInt().coerceAtLeast(1),
                                    )
                                val decision = aiLayer.infer(qos)
                                lastAiDecision = decision
                                if (initialized) {
                                    client.postPlayerOperation { player ->
                                        PlayerOptimizer(player).optimize(qos, decision)
                                    }
                                }
                            } finally {
                                aiInferenceBusy = false
                            }
                        }
                    },
                    enabled = initialized && !aiInferenceBusy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run AI decision & apply to player")
                }
                Text(
                    "Last AI decision: ${lastAiDecision?.name ?: "—"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
            }

            /*TODO:SdkDevToolCard(
                title = "Encrypted Secret Store (Demo Key)",
                subtitle = "AES-backed prefs for integrator credentials",
                icon = Icons.Filled.Lock,
                iconTint = Color(0xFF4DB6AC),
            ) {
                OutlinedTextField(
                    value = demoSecretDraft,
                    onValueChange = { demoSecretDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Integrator placeholder secret") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            secretStore.putSecret(DEMO_ENCRYPTED_SECRET_PREF_KEY, demoSecretDraft)
                            demoSecretStored = secretStore.getSecret(DEMO_ENCRYPTED_SECRET_PREF_KEY) != null
                        }
                    ) {
                        Text("Save")
                    }
                    OutlinedButton(
                        onClick = {
                            secretStore.removeSecret(DEMO_ENCRYPTED_SECRET_PREF_KEY)
                            demoSecretStored = false
                            demoSecretDraft = ""
                        }
                    ) {
                        Text("Clear")
                    }
                }
                Text(
                    if (demoSecretStored) "A secret is stored for this demo key." else "No secret stored.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
            }*/

/*  TODO        SdkDevToolCard(
                title = "GPU Asset Manager (3D / Overlays)",
                subtitle = "Reference-counted overlay assets",
                icon = Icons.Filled.ViewInAr,
                iconTint = Color(0xFFFFD54F),
            ) {
                Text(
                    "Retained: ${gpuPressure.retainedAssets} · Est. bytes: ${gpuPressure.estimatedBytes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
            }*/

            SdkDevToolCard(
                title = "Analytics",
                subtitle = "Playback and session counters from AnalyticsTracker",
                icon = Icons.Filled.Analytics,
                iconTint = BrandAccentGreen,
            ) {
                Text(
                    "plays=$playEvents · bufferStarts=$bufferStarts · sessionStart=$sessionStarts · sessionEnd=$sessionEnds · " +
                        "sportsMarkers=$sportsMarkerEvents · errors=${lastError ?: "none"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
           /* TODO: FeatureCard(
                title = "Multi-Camera Switch",
                subtitle = "sync 4+ angles with frame-accurate switching",
                icon = Icons.Default.Refresh, // Replace with appropriate icon
                iconTint = Color(0xFF4DB6AC)
            )*/
            /* TODO: FeatureCard(
                title = "Instant Replay Buffer",
                subtitle = "30s rolling DVR, client-side clip export",
                icon = Icons.Default.CheckCircle, // Replace with appropriate icon
                iconTint = Color(0xFFF06292)
            )*/
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun Badge(text: String, color: Color, showDot: Boolean = false, dotColor: Color = Color.White) {
    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showDot) {
                Box(modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp), color = Color.White)
        }
    }
}

@Composable
private fun TabItem(text: String, selected: Boolean = false) {
    Surface(
        color = if (selected) BrandAccentPurple else Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun SdkDevToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureStyleIconBox(icon = icon, iconTint = iconTint)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = BrandTextPrimary)
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = BrandTextSecondary)
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = BrandTextSecondary
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun FeatureStyleIconBox(icon: ImageVector, iconTint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BrandCardIconBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String, icon: ImageVector, iconTint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeatureStyleIconBox(icon = icon, iconTint = iconTint)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = BrandTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = BrandTextSecondary)
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = BrandTextSecondary)
        }
    }
}

@Composable
private fun LiveAndLatencyRow(
    diagnostics: StreamingDiagnostics?,
    initialized: Boolean,
    targetLiveOffsetMs: Long? = null,
) {
    val onAir =
        diagnostics != null && initialized &&
            diagnostics.isCurrentMediaItemLive &&
            diagnostics.playbackState == Player.STATE_READY
    val liveOffset = diagnostics?.liveOffsetMs ?: C.TIME_UNSET
    val targetLabel =
        when {
            targetLiveOffsetMs == null -> "Target: manifest default"
            else -> "Target: ${targetLiveOffsetMs / 1000f}s behind live edge (MediaItem)"
        }
    val latencyLabel =
        when {
            !initialized || diagnostics == null -> "Measured: —"
            liveOffset == C.TIME_UNSET -> "Measured: — (VOD or unknown)"
            else -> "Measured (current live offset): ${liveOffset / 1000f}s"
        }
    val captionLine =
        when {
            !initialized || diagnostics == null ->
                "Captions: — (text renditions unknown)"
            diagnostics.textTrackCount == 0 ->
                "Captions: none advertised in manifest"
            diagnostics.selectedTextTrackSummary != null ->
                "Captions: ${diagnostics.selectedTextTrackSummary} · ${diagnostics.textTrackCount} renditions"
            else ->
                "Captions: off / default · ${diagnostics.textTrackCount} renditions available"
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Live:", style = MaterialTheme.typography.labelLarge)
                if (onAir) {
                    Text(
                        text = "● ON AIR",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    Text("○ not live edge", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(latencyLabel, style = MaterialTheme.typography.bodyMedium)
        }
        Text(targetLabel, style = MaterialTheme.typography.bodyMedium, color = BrandTextSecondary)
        Text(captionLine, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    LVSPOCStreamKitTheme {
        Text("StreamKit")
    }
}
