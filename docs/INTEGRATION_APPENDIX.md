# Fluvian SDK — Integration Appendix

| Field | Value |
|-------|--------|
| **Document version** | 1.3.6 |
| **Date** | 2026-04-18 |
| **Author** | monigarr@monigarr.com |

This appendix is the **integration one-pager** referenced from [README.md](../README.md). Use the README for **product positioning**, **hybrid model (Open Core · PRO SDK · Enterprise Services)**, **pricing**, and **evaluation / IP scope**; use [ARCHITECTURE.md](internal/ARCHITECTURE.md) for APIs, QoS, AI, DRM, testing, and distribution detail.

---

## Hybrid model (integration view)

| Tier | What you wire in Gradle | What you get |
|------|-------------------------|--------------|
| **Open Core** | `project(":fluvian-sdk-core")` or published `com.fluvian.sdk:fluvian-sdk-core` | Public module: `StreamingClient`, Media3 playback, DRM-ready config (no secrets in source), rule-based QoS (Measure → Interpret → Execute), AI abstraction / facades (no live models or weights in git) |
| **PRO SDK** | Licensed **private AAR** from your Maven repo (credentials never in git) | AI-assisted QoS optimization, bandwidth prediction and advanced tuning paths, priority delivery—per your license |
| **Enterprise Services** | Statements of work + optional private repos | Integration support, white-label programs, custom architecture, performance engagements, SLA-backed delivery |

**Pricing and packaging** are summarized in the README (**Pricing**); final terms are confirmed in writing.

---

## Architecture boundary (integration)

**Open Core (public repo):** `StreamingClient` API, ExoPlayer (Media3) integration, DRM configuration patterns (secrets at runtime only), QoS framework (M → I → E), AI abstraction layer.

**Private / licensed layers:** AI optimization engine, QoS prediction models, performance tuning systems, production DRM workflows—delivered under contract, not implied by a public clone alone.

---

## Gradle coordinates (Open Core)

Published artifacts use group **`com.fluvian.sdk`** (see `SDK_DEMO_ANDROID/fluvian-sdk-core/build.gradle.kts`; `publishing.gradle.kts` documents coordinates and private-repo properties).

**Local development (monorepo):**

```kotlin
dependencies {
    implementation(project(":fluvian-sdk-core"))
}
```

**Published coordinate (example version):**

```text
com.fluvian.sdk:fluvian-sdk-core:1.3.6
```

**Private Maven (PRO SDK):** After MoniGarr provisions a repository URL and credentials, add a `maven { url = …; credentials { … } }` block and depend on the licensed artifact coordinates supplied with your agreement. Use `fluvian.maven.releaseUrl`, `fluvian.maven.username`, and `fluvian.maven.password` from `~/.gradle/gradle.properties` or CI secrets—**never** commit passwords.

---

## Minimum requirements

- **JDK:** 17 (CI) / Android Studio embedded JDK  
- **Android Gradle Plugin & compileSdk:** As declared in `SDK_DEMO_ANDROID/gradle/libs.versions.toml` and root build files  
- **NDK:** Required only when modifying `fluvian-sdk-core/src/main/cpp/`

---

## White-label branding

Pass `SdkBrandBundle` through `StreamConfig.sdkBrand` for product display name, support URL, optional colors, and `analyticsTenantKey` (non-secret routing label). Demo reference: `SDK_DEMO_ANDROID/app/src/main/java/com/fluvian/sdk/demo/DemoWhiteLabel.kt`.

---

## Client configuration injection

Use `StreamConfig.clientMetadata` for **non-secret** key/value pairs (feature flags, SKU codes, deployment lane). Values are **not** forwarded into `AnalyticsStreamConfigSummary`; only `clientMetadataEntryCount` is emitted for coarse telemetry.

---

## AI provider injection

Implement `AIProviderResolver` and assign it to `StreamConfig.aiProviderResolver`. Return `null` to keep default `AIProviderFactory` routing. Open Core stays CI-safe; licensed **PRO SDK** paths extend optimization and model-backed behavior per contract.

---

## API contract and upgrades

The public API surface is tracked in `fluvian-sdk-core/api/fluvian-sdk-core.api`. Run `./gradlew :fluvian-sdk-core:apiCheck` before releases when changing published APIs (see README / CI for parity).

---

## PRO SDK artifacts (licensed)

**PRO SDK** artifacts (e.g. private AARs, protected repositories) ship **outside** this public tree: GenAI / ML paths, extended parsers, fleet **Learn**-class capabilities, and other licensed layers as agreed.

- Integrate only artifacts and repository URLs issued under your license.  
- Do not expect a silent `include` for PRO-only modules in the public clone—Open Core builds **without private dependencies** for evaluation.  
- Coordinate versioning and migration notes with **monigarr@monigarr.com** when upgrading across SemVer bumps.

---

## Enterprise Services (commercial)

For white-label SDK programs, custom streaming architecture, performance optimization engagements, SLA-backed support, and NDA runbooks, scope is delivered via **Enterprise Services** (SOW / procurement path), not the public Gradle graph alone.

---

## Support and next steps

**Contact:** monigarr@monigarr.com  

**Typical requests:** PRO SDK access and private Maven setup, enterprise pricing, technical consultation, NDAs, and quotes aligned to your deployment scope (see README **Work With Fluvian SDK** and **Evaluation scope, security, and intellectual property**).
