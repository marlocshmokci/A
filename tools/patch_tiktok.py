#!/usr/bin/env python3
"""TTAuOI integration guard for the supplied TikTok APK.

The script is deliberately fail-closed. It verifies the package and the known
privacy/settings protocol anchor before any APK rebuild is attempted.
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

EXPECTED_PACKAGE = "com.zhiliaoapp.musically"
VERSION = "v2.0.5"
ANCHORS = (
    "content_section_cell_settings_and_privacy",
    "ProfileNavbarSettingsAndPrivacyProtocol",
    "aweme://privacy/setting",
)

def run(cmd: list[str]) -> None:
    print("$", " ".join(cmd))
    subprocess.run(cmd, check=True)

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("apk", type=Path)
    ap.add_argument("--out", type=Path, default=Path(f"TTAuOI-{VERSION}.apk"))
    ap.add_argument("--inspect-only", action="store_true")
    args = ap.parse_args()

    apk = args.apk.resolve()
    if not apk.is_file():
        print(f"error: APK not found: {apk}", file=sys.stderr)
        return 2

    if shutil.which("apktool") is None:
        print("error: install Apktool 3.0.3 and make it available as 'apktool'", file=sys.stderr)
        return 2

    with tempfile.TemporaryDirectory(prefix="ttauoi-") as tmp:
        decoded = Path(tmp) / "decoded"
        run(["apktool", "d", "-f", str(apk), "-o", str(decoded)])

        manifest = decoded / "AndroidManifest.xml"
        if not manifest.exists():
            print("error: decoded manifest is missing", file=sys.stderr)
            return 3

        manifest_text = manifest.read_text(encoding="utf-8", errors="replace")
        if EXPECTED_PACKAGE not in manifest_text:
            print(f"error: expected package {EXPECTED_PACKAGE} not found", file=sys.stderr)
            return 4

        hits: list[tuple[str, str]] = []
        for root in sorted(decoded.glob("smali*")):
            if not root.is_dir():
                continue
            for f in root.rglob("*.smali"):
                data = f.read_text(encoding="utf-8", errors="replace")
                for anchor in ANCHORS:
                    if anchor in data:
                        hits.append((str(f.relative_to(decoded)), anchor))

        if not hits:
            print("error: no verified TTAuOI integration anchor found; refusing to patch", file=sys.stderr)
            return 5

        print("Verified anchors:")
        for path, anchor in hits[:50]:
            print(f"  {path}: {anchor}")

        if args.inspect_only:
            return 0

        print(
            "error: this TikTok build is detected, but the bytecode adapter is not "
            "verified for this exact build. No modified APK was emitted.",
            file=sys.stderr,
        )
        return 6

if __name__ == "__main__":
    raise SystemExit(main())
