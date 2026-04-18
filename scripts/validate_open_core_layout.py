#!/usr/bin/env python3
# File: validate_open_core_layout.py
# Description: Ensures required Open Core packages, native bridge, Gradle wiring, and binary API baseline exist.
# Author: monigarr@monigarr.com
# Date: 2026-04-18
# Version: 1.3.6
#
# Usage:
#   python3 scripts/validate_open_core_layout.py
#
# Usage example:
#   python3 scripts/validate_open_core_layout.py

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CORE = ROOT / "SDK_DEMO_ANDROID" / "fluvian-sdk-core" / "src" / "main" / "java" / "com" / "fluvian" / "sdk" / "core"
MODULE_CORE = ROOT / "SDK_DEMO_ANDROID" / "fluvian-sdk-core"
SETTINGS = ROOT / "SDK_DEMO_ANDROID" / "settings.gradle.kts"
APP_BUILD = ROOT / "SDK_DEMO_ANDROID" / "app" / "build.gradle.kts"
API_DUMP = MODULE_CORE / "api" / "fluvian-sdk-core.api"

REQUIRED_FILES = [
    MODULE_CORE / "build.gradle.kts",
    MODULE_CORE / "src" / "main" / "AndroidManifest.xml",
    MODULE_CORE / "src" / "main" / "cpp" / "CMakeLists.txt",
    MODULE_CORE / "src" / "main" / "cpp" / "core.cpp",
    CORE / "abr" / "BandwidthPredictor.kt",
    CORE / "abr" / "AbrHint.kt",
    CORE / "aicore" / "AIManager.kt",
    CORE / "aicore" / "AILayerInference.kt",
    CORE / "aicore" / "AIConfig.kt",
    CORE / "performance" / "StreamOrchestrator.kt",
    CORE / "qos" / "PlaybackQoSSignals.kt",
    CORE / "branding" / "SdkBrandBundle.kt",
    CORE / "StreamingClient.kt",
    CORE / "NativeLib.kt",
    CORE / "player" / "StreamingClientImpl.kt",
    CORE / "player" / "MediaSourceFactory.kt",
    CORE / "player" / "ExoPlayerProvider.kt",
    CORE / "network" / "NetworkHealthMonitor.kt",
    CORE / "security" / "FluvianSecretStore.kt",
]


def _validate_settings_includes_open_core() -> list[str]:
    if not SETTINGS.is_file():
        return [str(SETTINGS.relative_to(ROOT))]
    text = SETTINGS.read_text(encoding="utf-8", errors="replace")
    errs: list[str] = []
    if 'include(":fluvian-sdk-core")' not in text:
        errs.append(
            f"{SETTINGS.relative_to(ROOT)} must include Open Core module "
            '(expected literal include(":fluvian-sdk-core"))'
        )
    if 'include(":fluvian-sdk-pro-genai")' in text:
        errs.append(
            f"{SETTINGS.relative_to(ROOT)} must not include missing proprietary module "
            'include(":fluvian-sdk-pro-genai") — Open Core clones must build standalone.'
        )
    return errs


def _validate_app_build_has_no_pro_genai_dependency() -> list[str]:
    if not APP_BUILD.is_file():
        return [str(APP_BUILD.relative_to(ROOT))]
    text = APP_BUILD.read_text(encoding="utf-8", errors="replace")
    errs: list[str] = []
    if "fluvian-sdk-pro-genai" in text:
        errs.append(
            f"{APP_BUILD.relative_to(ROOT)} must not reference :fluvian-sdk-pro-genai "
            "(ship PRO as a private AAR under contract, not as a missing includedBuild path)."
        )
    return errs


def _validate_api_dump_baseline() -> list[str]:
    errs: list[str] = []
    if not API_DUMP.is_file():
        errs.append(f"missing committed binary API dump: {API_DUMP.relative_to(ROOT)}")
        return errs
    raw = API_DUMP.read_text(encoding="utf-8", errors="replace")
    lines = [ln for ln in raw.splitlines() if ln.strip()]
    if len(lines) < 40:
        errs.append(
            f"{API_DUMP.relative_to(ROOT)} looks empty or truncated "
            f"({len(lines)} non-blank lines; need >= 40)"
        )
    markers = (
        "com/fluvian/sdk/core/StreamingClient",
        "com/fluvian/sdk/core/NativeLib",
    )
    for m in markers:
        if m not in raw:
            errs.append(f"{API_DUMP.relative_to(ROOT)} missing expected public API marker: {m}")
    return errs


def main() -> int:
    missing = [str(p.relative_to(ROOT)) for p in REQUIRED_FILES if not p.is_file()]
    missing.extend(_validate_settings_includes_open_core())
    missing.extend(_validate_app_build_has_no_pro_genai_dependency())
    missing.extend(_validate_api_dump_baseline())
    if missing:
        print("Open Core layout validation failed:", file=sys.stderr)
        for m in missing:
            print(f"  - {m}", file=sys.stderr)
        return 1
    print("Open Core layout validation OK.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
