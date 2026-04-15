# Changelog

## v1.3.4 — 2026-04-15

### Documentation and distribution

- **Sponsor brief + ADRs:** Added [docs/EXECUTIVE_BRIEF.md](docs/EXECUTIVE_BRIEF.md) (one-page, PDF-friendly markdown for sponsors) and [docs/adr/0001-template.md](docs/adr/0001-template.md) (ADR template). [docs/PROGRAM_HANDBOOK.md](docs/PROGRAM_HANDBOOK.md) now includes **when to write an ADR** guidance; [README.md](README.md), [docs/PRD.md](docs/PRD.md), and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) section 24 cross-link the new artifacts.
- **Enterprise handoff:** Added [docs/PROGRAM_HANDBOOK.md](docs/PROGRAM_HANDBOOK.md) — role-based reading order, first-week engineering checklist, repository map, governance, glossary, CI runbook and troubleshooting, stakeholder FAQ, and outgoing/incoming handoff checklists. Linked from [README.md](README.md), [docs/PRD.md](docs/PRD.md) section 8, and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) sections 24 and 27.
- **Program version 1.3.4:** [README.md](README.md), [docs/PRD.md](docs/PRD.md), and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) aligned with **`EchelonProgramInfo.DOCUMENT_VERSION`**, demo **`versionName`**, and [SDK_DEMO_ANDROID/publishing.gradle.kts](SDK_DEMO_ANDROID/publishing.gradle.kts) Maven artifact version (coordinated bump).
- **README / ARCHITECTURE §13:** In-app program badge uses **`BuildConfig.VERSION_NAME`** and must match **`EchelonProgramInfo.DOCUMENT_VERSION`**; documented with the equality contract enforced in `:app` unit tests.
- **Version/header parity cleanup:** Standardized stale Echelon file-header metadata across Gradle, Kotlin, test, and native source files to **`Version: 1.3.4`** (with aligned header dates) so code headers, docs, and release metadata remain synchronized.

### Demo

- **`versionCode`:** 3 (with **`versionName` 1.3.4**).
- **Version badge:** SDK FEATURES chip shows **`v{BuildConfig.VERSION_NAME}`** (`buildFeatures.buildConfig = true`). **`ExampleUnitTest.demoVersionName_matchesEchelonDocumentVersion`** asserts `VERSION_NAME == EchelonProgramInfo.DOCUMENT_VERSION` so the label cannot drift from docs or core metadata; Echelon CI runs `:app:testDebugUnitTest`.

## v1.3.3 — 2026-04-14

### Documentation

- **Program version 1.3.3:** [README.md](README.md), [docs/PRD.md](docs/PRD.md), and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) aligned with **`EchelonProgramInfo.DOCUMENT_VERSION`** (`streamkit-sdk-core`) and module Gradle headers.
- **ARCHITECTURE:** Document revision footer and §20 Maven sketch coordinate updated; illustrative Echelon header example in §25 set to **1.3.3**.

### Demo / distribution

- **Demo app:** `versionName` **1.3.3** (was `1.0`); `versionCode` **2**. SDK FEATURES badge reads **`v{EchelonProgramInfo.DOCUMENT_VERSION} · Enterprise`** from the core module (single source of truth).
- **Publishing sketch:** [SDK_DEMO_ANDROID/publishing.gradle.kts](SDK_DEMO_ANDROID/publishing.gradle.kts) artifact version **1.3.3** (was `1.2.2`).

### SDK (headers)

- Echelon file-header **Version** normalized to **1.3.3** on `StreamingClient`, cloud/on-device AI helpers, and parsers that still carried **1.3.1** / **1.3.2**.

### CI

- [.github/workflows/echelon-ci.yml](.github/workflows/echelon-ci.yml) workflow header **Version** / **Date** aligned to **1.3.3** / **2026-04-14**.

## v1.3.2 — 2026-04-14

### SDK

- **On-device AI:** New `AIProviderType.ON_DEVICE_GENAI` and `OnDeviceGenAiProvider` using **ML Kit Prompt API** (`com.google.mlkit:genai-prompt` / `genai-common` **1.0.0-beta2**) with **Gemini Nano** via AICore on supported devices (API 26+). `checkStatus` / `generateContent` run inside `runBlocking`; if the feature is not `AVAILABLE` or inference fails, **rule-based fallback** applies.
- **Configuration:** `AIManager.configure` and `AILayerInference.configure` accept optional `applicationContext` (required for on-device path). `AIProviderFactory.create` mirrors this. `StreamingClientImpl` passes application context into the always-on AI layer.

### Demo

- AI card: **On-device** provider chip and short capability note.

### Documentation

- **ARCHITECTURE / PRD / README:** Program version **1.3.2**; AI sections describe on-device GenAI path and ML Kit dependency.
- **Parity pass:** PRD revision footer aligned to **1.3.2**; ARCHITECTURE §3 `AnalyticsTracker` snippet includes `onSportsEventMarker`; §13 demo table includes **On-device** / `OnDeviceGenAiWarmup`; **v1.2.1** demo bullet corrected (HLS-only curated picker vs `.mpd` URL support).

## v1.3.1 — 2026-04-14

### SDK

- **AIConfig (per tenant):** Optional `systemPrompt`, `structuredOutputJsonSchema`, `structuredOutputSchemaName`, and `structuredOutputStrict`. `OpenAIProvider` sends OpenAI-style `response_format.type=json_schema` when a valid schema string is provided.
- **Parsing:** `LlmDecisionParser.parseOptimizationDecisionFromModelContent` parses JSON (including nested objects) for known decision keys, then falls back to plain-token scanning.
- **Reference:** Public `DEFAULT_OPTIMIZATION_JSON_SCHEMA` constant documents a minimal strict schema for the `decision` field.

### Demo

- AI card: optional custom system prompt, JSON schema editor, default-schema chip, schema name, strict toggle.

### Documentation

- **ARCHITECTURE / PRD / README:** Program version **1.3.1**; stream picker copy and **§13 AI reference integration** aligned with current `MainActivity` (three HLS samples; full AI card behavior).

## v1.3.0 — 2026-04-14

### SDK

- **AI (cloud):** `OpenAIProvider` performs HTTPS OpenAI **chat completions** (and OpenAI-compatible enterprise URLs). `AIConfig` adds `useAzureStyleApiKeyHeader` for Azure-style `api-key` authentication. Network failures fall back to `RuleBasedAIProvider`.
- **AI (always-on):** `StreamConfig` adds `enableAlwaysOnAiOptimization`, `aiOptimizationIntervalMs`, and `aiConfig`. `StreamingClientImpl` runs a background M.I.L.E. loop that applies `PlayerOptimizer` on the playback thread; bandwidth-hint **application** is disabled while the loop is active to avoid conflicting caps.
- **Telemetry:** Dropped video frames are accumulated for QoS when always-on AI is enabled.

### Demo

- AI dev card: provider selection (local / OpenAI / enterprise), optional API key storage, Azure header toggle, always-on SDK loop toggle (before first play).

### Documentation

- **ARCHITECTURE / PRD / README:** Program version **1.3.0**; AI sections updated for cloud inference and always-on optimization.

## v1.2.2 — 2026-04-12

### CI and SDK

- **Binary API gate**: `streamkit-sdk-core` uses JetBrains **kotlinx.binary-compatibility-validator** (`api/` + `:streamkit-sdk-core:apiCheck`). AGP 9 **built-in Kotlin** is opted out via `android.builtInKotlin=false` and `android.newDsl=false` in `gradle.properties` so `org.jetbrains.kotlin.android` can apply (required by the validator until KGP ABI supports built-in Kotlin).
- **Demo**: `StreamKitSecretStore` wired in `MainActivity` with masked input; UI shows only stored vs empty—no secret values in logs.

## v1.2.1 — 2026-04-12

### SDK and demo

- **DASH**: `MediaSourceFactory` builds `DashMediaSource` when the manifest path ends in `.mpd` (case-insensitive); HLS remains the default for other URLs.
- **Analytics**: `AnalyticsTracker` gains privacy-minimized `onSessionStart` / `onSessionEnd` (opaque UUID per `initialize` … `release` cycle); `StreamingClientImpl` emits them on the main thread.
- **Diagnostics**: `StreamingDiagnostics` exposes text-track counts and the selected caption summary for caption-ready UI and observability.
- **Security**: Added `StreamKitSecretStore` (AndroidX Security `EncryptedSharedPreferences`) for integrator API keys and similar secrets outside source control.

### Demo

- Analytics panel shows session start/end counts; live row shows caption readiness from diagnostics. **Correction:** an earlier changelog line mentioned an Akamai DASH row in the picker; the sustained reference app uses **three curated public HTTPS HLS** samples (see README/PRD). DASH remains supported when `play()` is given a URL whose path ends in `.mpd`.

## v1.2.0 — 2026-04-12

### SDK and demo

- **Threading**: Introduced `StreamOrchestrator` (dedicated playback `HandlerThread`) so ExoPlayer/`MediaCodec` wiring runs off the UI thread; `StreamingClientImpl` marshals surface binding, telemetry, and track mutations through the playback looper.
- **ABR**: Added `BandwidthPredictor` (off-thread smoothing + `StateFlow` hints) and `NetworkHealthMonitor` (simulated throttling profiles) with optional `StreamConfig.enableBandwidthPredictorHints` to apply caps on the playback thread.
- **AI**: Added `AILayerInference` for coroutine-first inference; demo uses `postPlayerOperation` so `PlayerOptimizer` never mutates ExoPlayer from Compose.
- **3D assets**: Added `AssetManager3D` (reference-counted registry + pressure `StateFlow`) for overlay/AR texture lifecycle hygiene.
- **DI**: Added internal `StreamKitInternalComponents` factory so composition stays inside `streamkit-sdk-core` without leaking a DI framework into host apps.
- **Demo**: Curated public HTTPS HLS fixtures (Akamai synthetic live, Apple Advanced fMP4, Mux Big Buck Bunny, Unified Streaming Tears of Steel), `StreamKitVideoSurface` (`TextureView` → `bindVideoSurface`), and live ABR/AI readouts.

### Documentation

- **ARCHITECTURE / README / PRD**: Documented Echelon threading, Kotlin **Binary Compatibility Validator** adoption path, Maven coordinates (`com.monigarr.streamkit:streamkit-sdk-core`), GLSL/PBR overlay guidance, and Widevine/4K thermal buffering notes.
- **README / PRD / CHANGELOG / ARCHITECTURE §26**: Aligned demo stream list with `MainActivity` (four curated public HLS fixtures), documented CI **NDK** prerequisite and Gradle/JaCoCo steps, and bumped Echelon workflow header version to **1.2.0**.

## v1.1.1 — 2026-04-12

### Documentation

- **README**: Repo layout includes `scripts/` and `.github/workflows/`; Windows `gradlew.bat` note; integration example uses `StreamConfig`, correct `StreamingClientImpl` package, and `player()` for UI binding.
- **ARCHITECTURE**: Core API snippets aligned with `streamkit-sdk-core` (`StreamingClient.player()`, `StreamConfig`, `MediaSourceFactory` + `StreamingClientImpl` flow, `DrmConfig` defaults, header example version).
- **CHANGELOG**: Prior **v1.0.0** entry rewritten for this program (removed unrelated template wording).
- **Removed**: `BLANK_README.md` (unused third-party README template).

## v1.1.0 — 2026-04-12

### Added or changed

- **Echelon enterprise documentation**: Rewrote [docs/PRD.md](docs/PRD.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), and [README.md](README.md) for cross-aligned **version 1.1.0**, executive handoff, governance, and testing expectations.
- **ARCHITECTURE**: Removed duplicate legacy appendices; added sections **20–27** (SDK distribution, ABR/profiling/rendering, extended AI, optional 3D layer, documentation versioning, **mandatory source/script headers**, enterprise testing, operational readiness).
- **SDK**: Added Echelon **KDoc file header** to `NativeLib.kt` (author, date, version, usage, example).
- **Echelon headers**: Applied to `MainActivity.kt`, all example unit/instrumented tests, Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `libs.versions.toml`, `publishing.gradle.kts`), and `core.cpp`.
- **CI**: [`.github/workflows/echelon-ci.yml`](.github/workflows/echelon-ci.yml) runs `scripts/check_echelon_headers.py`, Gradle unit tests, `jacocoStreamkitCoreDebug`, and `scripts/check_jacoco_coverage.py` (default **10%** LINE on `streamkit-sdk-core`).
- **Coverage**: `streamkit-sdk-core` JaCoCo task uses AGP **9** Kotlin class dir `intermediates/built_in_kotlinc/...`; added `EchelonProgramInfo.describe()` for measurable line coverage.
- **Fix**: App module now depends on `project(":streamkit-sdk-core")` (was incorrect `:streamkit-core`).

### Removed

- Stale duplicate blocks previously pasted into `PRD.md` and `README.md`.

## v1.0.0

### Added

- Initial **LVSPOC StreamKit** repository baseline: Android reference project under `SDK_DEMO_ANDROID/`, core streaming module, and early documentation structure before the Echelon **1.1.x** documentation and CI pass.