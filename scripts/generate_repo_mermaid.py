"""
File: generate_repo_mermaid.py
Description: Emit a Mermaid mindmap of repository files (excluding build/.gradle/.git/.cxx caches).
Author: monigarr@monigarr.com
Date: 2026-04-15
Version: 1.3.6

Usage:
  python scripts/generate_repo_mermaid.py > docs/REPOSITORY_FILES_MERMAID.md

Usage example:
  python scripts/generate_repo_mermaid.py
"""

from __future__ import annotations

import os
import sys
from pathlib import Path

SKIP_FILE_NAMES = frozenset({"local.properties"})


SKIP_DIR_NAMES = frozenset(
    {
        "build",
        ".gradle",
        ".git",
        ".cxx",
        ".kotlin",
        "intermediates",
        "incremental",
        "kotlin",
        "outputs",
        "tmp",
        "zip-cache",
        "apk",
        "dexBuilderDebug",
        "desugar_graph",
        "node_modules",
    }
)


def should_skip_path(path: Path, root: Path) -> bool:
    rel = path.relative_to(root)
    parts = set(rel.parts)
    return bool(parts & SKIP_DIR_NAMES)


def collect_files(root: Path) -> list[str]:
    out: list[str] = []
    for dirpath, dirnames, filenames in os.walk(root):
        p = Path(dirpath)
        if should_skip_path(p, root):
            dirnames[:] = []
            continue
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIR_NAMES and d != ".git"]
        for name in filenames:
            if name in SKIP_FILE_NAMES:
                continue
            fp = p / name
            if should_skip_path(fp, root):
                continue
            rel = fp.relative_to(root).as_posix()
            out.append(rel)
    return sorted(out)


def escape_mermaid_label(s: str) -> str:
    return s.replace("(", " ").replace(")", " ").replace('"', "'")


def build_tree(files: list[str]) -> dict:
    tree: dict = {}
    for f in files:
        parts = f.split("/")
        d = tree
        for i, part in enumerate(parts):
            if i == len(parts) - 1:
                d.setdefault("__files__", []).append(part)
            else:
                d = d.setdefault(part, {})
    return tree


def emit_mindmap(tree: dict, indent: int = 2) -> list[str]:
    lines: list[str] = []
    pad = " " * indent
    for key in sorted(k for k in tree if k != "__files__"):
        lines.append(f"{pad}{escape_mermaid_label(key)}")
        sub = tree[key]
        if isinstance(sub, dict):
            lines.extend(emit_mindmap(sub, indent + 2))
    for fn in sorted(tree.get("__files__", [])):
        lines.append(f"{pad}{escape_mermaid_label(fn)}")
    return lines


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    files = collect_files(root)
    tree = build_tree(files)
    body = "\n".join(emit_mindmap(tree))
    md = f"""# Repository file tree (Mermaid)

This diagram lists **source and project files** under the repository root. **Excluded:** `build/`, `.gradle/`, `.git/`, `.cxx/`, `.kotlin/` (IDE/compiler session data), common Gradle cache/intermediates (local build outputs), and `local.properties` (machine-local SDK path). A full workspace scan including build caches is much larger (often hundreds of files).

**File count (listed):** {len(files)}

```mermaid
mindmap
  root((Fluvian SDK))
{body}
```

## Regenerate

```text
python scripts/generate_repo_mermaid.py > docs/REPOSITORY_FILES_MERMAID.md
```

Or run without redirect and copy the `mermaid` block.
"""
    sys.stdout.write(md)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
