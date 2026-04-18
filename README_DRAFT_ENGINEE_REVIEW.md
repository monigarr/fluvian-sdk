# Fluvian SDK

AI-first Android streaming SDK for live video, DRM, and real-time QoS optimization.

## Overview

Fluvian SDK is a modular Android streaming platform that combines ExoPlayer, DRM-ready architecture, and AI-driven QoS optimization to deliver stable, low-latency video experiences.

It is also a modular, production-ready streaming platform designed for teams building high-performance video applications. It combines:

- ExoPlayer-based playback (AndroidX Media3)
- DRM-ready architecture (Widevine-oriented license and header model)
- Real-time analytics and diagnostics contracts
- AI-driven QoS optimization (pluggable abstraction; open core includes hooks and reference paths)

## Open Core Model

This repository represents the **open core** layer of Fluvian SDK.

**Public (free) in this repo:** SDK architecture, ExoPlayer integration, DRM-ready structure, analytics interfaces, and the AI abstraction layer.

**Paid / private (enterprise):** AI optimization engine, QoS prediction logic, performance tuning system, and production DRM workflows.

**High-touch offerings:** Custom SDK builds, streaming optimization consulting, and client-specific AI integration.

## Repository

Canonical GitHub project: [https://github.com/monigarr/fluvian-sdk](https://github.com/monigarr/fluvian-sdk)

Clone:

```bash
git clone https://github.com/monigarr/fluvian-sdk.git
```

## Maven artifact (library)

Maven coordinates (see `SDK_DEMO_ANDROID/publishing.gradle.kts`):

- **Group:** `com.fluvian.sdk`
- **Artifact:** `fluvian-sdk-core`

## Target Audiences

**Tier 1 (primary revenue):** Streaming platforms (sports, media), mobile teams building video apps, consulting firms (for example InfernoRed), agencies delivering apps to clients.

**Tier 2 (fast entry):** Startups building video MVPs, indie developers needing streaming infrastructure, EdTech, fitness, and live-event applications.

**Tier 3 (authority / long game):** AI and ML communities (Hugging Face, Kaggle), and the broader developer audience (GitHub, YouTube).

## Use Cases

- Live sports streaming
- OTT and mobile video apps
- Real-time event platforms

## White Label

Fluvian SDK can be rebranded for clients—for example as a **Client Streaming SDK** or **Internal Platform**—with a setup fee, customization fee, and optional ongoing support.

**Example positioning:** We provide a ready-to-integrate Android streaming SDK that can be customized and branded for your platform, reducing development time significantly.

## Technical Layout

From `SDK_DEMO_ANDROID`:

- **Public SDK surface:** `fluvian-sdk-core/src/main/java/com/fluvian/sdk/core/StreamingClient.kt` — lifecycle, threading expectations, diagnostics.
- **Media3 implementation:** `fluvian-sdk-core/src/main/java/com/fluvian/sdk/core/player/StreamingClientImpl.kt`
- **DRM bootstrap:** `fluvian-sdk-core/src/main/java/com/fluvian/sdk/core/DrmConfig.kt`
- **Analytics boundary:** `fluvian-sdk-core/src/main/java/com/fluvian/sdk/core/AnalyticsTracker.kt`
- **Reference host app:** `app/src/main/java/com/fluvian/sdk/demo/`

Modules:

- `fluvian-sdk-core`: reusable integration layer
- `app`: demo and contract-validation harness (not a production content service)

## Verification

```bash
cd SDK_DEMO_ANDROID
./gradlew :fluvian-sdk-core:apiCheck :fluvian-sdk-core:testDebugUnitTest :app:testDebugUnitTest
```

Windows PowerShell:

```powershell
Set-Location SDK_DEMO_ANDROID
.\gradlew.bat :fluvian-sdk-core:apiCheck :fluvian-sdk-core:testDebugUnitTest :app:testDebugUnitTest
```

Then open the `app` run configuration in Android Studio for end-to-end playback.

## Documentation

- Product scope: `docs/PRD.md`
- Technical architecture: `docs/ARCHITECTURE.md`
- Program handbook: `docs/PROGRAM_HANDBOOK.md`
- History: `CHANGELOG.md`

## Risks

- Integration risk if host apps bypass `StreamingClient` lifecycle and threading contracts.
- DRM risk if `DrmConfig` license URLs and headers are not managed per environment.
- Analytics risk if `AnalyticsTracker` is not connected to your observability stack.
- Performance risk without ABR tuning and diagnostics review on production networks and devices.
