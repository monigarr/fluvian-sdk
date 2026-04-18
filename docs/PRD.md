# Fluvian SDK — Product Requirements (Open Core)

| Field | Value |
|-------|--------|
| **Document version** | 1.3.6 |
| **Date** | 2026-04-18 |
| **Author** | monigarr@monigarr.com |

## 1. Vision

Fluvian SDK is an **AI-ready**, **DRM-aware**, **analytics-friendly** Android playback platform for live and on-demand streaming. The **Open Core** proves architecture, APIs, and safety defaults; **PRO** and **ENTERPRISE** tiers monetize intelligence, integrations, and SLAs.

---

## 2. Goals

1. **Clone-to-build:** A new engineer must compile `:fluvian-sdk-core` and `:app` without private artifacts.
2. **Stable surface:** Public API is checked against `api/fluvian-sdk-core.api`.
3. **Observable playback:** Hosts receive explicit analytics callbacks without PII in defaults.
4. **M.I.L.E. ready:** QoS and AI pipelines expose Measure / Interpret / Learn / Execute hooks even when **Learn** is a no-op in Open Core.

---

## 3. Non-goals (Open Core)

- Operating a production DRM license service.
- Shipping model weights, remote LLM keys, or fleet-tuned policies in public Git.
- Guaranteeing third-party demo stream SLAs.

---

## 4. Personas

| Persona | Need |
|---------|------|
| **Mobile lead** | Thread-safe player, documented extension points |
| **Video platform architect** | QoS contracts, ABR hooks, live latency controls |
| **Security reviewer** | Clear tiering, no secret leakage via `.gitignore` tricks |
| **Procurement** | Open Core vs PRO vs Enterprise narrative (`README.md`) |

---

## 5. Real-world use cases

1. **Live sports preview:** Low-latency HLS with simulated network stress in the demo.
2. **Enterprise white-label:** `SdkBrandBundle` + `StreamConfig.clientMetadata` for tenant-safe configuration injection.
3. **AI-assisted operations (licensed):** Same `StreamConfig` hooks route to private `AIProvider` implementations.

---

## 6. Acceptance criteria (Open Core)

| ID | Criterion |
|----|-----------|
| AC-01 | `./gradlew :app:assembleDebug` succeeds from a clean clone on a standard Android CI image. |
| AC-02 | `./gradlew :fluvian-sdk-core:apiCheck` succeeds. |
| AC-03 | QoS unit tests (`RuleBasedQoSDecisionEngineTest`, `BandwidthPredictorEdgeCaseTest`) pass. |
| AC-04 | On-device GenAI instrumented tests document `UNAVAILABLE` readiness in Open Core. |
| AC-05 | Program version integers and strings match `EchelonProgramInfo.DOCUMENT_VERSION` for the release row. |

---

## 7. Success metrics

- **Build:** CI green on default branch workflow.
- **Coverage:** JaCoCo LINE floors for Open Core and app modules (see `ARCHITECTURE.md` §6).
- **Adoption:** Time-to-first-playback in the demo app under five minutes for a prepared workstation.

---

## 8. Monetization alignment

| Tier | Value |
|------|-------|
| **Open Core** | Trust, architecture proof, hiring signal |
| **PRO** | Private AARs: real on-device GenAI, tuned QoS, fleet **Learn** exporters |
| **ENTERPRISE** | SOW-led integrations, SLAs, custom factories |

---

## 9. Revision history

| Version | Note |
|---------|------|
| 1.3.5 | Open Core standalone build; removed dependency on ignored sources; formal QoS signal export; `clientMetadata` injection. |
