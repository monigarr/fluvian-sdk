# LVSPOC StreamKit (Architecture Evaluation)

Android streaming reference architecture for live sports use cases: a Kotlin SDK module (`streamkit-sdk-core`) plus a host integration module (`app`) for validating API contracts, playback behavior, DRM wiring, analytics and observability, and CI quality gates.

## Context

This repository is intentionally **SDK-first**, not app-first.  
Primary objective: define an Android streaming foundation with clear module boundaries, stable integration contracts, and operational guardrails for production delivery.

## Scope

Capabilities in scope:
- Live and VOD streaming with Media3/ExoPlayer (`HLS`, `DASH .mpd`)
- Reusable SDK API boundary (`StreamingClient`, `StreamConfig`) for host-app abstraction
- Widevine-oriented DRM bootstrap (`DrmConfig`) with license endpoint + request-header model
- Analytics contract (`AnalyticsTracker`) with privacy-first defaults (no PII in default analytics)
- Performance and reliability controls (ABR hints, diagnostics, playback-thread discipline, CI gating)

## Non-Goals

- Not a production content service (no CDN ownership, no rights management operations, no license server operations).
- Not a backend analytics platform (exports analytics contracts only; sink ownership remains with integrators).
- Not a single-module app architecture; host UI exists to exercise SDK integration paths.
- Not a replacement for organization-specific secret management or compliance workflows.

## Architecture

1. **Public SDK contract**  
   `SDK_DEMO_ANDROID/streamkit-sdk-core/src/main/java/com/monigarr/streamkit/core/StreamingClient.kt`  
   Defines SDK lifecycle contract, threading constraints, diagnostics and extension hooks.

2. **Playback implementation**  
   `SDK_DEMO_ANDROID/streamkit-sdk-core/src/main/java/com/monigarr/streamkit/core/player/StreamingClientImpl.kt`  
   Implements Media3 orchestration, lifecycle transitions, and host-integration behavior.

3. **DRM model**  
   `SDK_DEMO_ANDROID/streamkit-sdk-core/src/main/java/com/monigarr/streamkit/core/DrmConfig.kt`  
   Defines DRM handshake input shape: license endpoint plus auth/token headers.

4. **Analytics model**  
   `SDK_DEMO_ANDROID/streamkit-sdk-core/src/main/java/com/monigarr/streamkit/core/AnalyticsTracker.kt`  
   Analytics boundary for play/buffer/error/session events with privacy-first defaults.

5. **Host integration surface**  
   `SDK_DEMO_ANDROID/app/src/main/java/com/monigarr/streamkit/demo/`  
   Demonstrates host-module wiring and integration pattern.

Built as a modular Android video streaming SDK + host module:
- `streamkit-sdk-core`: reusable integration layer
- `app`: reference host for implementation and onboarding flow

Separation is deliberate: streaming capability evolves in the SDK, while the app remains an integration harness and acceptance surface.

Domain alignment (live sports):
- Live playback orientation with low-latency readiness and ABR governance
- Analytics hooks aligned with production observability pipelines
- DRM-ready path for protected premium content
- Design choices aligned with bursty, event-driven traffic and strict playback SLAs

## Verification

From `SDK_DEMO_ANDROID`:

```bash
./gradlew :streamkit-sdk-core:apiCheck :streamkit-sdk-core:testDebugUnitTest :app:testDebugUnitTest
```

On Windows PowerShell:

```powershell
.\gradlew.bat :streamkit-sdk-core:apiCheck :streamkit-sdk-core:testDebugUnitTest :app:testDebugUnitTest
```

Then run the `app` target in Android Studio to validate end-to-end playback and host integration.

Reference documentation:

- Product scope and outcomes: `docs/PRD.md`
- Technical architecture: `docs/ARCHITECTURE.md`
- Program handoff and onboarding: `docs/PROGRAM_HANDBOOK.md`
- Delivery history: `CHANGELOG.md`

## Risks

- Integration risk if host teams bypass SDK lifecycle/threading contracts in `StreamingClient`.
- DRM deployment risk if license endpoint/auth configuration in `DrmConfig` is not environment-managed.
- Analytics and observability risk if `AnalyticsTracker` is not wired to downstream monitoring/alerting systems.
- Performance risk on constrained networks/devices without ABR tuning and diagnostic review in production.
- Governance risk if API checks, tests, and documentation alignment are not enforced in CI.
