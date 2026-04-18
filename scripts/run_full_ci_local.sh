#!/usr/bin/env bash
# File: run_full_ci_local.sh
# Description: Run the same gates as `.github/workflows/fluvian-sdk-ci.yml` (Python + Gradle + JaCoCo) from bash.
# Author: monigarr@monigarr.com
# Date: 2026-04-18
# Version: 1.3.6
#
# Usage:
#   bash scripts/run_full_ci_local.sh
#
# Usage example:
#   cd "$(git rev-parse --show-toplevel)" && bash scripts/run_full_ci_local.sh

set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 scripts/check_echelon_headers.py
python3 scripts/validate_open_core_layout.py

pushd "$ROOT/SDK_DEMO_ANDROID" >/dev/null
chmod +x gradlew
./gradlew :fluvian-sdk-core:apiCheck --no-daemon --stacktrace
./gradlew \
  :fluvian-sdk-core:testDebugUnitTest \
  :app:testDebugUnitTest \
  :app:assembleDebug \
  :fluvian-sdk-core:jacocoFluvianSdkCoreDebug \
  :app:jacocoAppDebug \
  --no-daemon --stacktrace
popd >/dev/null

python3 scripts/check_jacoco_coverage.py \
  "$ROOT/SDK_DEMO_ANDROID/fluvian-sdk-core/build/reports/jacoco/jacocoFluvianSdkCoreDebug/jacoco.xml" \
  0.15

python3 scripts/check_jacoco_coverage.py \
  "$ROOT/SDK_DEMO_ANDROID/app/build/reports/jacoco/jacocoAppDebug/jacoco.xml" \
  0.012

echo "Local CI parity run completed successfully."
