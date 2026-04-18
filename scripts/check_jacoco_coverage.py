#!/usr/bin/env python3
# File: check_jacoco_coverage.py
# Description: Fail CI when JaCoCo LINE coverage ratio is below a configured minimum (Fluvian SDK CI).
# Author: monigarr@monigarr.com
# Date: 2026-04-18
# Version: 1.3.6
#
# Usage:
#   python3 scripts/check_jacoco_coverage.py <path-to-jacoco.xml> <min_ratio>
#
# Usage example:
#   python3 scripts/check_jacoco_coverage.py SDK_DEMO_ANDROID/fluvian-sdk-core/build/reports/jacoco/jacocoFluvianSdkCoreDebug/jacoco.xml 0.15

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def line_coverage_ratio(tree: ET.ElementTree) -> float:
    """Use the report-level LINE counter only (JaCoCo duplicates metrics on nested nodes)."""
    root = tree.getroot()
    for child in root:
        if child.tag == "counter" and child.get("type") == "LINE":
            covered = int(child.get("covered", "0"))
            missed = int(child.get("missed", "0"))
            total = covered + missed
            return (covered / total) if total else 0.0
    return 0.0


def main() -> int:
    if len(sys.argv) != 3:
        print("Usage: check_jacoco_coverage.py <jacoco.xml> <min_ratio>", file=sys.stderr)
        return 2
    path = Path(sys.argv[1])
    minimum = float(sys.argv[2])
    if not path.is_file():
        print(f"Missing JaCoCo XML: {path}", file=sys.stderr)
        return 1
    ratio = line_coverage_ratio(ET.parse(path))
    if ratio + 1e-9 < minimum:
        print(f"LINE coverage {ratio:.4f} < required {minimum:.4f} ({path})", file=sys.stderr)
        return 1
    print(f"LINE coverage OK: {ratio:.4f} >= {minimum:.4f} ({path})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
