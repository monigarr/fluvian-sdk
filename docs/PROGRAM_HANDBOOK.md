# Fluvian SDK — Program Handbook

| Field | Value |
|-------|--------|
| **Program version** | 1.3.6 |
| **Date** | 2026-04-18 |
| **Author** | monigarr@monigarr.com |

## Who you are (pick a lane)

| If you are… | Start here | You can skip (for now) |
|-------------|------------|-------------------------|
| **Executive or sponsor** | [EXECUTIVE_BRIEF.md](internal/EXECUTIVE_BRIEF.md), then repository `README.md` → **Commercial & Enterprise Layers** | Gradle commands until you care about CI spend |
| **Product or program** | [PRD.md](PRD.md) (use cases + acceptance criteria), then **Commercial & Enterprise Layers** in `README.md` | JNI / CMake unless you own platform |
| **Engineer taking over the repo** | `README.md` Getting started + [ARCHITECTURE.md](ARCHITECTURE.md) §2, §8, §9 | Executive brief until you need to explain tiers |
| **Security reviewer** | `README.md` **Evaluation scope, security, and intellectual property**, [ARCHITECTURE.md](ARCHITECTURE.md) §5–§5a, §9.3 | Demo UX polish |

## Day one (same calendar day)

- [ ] Clone the repo; confirm remote matches `README.md` repository layout.
- [ ] Open `SDK_DEMO_ANDROID/` in Android Studio; sync Gradle; run the **app** configuration.
- [ ] Skim **Commercial & Enterprise Layers** in root `README.md` so you know what is evaluation-only vs licensed.
- [ ] Note program version in `README.md` / this handbook / `PRD.md` / `ARCHITECTURE.md` — they should match for a given drop.

## First-week reading order

1. [EXECUTIVE_BRIEF.md](internal/EXECUTIVE_BRIEF.md) — scope and tiers (10-minute brief).
2. [PRD.md](PRD.md) — goals, **real-world use cases**, **acceptance criteria**, monetization path.
3. [ARCHITECTURE.md](ARCHITECTURE.md) — threading, **§8 QoS**, **§9 AI**, security, testing.
4. Repository `README.md` — clone, build, version matrix, **Open core model**, **Commercial & Enterprise Layers**.
5. [INTEGRATION_APPENDIX.md](INTEGRATION_APPENDIX.md) — Maven coordinates, `SdkBrandBundle`, `AIProviderResolver`, analytics hooks, optional private repository properties (pairs with demo `DemoWhiteLabel.kt`).
6. `CHANGELOG.md` — release discipline and migrations.

**Optional deep pass:** `docs/adr/0001-template.md` for the next architectural decision you need to record.

## Repository map

| Path | Role |
|------|------|
| `SDK_DEMO_ANDROID/fluvian-sdk-core` | Public Kotlin library + JNI sample (Open Core) |
| `SDK_DEMO_ANDROID/app` | Reference Compose demo |
| `scripts/` | CI gates (Echelon headers, JaCoCo thresholds, Open Core layout) |
| `.github/workflows/` | GitHub Actions pipeline |

## Build and CI (local)

```text
cd SDK_DEMO_ANDROID
./gradlew :fluvian-sdk-core:apiCheck :fluvian-sdk-core:testDebugUnitTest :app:testDebugUnitTest
```

**JaCoCo (after unit tests):**

```text
./gradlew :fluvian-sdk-core:jacocoFluvianSdkCoreDebug :app:jacocoAppDebug
```

From repo root, enforce **line** minimums (same as CI): `python3 scripts/check_jacoco_coverage.py` on `jacocoFluvianSdkCoreDebug/jacoco.xml` at **0.15** and `jacocoAppDebug/jacoco.xml` at **0.012**.

**Full CI parity (Echelon + Open Core layout + Gradle + JaCoCo gates)** — from repository root:

- **Windows:** `pwsh -File scripts/run_full_ci_local.ps1`
- **macOS / Linux:** `bash scripts/run_full_ci_local.sh`

Python gates only (from repo root):

```text
python3 scripts/check_echelon_headers.py
python3 scripts/validate_open_core_layout.py
```

## Roles

| Role | Focus |
|------|--------|
| **Engineering** | `ARCHITECTURE.md` §2 §8 §9, `fluvian-sdk-core.api`, `StreamingClientImpl`, `SdkLayerBoundaryTest` |
| **Security** | `README.md` usage restrictions, `ARCHITECTURE.md` §5–§5a, secret storage patterns (`FluvianSecretStore`) |
| **Product** | `PRD.md` use cases + acceptance criteria; Open vs PRO vs Enterprise positioning (`README.md`) |
| **Support / solutions** | Demo stream picker behavior, public fixture limitations, escalation to MoniGarr for licensed tiers |

## Handoff checklist (lead leaving / joining)

- [ ] Program version synchronized across `README.md`, `PRD.md`, `ARCHITECTURE.md`, handbook, and `EchelonProgramInfo.DOCUMENT_VERSION`.
- [ ] CI green on default workflow (or documented waiver).
- [ ] No customer URLs, tokens, or keys added to docs or fixtures.
- [ ] Sponsor has PDF or link to `docs/internal/EXECUTIVE_BRIEF.md` and understands **Commercial & Enterprise Layers**.

## Glossary

| Term | Meaning |
|------|---------|
| **Open Core** | Public, compilable slice; rules + stubs only |
| **PRO** | Licensed private AARs / protected repos (ML Kit, fleet **Learn**, tuned QoS) — never required for a public clone to compile |
| **M.I.L.E.** | Measure, Interpret, Learn, Execute engineering loop |
| **Echelon header** | Mandatory source banner (File, Description, Author, Date, Version, Usage) |

## Contact

**monigarr@monigarr.com**
