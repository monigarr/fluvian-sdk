# Evaluation Terms – Fluvian SDK

This repository is provided for **evaluation and demonstration purposes only** under the terms in [LICENSE](../LICENSE). Commercial licensing, PRO SDK access, and enterprise integration support are available upon request.

---

## 1. What you can do with this public repo

- Clone, build, and run the demo app.
- Inspect the source code of the **Open Core** module (`fluvian-sdk-core`).
- Use the SDK in your own internal prototypes and evaluations.
- File issues for bugs or documentation gaps (but not for production support).

---

## 2. What you cannot do without a license

- Use the SDK in a production application (internal or external).
- Redistribute, sublicense, or resell the SDK (or any modified version) as part of a product or service.
- Remove or alter proprietary notices, including the Echelon file headers.
- Bypass the evaluation scope to access PRO SDK features (e.g., AI optimization, private AARs).

---

## 3. Security and secrets

**No real credentials, API keys, or DRM tokens are stored in this repository.**

- Pull requests and issues **must** use placeholders only – never real Widevine/PlayReady tokens, customer `DrmConfig` URLs, private HLS/DASH origins, or cloud inference keys.
- Load all secrets at runtime from your own secure vault or backend.

---

## 4. Demo content

- In‑app and README sample URLs point to **public third‑party HTTPS fixtures** for manual QA.
- These streams are **not** covered by any content SLA.
- They are **not licensed** for your own product by default.
- The URLs may change or disappear without notice.

---

## 5. Open Core vs. commercial layers

- The public tree documents the architecture and stable APIs.
- Production DRM playbooks, vendor‑specific proxy formats, CAO integrations, and authenticated cloud optimization paths ship as **commercial / private layers** under NDA where applicable.

---

## 6. Content and compliance

- Cloning this repository **does not** grant rights to underlying video, sports data, partner brands, or operational keys.
- Integrators remain responsible for their own content agreements, DRM contracts, and regional compliance (e.g., GDPR, CCPA, COPPA).

---

## 7. Support and contact

- Evaluation‑related questions: open a GitHub issue.
- Licensing, PRO access, enterprise services: **monigarr@monigarr.com**

---

*These evaluation terms supplement but do not replace the full license in [LICENSE](../LICENSE). In case of conflict, the license controls.*