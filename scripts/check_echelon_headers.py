#!/usr/bin/env python3
# File: check_echelon_headers.py
# Description: CI gate for Echelon-format headers on Kotlin, Gradle, and Python sources (ARCHITECTURE §25).
# Author: monigarr@monigarr.com
# Date: 2026-04-18
# Version: 1.3.6
#
# Usage:
#   python3 scripts/check_echelon_headers.py
#
# Usage example:
#   python3 scripts/check_echelon_headers.py

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SKIP_DIR_NAMES = {
    "build",
    ".gradle",
    ".git",
    "intermediates",
    "generated",
    "jacoco",
    "outputs",
    "tmp",
    "kotlin",
    "externalNativeBuild",
    ".cxx",
}

EXTENSIONS = {".kt", ".kts", ".py"}


def is_skipped(path: Path) -> bool:
    parts = set(path.parts)
    if parts & SKIP_DIR_NAMES:
        return True
    if "build" in path.parts:
        return True
    return False


def iter_sources() -> list[Path]:
    roots = [
        ROOT / "SDK_DEMO_ANDROID",
        ROOT / "scripts",
    ]
    out: list[Path] = []
    for base in roots:
        if not base.exists():
            continue
        for p in base.rglob("*"):
            if not p.is_file():
                continue
            if p.suffix.lower() not in EXTENSIONS:
                continue
            if is_skipped(p):
                continue
            out.append(p)
    return sorted(out)


HEADER_PATTERN = re.compile(
    r"^\s*/\*\*\s*$.*?^\s*\*\s*File:\s*\S+.*?"
    r"^\s*\*\s*Author:\s*\S+.*?"
    r"^\s*\*\s*Date:\s*\S+.*?"
    r"^\s*\*\s*Version:\s*\S+",
    re.MULTILINE | re.DOTALL,
)

def _python_has_echelon_header(text: str) -> bool:
    head = "\n".join(text.splitlines()[:48])
    return (
        "File:" in head
        and "Author:" in head
        and "Date:" in head
        and "Version:" in head
    )


def has_valid_header(text: str, suffix: str) -> bool:
    if suffix == ".py":
        return _python_has_echelon_header(text)
    return bool(HEADER_PATTERN.search(text))


def main() -> int:
    failures: list[str] = []
    for path in iter_sources():
        raw = path.read_text(encoding="utf-8", errors="replace")
        if not has_valid_header(raw, path.suffix.lower()):
            failures.append(str(path.relative_to(ROOT)))
    if failures:
        print("Echelon header check failed for:", file=sys.stderr)
        for f in failures:
            print(f"  - {f}", file=sys.stderr)
        return 1
    print(f"Echelon header check OK ({len(iter_sources())} files).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
