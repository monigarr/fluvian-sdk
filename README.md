# LVSPOC StreamKit

**LVSPOC StreamKit** is an enterprise-grade Android streaming SDK (Kotlin) built on Media3: live and on-demand playback, DRM-ready design, analytics, and an AI-oriented optimization layer—aimed at live sports and other high-performance video products.

| Field | Value |
|-------|--------|
| **Program / README version** | 1.3.4 |
| **Date** | 2026-04-15 |
| **Author** | monigarr@monigarr.com |

---

## Usage notice

This repository is provided for **evaluation and demonstration**. All materials are proprietary to **MoniGarr** and may not be reused, copied, or distributed without permission. For enterprise modules or integration support, contact **monigarr@monigarr.com**.

---

## Why this exists

Modern streaming stacks need more than a player:

- Predictable performance on constrained devices  
- Observability and analytics without default PII  
- Room for intelligent adaptation (telemetry + policy, AI paths where appropriate)  
- A reusable SDK boundary so product teams ship faster  

This repo shows how those concerns are structured end to end in one reference program.

---

## Open core model

This repository is the **open core** slice of the StreamKit platform. **Included here:** SDK architecture, playback foundation, DRM-ready structure, analytics contracts, and AI abstraction hooks.

**Not in this repo** (commercial / enterprise layers): AI-driven optimization as shipped to paying customers, deep performance tuning packs, and full production DRM operational playbooks. Use the contact above for access to those layers.

---

## New team: start here

- **Sponsors and executives** who will not browse the tree: **[docs/EXECUTIVE_BRIEF.md](docs/EXECUTIVE_BRIEF.md)** — one page, PDF-friendly.  
- **Anyone taking over** the program (engineering, product, security, leadership): **[docs/PROGRAM_HANDBOOK.md](docs/PROGRAM_HANDBOOK.md)** — role-based reading order, first-week checklist, glossary, CI notes, then pointers into the PRD and architecture.

---

## Documentation map

| Document | Audience | Purpose |
|----------|-----------|---------|
| **[docs/EXECUTIVE_BRIEF.md](docs/EXECUTIVE_BRIEF.md)** | Sponsors, board, procurement | Scope, metrics, governance; export to PDF for packets |
| **[docs/PROGRAM_HANDBOOK.md](docs/PROGRAM_HANDBOOK.md)** | Transition / onboarding | Handoff spine, repo map, checklists, glossary, CI troubleshooting |
| [docs/PRD.md](docs/PRD.md) | Product, sponsors, engineering | Scope, NFRs, success metrics, enterprise pillars |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Engineering, security, platform | APIs, ExoPlayer/Media3, DRM, AI, distribution, testing, file-header standard |
| **README.md** (this file) | Everyone | Summary, version matrix, layout, how to run the demo |

### Version alignment

`PRD.md`, `ARCHITECTURE.md`, and this README share the **same program version** for each named documentation release (**1.3.4** today). Runtime parity:

- **`EchelonProgramInfo.DOCUMENT_VERSION`** in `streamkit-sdk-core` mirrors that value.  
- The demo app **`versionName`** (shown on the SDK features badge via `BuildConfig.VERSION_NAME`) **must** match; `:app:testDebugUnitTest` fails on mismatch so CI and the in-app label cannot drift from docs.  

The **program handbook** uses the same aligned version for each drop but is not wired into those runtime checks. SemVer history lives in [CHANGELOG.md](CHANGELOG.md).

---

## Executive summary

StreamKit combines **modular SDK design**, **Widevine-oriented DRM**, **analytics and observability**, and an **AI-first** optimization story (MoniGarr operating model and **M.I.L.E.**: Measure, Instrument, Learn, Execute). The program targets **enterprise procurement**: stable APIs, meaningful tests, explicit privacy and security posture, and **mandatory Echelon file headers** on new or materially revised sources and scripts ([architecture §25](docs/ARCHITECTURE.md#25-echelon-enterprise-source-and-script-header-standard)).

---

## Repository layout

| Path | Description |
|------|-------------|
| `docs/EXECUTIVE_BRIEF.md` | Sponsor one-pager (markdown; export to PDF as needed) |
| `docs/PROGRAM_HANDBOOK.md` | Program takeover and onboarding spine |
| `docs/adr/` | Architecture Decision Records (start from `0001-template.md`) |
| `docs/PRD.md` | Product requirements |
| `docs/ARCHITECTURE.md` | Technical specification and enterprise standards |
| `SDK_DEMO_ANDROID/` | Android Studio project: demo app + `streamkit-sdk-core` |
| `scripts/` | Echelon header check and JaCoCo coverage gates (local + CI) |
| `.github/workflows/` | GitHub Actions |
| `CHANGELOG.md` | SemVer history |
| `LICENSE.txt` | License terms |

---

## Objectives (aligned with PRD)

- Ship a **production-ready** streaming SDK for Android.  
- Demonstrate **HLS** (live + DVR), **DASH** (`.mpd`) lab fixtures where applicable, adaptive bitrate, and low-latency readiness.  
- Keep a **DRM-ready** architecture (Widevine, token-aware license flows).  
- Ship **analytics + observability** with privacy-first defaults.  
- Enable **fast integration** without sacrificing **SemVer**, **tests**, or **documentation sync**.

---

## Enterprise pillars (summary)

| Pillar | Commitment |
|--------|------------|
| **Privacy** | Minimized data; no PII in default analytics; governance for AI and frame hooks |
| **Security** | HTTPS; secrets out of source; encrypted key storage; careful logging |
| **Usability** | Clear lifecycle APIs; demo parity with documented flows |
| **Accessibility** | WCAG-oriented UI patterns on product surfaces |
| **Maintenance** | SemVer, changelog, ADRs; new or revised units use the **Enterprise header** (author `monigarr@monigarr.com`, date, version, usage, example) |
| **Testing** | Unit, integration, instrumented, security, and accessibility expectations — [architecture §26](docs/ARCHITECTURE.md#26-enterprise-testing-strategy-and-coverage) |

---

## Features (product summary)

| Area | Capabilities |
|------|----------------|
| **Live streaming** | HLS (live + DVR), DASH (`.mpd`), ABR, low-latency readiness |
| **SDK** | Modular API; lifecycle-safe player abstraction |
| **DRM** | Widevine-oriented design; token support for licenses |
| **Analytics** | Lifecycle, buffering, errors; privacy-minimized sessions |
| **Sports UX** | Live indicator, event markers, latency display |
| **Advanced (optional)** | Extended rendering / 3D; on-device GenAI (e.g. ML Kit / Gemini Nano where AICore exists) and cloud paths — [architecture §12–§15](docs/ARCHITECTURE.md#12-ai-powered-streaming-optimization-layer-mile-extension), [§21–§23](docs/ARCHITECTURE.md#21-advanced-streaming-profiling-and-rendering) |

---

## Success metrics (targets)

| Metric | Target |
|--------|--------|
| Playback start | Under 2 seconds |
| Buffer ratio | Under 2% |
| Crash-free sessions | Above 99.9% |
| Typical SDK integration | Under one day |

---

## Getting started (reference project)

1. Clone the repository.  
2. Open **`SDK_DEMO_ANDROID/`** in **Android Studio** (current stable channel).  
3. Sync Gradle and run the **app** configuration on a device or emulator.  
4. Use the in-app **stream picker** (`MainActivity`): curated **public HTTPS** samples — **Big Buck Bunny** (Mux), **Apple Advanced HDR** (fMP4), **Tears of Steel** (Unified Streaming), **Unified Live (SCTE35)**. These are for demos and manual QA only, not a content SLA. The SDK also accepts **DASH** when the manifest URL ends in `.mpd`; add your own lab URL to the picker if you need `.mpd` in the reference UI.  
5. Read **`streamkit-sdk-core`** sources and tests as the baseline described in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

On **Windows**, from `SDK_DEMO_ANDROID` use `.\gradlew.bat` in place of `./gradlew` in shell examples and in [CHANGELOG.md](CHANGELOG.md).

---

## Example integration (conceptual)

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). The Media3-backed implementation is **`StreamingClientImpl`** in `com.monigarr.streamkit.core.player`.

```kotlin
import com.monigarr.streamkit.core.StreamConfig
import com.monigarr.streamkit.core.StreamingClient
import com.monigarr.streamkit.core.player.StreamingClientImpl

val client: StreamingClient = StreamingClientImpl(
    context = context,
    analytics = myAnalyticsTracker,
    drmConfig = optionalWidevineConfig,
)
client.initialize(StreamConfig(enableBandwidthPredictorHints = true)) {
    client.play("https://example.com/live/playlist.m3u8")
}
// pause(), stop(), release() as required; bind a Surface via client.bindVideoSurface(...)
```

---

## Contributing and change control

- Follow **style, headers, and testing** in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).  
- For API or behavior changes: update **PRD** when user-visible or compliance-related, **ARCHITECTURE**, and this **README** in one coherent version bump.  
- Append [CHANGELOG.md](CHANGELOG.md) with SemVer notes and migrations.

---

## Enterprise CI (headers + coverage + API check)

| Artifact | Role |
|----------|------|
| [scripts/check_echelon_headers.py](scripts/check_echelon_headers.py) | Validates Echelon headers ([architecture §25](docs/ARCHITECTURE.md#25-echelon-enterprise-source-and-script-header-standard)) |
| [scripts/check_jacoco_coverage.py](scripts/check_jacoco_coverage.py) | Enforces minimum **LINE** coverage from JaCoCo XML ([architecture §26](docs/ARCHITECTURE.md#26-enterprise-testing-strategy-and-coverage)) |
| [.github/workflows/echelon-ci.yml](.github/workflows/echelon-ci.yml) | Headers → Android **NDK** (CMake) → `./gradlew :streamkit-sdk-core:apiCheck :streamkit-sdk-core:testDebugUnitTest :app:testDebugUnitTest :streamkit-sdk-core:jacocoStreamkitCoreDebug :app:jacocoAppDebug` → **core ≥ 10%** LINE, **app ≥ 1%** LINE |

**Local parity with CI** (from repo root, after a successful Gradle run):

```text
python scripts/check_echelon_headers.py
```

```text
cd SDK_DEMO_ANDROID
./gradlew :streamkit-sdk-core:apiCheck :streamkit-sdk-core:testDebugUnitTest :app:testDebugUnitTest :streamkit-sdk-core:jacocoStreamkitCoreDebug :app:jacocoAppDebug
cd ..
python scripts/check_jacoco_coverage.py SDK_DEMO_ANDROID/streamkit-sdk-core/build/reports/jacoco/jacocoStreamkitCoreDebug/jacoco.xml 0.10
python scripts/check_jacoco_coverage.py SDK_DEMO_ANDROID/app/build/reports/jacoco/jacocoAppDebug/jacoco.xml 0.01
```

---

## License

See [LICENSE.txt](LICENSE.txt).

---

## Advisory and engineering services

MoniGarr works with teams on high-performance mobile and streaming systems: Android SDKs, playback optimization, AI-native integration, and scale-up architecture. **monigarr@monigarr.com**
