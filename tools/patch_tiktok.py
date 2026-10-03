#!/usr/bin/env python3
"""
TTAuOI base-APK integration guard.

This tool intentionally fails closed. It does not claim to produce a real TikTok
mod unless the supplied APK matches the expected package and the known privacy
menu structure. Apktool 3.0.3 must be installed and available as "apktool".
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

EXPECTED_PACKAGE = "com.zhiliaoapp.musically"
VERSION = "v2.0.5"
PRIVACY_ANCHOR = "content_section_cell_settings_and_privacy"


def run(cmd: list[str], cwd: Path | None = None) -> None:
    print("$", " ".join(cmd))
    subprocess.run(cmd, cwd=cwd, check=True)


def package_from_manifest(apk: Path) -> str:
    # Binary AndroidManifest.xml is deliberately not parsed here. The decoded
    # manifest is authoritative and lets Apktool handle resource formats.
    return ""


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("apk", type=Path)
    parser.add_argument("--out", type=Path, default=Path(f"TTAuOI-{VERSION}.apk"))
    args = parser.parse_args()

    apk = args.apk.resolve()
    out = args.out.resolve()

    if not apk.is_file():
        print(f"error: APK not found: {apk}", file=sys.stderr)
        return 2

    if shutil.which("apktool") is None:
        print("error: apktool is required (tested with Apktool 3.0.3)", file=sys.stderr)
        return 2

    with tempfile.TemporaryDirectory(prefix="ttauoi-") as tmp:
        work = Path(tmp)
        decoded = work / "decoded"

        run(["apktool", "d", "-f", str(apk), "-o", str(decoded)])

        manifest = decoded / "AndroidManifest.xml"
        if not manifest.exists():
            print("error: decoded AndroidManifest.xml is missing", file=sys.stderr)
            return 3

        manifest_text = manifest.read_text(encoding="utf-8", errors="replace")
        if EXPECTED_PACKAGE not in manifest_text:
            print(
                f"error: target package is not {EXPECTED_PACKAGE}; refusing to patch",
                file=sys.stderr,
            )
            return 4

        smali_hits: list[Path] = []
        for root in (decoded / "smali", decoded / "smali_classes2",
                     decoded / "smali_classes3", decoded / "smali_classes4"):
            if not root.exists():
                continue
            for file in root.rglob("*.smali"):
                try:
                    data = file.read_text(encoding="utf-8", errors="replace")
                except OSError:
                    continue
                if PRIVACY_ANCHOR in data:
                    smali_hits.append(file)

        if not smali_hits:
            print(
                "error: the expected privacy-menu anchor was not found; "
                "this TikTok build needs a new integration adapter",
                file=sys.stderr,
            )
            return 5

        print("Detected privacy-menu anchor in:")
        for hit in smali_hits[:20]:
            print("  ", hit.relative_to(decoded))

        # Do not perform a guessed bytecode edit. The current TikTok build uses
        # generated/privacy menu data, so blindly editing the first matching
        # class is unsafe. The adapter is deliberately version-gated.
        print(
            "error: target structure detected, but no verified adapter exists "
            "for this exact TikTok build yet. No APK was emitted.",
            file=sys.stderr,
        )
        return 6


if __name__ == "__main__":
    raise SystemExit(main())
