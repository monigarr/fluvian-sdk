# Local CI – Fluvian SDK

You can run the exact same checks as GitHub Actions on your own machine. This is useful for debugging failures locally before pushing a branch.

---

## Prerequisites

- **Python 3** (for header and coverage scripts)
- **JDK 17** (for Gradle builds)
- **Android SDK** (with NDK, if your workflow uses it)

On **Windows**, you can run the CI inside Android Studio’s embedded JDK + SDK environment, or install them separately.

---

## Quick start (from repository root)

After a successful Gradle build, run the following commands **one by one** to reproduce the CI pipeline.

### 1. Run all checks in one command

The repository includes helper scripts:

| OS | Command |
|----|---------|
| Windows (PowerShell) | `pwsh -File scripts/run_full_ci_local.ps1` |
| macOS / Linux | `bash scripts/run_full_ci_local.sh` |

These scripts execute: header validation → Open Core layout check → API check → unit tests → JaCoCo coverage → assembly.

---

### 2. Run individual steps

#### Validate file headers

```bash
python scripts/check_echelon_headers.py

Enforces the header format described in ARCHITECTURE.md#25.

Run API check + unit tests + coverage

cd SDK_DEMO_ANDROID
./gradlew :fluvian-sdk-core:apiCheck --no-daemon
./gradlew :fluvian-sdk-core:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug :fluvian-sdk-core:jacocoFluvianSdkCoreDebug :app:jacocoAppDebug --no-daemon
cd ..

Check coverage thresholds

python scripts/check_jacoco_coverage.py SDK_DEMO_ANDROID/fluvian-sdk-core/build/reports/jacoco/jacocoFluvianSdkCoreDebug/jacoco.xml 0.15
python scripts/check_jacoco_coverage.py SDK_DEMO_ANDROID/app/build/reports/jacoco/jacocoAppDebug/jacoco.xml 0.012

Current minimum line coverage:

fluvian-sdk-core → 15%

app (demo) → 1.2%

These numbers are enforced in CI and will increase over time.

What CI checks
Check	Script / Gradle task	Purpose
Echelon headers	check_echelon_headers.py	Every Kotlin/Java/script file has enterprise header
Open Core layout	(part of run_full_ci_local)	No private AARs leaked into public tree
API check	:fluvian-sdk-core:apiCheck	Public API stability (no accidental breaking changes)
Unit tests	:fluvian-sdk-core:testDebugUnitTest + :app:testDebugUnitTest	Correctness of core and demo
JaCoCo coverage	jacocoFluvianSdkCoreDebug + jacocoAppDebug	Line coverage minimums
Assemble debug	:app:assembleDebug	Demo app builds without errors
Troubleshooting
check_echelon_headers.py fails → Add or update the header in the offending file. See ARCHITECTURE.md for the exact format.

Coverage below threshold → Write more unit tests, or (rarely) adjust the threshold in CI config with a documented reason.

Gradle daemon issues → Use --no-daemon (as in the examples above) to avoid stale state.

Windows path errors → Make sure you are using \ or forward slashes consistently; the scripts are tested with both.

For deeper CI configuration, see .github/workflows/fluvian-sdk-ci.yml.


---

## 📄 CONTRIBUTING.md

```markdown
# Contributing to Fluvian SDK

Thank you for your interest! Fluvian SDK is an open core project – the public repository is for **evaluation, bug reports, and documentation improvements**. Code contributions are welcome, but must follow the guidelines below.

> **Note:** This public repo does **not** accept changes that would expose PRO SDK features (private AARs, AI model weights, production DRM runbooks). Those remain in licensed repositories.

---

## How to contribute

### 1. Report a bug or request a clarification

Open a GitHub issue with:
- A clear title and description
- Steps to reproduce (if a bug)
- Expected vs. actual behavior
- Environment (Android version, device/emulator, build number)

### 2. Suggest a documentation improvement

Documentation lives in `docs/` and the root `README.md`.  
Open a pull request with your proposed changes.

### 3. Propose a code change (Open Core only)

For changes to `fluvian-sdk-core` or the demo app:

1. **File an issue first** describing what you want to change and why.
2. Wait for maintainer feedback (to avoid wasted work).
3. Fork the repository, create a branch, and make your changes.
4. Follow the **style and header requirements** below.
5. Run **local CI** (`LOCAL_CI.md`) and ensure all checks pass.
6. Open a pull request against the `main` branch.

---

## Code style and headers

- **Kotlin**: Follow [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide).
- **Java** (if any): Use Google Java Format.
- **Every new or materially revised source file** (`.kt`, `.java`, `.gradle`, `.sh`, `.py`) must include the **Echelon enterprise header**:

```kotlin
/*
 * Program: Fluvian SDK
 * File: <filename>
 * Author: monigarr@monigarr.com
 * Date: YYYY-MM-DD
 * Version: <program version from README>
 * Usage: <brief description of what this file does>
 */

 For scripts, use the same format with appropriate comment syntax (# for Python/shell, // for Gradle).

The CI script scripts/check_echelon_headers.py enforces this. See ARCHITECTURE.md for details.

Testing requirements
Any functional change must include unit tests (or integration tests where appropriate).
The CI enforces line coverage minimums (15% for core, 1.2% for app).
If your change adds new logic, you are expected to maintain or improve coverage.

Run tests locally:
cd SDK_DEMO_ANDROID
./gradlew testDebugUnitTest

Pull request process
Keep PRs small – one logical change per PR.

Update documentation if you change public APIs or user‑visible behavior (README, PRD, ARCHITECTURE).

Update CHANGELOG.md with a short entry under the appropriate version (or “Unreleased”).

Ensure CI passes (GitHub Actions will run automatically).

Request a review from @monigarr (or the maintainer).

A maintainer will respond within 5 business days.

What is not accepted
Changes that add PRO SDK features to the public tree.

Code that introduces secrets, API keys, or DRM tokens.

Breaking changes to the public API without a deprecation cycle and major version bump.

Changes that reduce test coverage without a strong justification.

Licensing of contributions
By contributing to this repository, you agree that your contribution is licensed under the same proprietary terms as the project (see [LICENSE](../LICENSE) and [CONTRIBUTING.md](../CONTRIBUTING.md)).
You retain copyright of your contribution, but grant MoniGarr a perpetual, worldwide, royalty‑free license to use, modify, and distribute it as part of Fluvian SDK.

If you need a different arrangement (e.g., for a paid integration), please contact monigarr@monigarr.com before contributing.

Questions?
Open a GitHub issue with the label question, or email monigarr@monigarr.com for non‑public matters.
