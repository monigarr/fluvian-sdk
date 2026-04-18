# Fluvian SDK — Technical Architecture

| Field | Value |
|-------|--------|
| **Document version** | 1.3.6 |
| **Date** | 2026-04-18 |
| **Author** | monigarr@monigarr.com |

This document is the engineering contract for the **Open Core** Android library (`:fluvian-sdk-core`) and the reference demo (`:app`). It aligns with the MoniGarr operating model (**systems over features**, **control over exposure**) and the **M.I.L.E.** loop (**Measure → Interpret → Learn → Execute**) on streaming paths.

---

## 1. Repository and modules

| Module | Responsibility |
|--------|------------------|
| `:fluvian-sdk-core` | Public Kotlin API, Media3 integration, QoS façade, AI abstractions, JNI sample bridge |
| `:app` | Compose reference shell, white-label demo wiring, analytics counters |

**Open Core rule:** no Gradle `implementation(project(":…"))` may reference artifacts that are not committed in this repository. Commercial **PRO** layers ship as **private AARs** or customer-specific forks under contract.

---

## 2. Runtime threading model

- **UI thread:** Compose and host chrome; never owns ExoPlayer mutation.
- **Playback thread:** A dedicated [`HandlerThread`](https://developer.android.com/reference/android/os/HandlerThread) owned by `com.fluvian.sdk.core.performance.StreamOrchestrator` exposes `playbackLooper` used when constructing `ExoPlayer`.
- **Background coroutines:** `StreamingClientImpl` uses structured concurrency for media-source preparation and optional always-on AI cadence (`Dispatchers.IO` / `Dispatchers.Default`), marshalling player mutations back through `StreamOrchestrator.post`.

See `FluvianThreading.kt` for the authoritative matrix.

---

## 3. M.I.L.E. on streaming

| Stage | Open Core implementation |
|-------|---------------------------|
| **Measure** | `QoSController.playerListener`, `QoSController.analyticsListener`, `BandwidthPredictor`, `QoSMetrics`, `PlaybackQoSSignals` |
| **Interpret** | `RuleBasedQoSDecisionEngine`, `AILayerInference` / `AIManager` → `AIProvider` |
| **Learn** | `QoSController.learn` no-op hook (telemetry export lives in enterprise services) |
| **Execute** | `PlayerOptimizer` (track selection caps), `LoadControl` via `QoSController.createInitialLoadControl` |

---

## 4. Layer map (logical packages)

| Layer | Package roots | Rule |
|-------|-----------------|------|
| Core contracts | `com.fluvian.sdk.core` | Stable interfaces consumed by hosts |
| Player | `…core.player` | Media3 factories, `StreamingClientImpl`, optimizers |
| DRM | `…core` (`DrmConfig`) | Configuration types only — **no** embedded secrets |
| Analytics | `…core` (`AnalyticsTracker`) | Events only; no PII in defaults |
| QoS | `…core.qos`, `…core.abr`, `…core.network` | May import `QoSData` / `OptimizationDecision` from `aicore` only (see `SdkLayerBoundaryTest`) |
| AI | `…core.aicore` | Must **not** import `…core.qos` (boundary test enforced) |

---

## 5. Security and IP posture

- **`.gitignore` is not a license boundary.** It excludes local secrets and large binaries only. All Kotlin required to compile Open Core **must** be tracked in Git.
- **Stub intelligence:** Public tree ships deterministic placeholders for prediction, on-device GenAI, and LLM parsing. **PRO** replaces internals without changing host-facing APIs where possible.
- **Secrets:** Widevine license URLs, API keys, and model weights are loaded from integrator vaults at runtime — never committed.

### 5a. DRM

`DrmConfig` carries HTTPS license endpoints and header **names** only; values are supplied by your secure channel.

---

## 6. Testing and coverage

- **Unit tests:** `:fluvian-sdk-core:testDebugUnitTest`, `:app:testDebugUnitTest` (Robolectric where applicable).
- **Instrumented tests:** `:fluvian-sdk-core:connectedDebugAndroidTest` for Android-only paths.
- **API stability:** `./gradlew :fluvian-sdk-core:apiCheck` (kotlinx **Binary Compatibility Validator** against `api/fluvian-sdk-core.api`).
- **JaCoCo:** CI enforces minimum **LINE** ratios on Open Core and the demo app (see `.github/workflows/fluvian-sdk-ci.yml`).

---

## 7. Native bridge

`NativeLib` demonstrates a small CMake/JNI sample for teams extending rendering or telemetry. NDK is a CI prerequisite when touching `src/main/cpp/`.

---

## 8. Quality of Service (QoS) pipeline

1. **Collect:** `QoSController` registers `Player.Listener` and `AnalyticsListener`; correlates buffer geometry, bandwidth estimates, and dropped-frame counters.
2. **Model:** `QoSMetrics` is the canonical snapshot; `PlaybackQoSSignals` is the exporter-friendly DTO (`QoSController.buildPlaybackQoSSignals`).
3. **Interpret:** `RuleBasedQoSDecisionEngine` emits `QoSDecision`.
4. **Execute:** `PlayerOptimizer` applies conservative `TrackSelectionParameters` caps on the playback looper.

`DefaultLoadControl` policy is centralized in `QoSController.createInitialLoadControl` for predictable buffering behavior in Open Core.

---

## 9. AI optimization layer

- **Providers:** `AIProviderFactory` maps `AIConfig.providerType` to `RuleBasedAIProvider`, `OpenAIProvider` (non-networked stub in Open Core), and `OnDeviceGenAiProvider` (deterministic stub).
- **Resolver hook:** `AIProviderResolver` on `StreamConfig` allows enterprises to inject private providers without forking `AIManager`.
- **Parsing:** `LlmDecisionParser` offers JSON/token helpers for tests and tooling — **not** a production safety boundary.

### 9.3 On-device GenAI

Open Core returns `OnDeviceGenAiReadiness.UNAVAILABLE` from `OnDeviceGenAiWarmup.awaitFeatureReady` and `OptimizationDecision.STABILIZE` from `OnDeviceGenAiProvider.predict`. Licensed stacks swap implementations.

---

## 20. SDK distribution

Maven coordinates for Open Core are documented in `SDK_DEMO_ANDROID/publishing.gradle.kts` and `INTEGRATION_APPENDIX.md`.

---

## 25. Echelon enterprise source and script header standard

All new or materially revised Kotlin, Gradle (`*.kts`), and CI Python scripts under `SDK_DEMO_ANDROID/` and `scripts/` carry an Echelon banner: **File**, **Description**, **Author**, **Date**, **Version**, **Usage** (validated by `scripts/check_echelon_headers.py`).

---

## 26. SDK distribution and release engineering

Release engineering aligns:

- `README.md` program table  
- this document’s footer  
- `docs/PRD.md` revision footer  
- `EchelonProgramInfo.DOCUMENT_VERSION`  
- Demo `versionName` / `versionCode`  

---

## 27. Operational readiness checklist

- Fresh clone builds `:app:assembleDebug` on a machine with JDK 17+ and Android SDK (NDK for native changes).
- `./gradlew :fluvian-sdk-core:apiCheck` passes before tagging.
- No `include(":fluvian-sdk-pro-genai")` or similar missing-module wiring appears in Gradle settings.
