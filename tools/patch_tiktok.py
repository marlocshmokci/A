#!/usr/bin/env python3
"""Build TTAuOI from the exact supplied TikTok APK.

The source APK is kept outside git. This patcher modifies only the known TTAuOI
bundle, its bundled manifest, and the Android launcher label, then rewrites the
ZIP without recompressing unchanged APK payloads.

It fails closed on unknown source builds.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import struct
import sys
import zipfile
import zlib
from pathlib import Path

VERSION = "v2.0.5"
PACKAGE_UTF16 = "com.zhiliaoapp.musically".encode("utf-16le")
SOURCE_SHA256 = "7528c76344ae2636ed33895699dae441fdc58f87c5370ae97dc6ced8060d5adb"
SOURCE_SIZE = 368042912
SOURCE_DEX_SHA256 = "9c13da272735e4bd450bca55d0a9a678488f97f5062eaf1c70d2a43f2d21a9e5"
BUNDLED_DEX = "assets/tty/bundled/classes.dex"
BUNDLED_MANIFEST = "assets/tty/bundled/manifest.json"
ANDROID_MANIFEST = "AndroidManifest.xml"

DEX_REPLACEMENTS = {
    "Mod Settings": "Mod",
    "Telegram channel": "TTAuOI Telegram",
    "TikTok You": "TTAuOI",
    "TikTokYou": "TTAuOI",
    "Version ": "v2.0.5 ",
    "TikTok You development": "TTAuOI development",
}

REQUIRED_FEATURE_STRINGS = (
    "Hide ads",
    "Hide livestreams",
    "Hide photos",
    "Hide stories",
    "Minimum likes",
    "Minimum views",
    "Region spoofing",
    "Select region",
    "Custom colors",
    "TTAuOI Telegram",
)


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def read_uleb(data: bytes, offset: int) -> tuple[int, int]:
    value = 0
    shift = 0
    pos = offset
    while True:
        if pos >= len(data):
            raise ValueError("truncated ULEB128")
        b = data[pos]
        pos += 1
        value |= (b & 0x7F) << shift
        if b < 0x80:
            return value, pos
        shift += 7


def dex_strings(dex: bytes) -> list[tuple[int, int, int, int, int, str]]:
    if len(dex) < 112 or dex[:4] != b"dex\n":
        raise ValueError("not a valid DEX")
    string_count = struct.unpack_from("<I", dex, 56)[0]
    string_off = struct.unpack_from("<I", dex, 60)[0]
    if string_off + string_count * 4 > len(dex):
        raise ValueError("DEX string table is out of bounds")

    rows = []
    for index in range(string_count):
        item_off = struct.unpack_from("<I", dex, string_off + index * 4)[0]
        utf16_len, cursor = read_uleb(dex, item_off)
        end = dex.find(b"\x00", cursor)
        if end < 0:
            raise ValueError(f"missing NUL for DEX string #{index}")
        value = dex[cursor:end].decode("utf-8", "strict")
        rows.append((index, item_off, utf16_len, cursor, end, value))
    return rows


def patch_dex(src: bytes) -> tuple[bytes, list[tuple[str, str]]]:
    rows = dex_strings(src)
    by_value: dict[str, list[tuple[int, int, int, int, int]]] = {}
    for index, item_off, length, cursor, end, value in rows:
        by_value.setdefault(value, []).append((index, item_off, length, cursor, end))

    out = bytearray(src)
    changes: list[tuple[str, str]] = []
    for old, new in DEX_REPLACEMENTS.items():
        matches = by_value.get(old, [])
        if not matches:
            raise ValueError(f"required DEX string missing: {old!r}")
        if len(matches) != 1:
            raise ValueError(f"DEX string is ambiguous ({len(matches)} matches): {old!r}")

        _, item_off, old_len, _, end = matches[0]
        new_bytes = new.encode("utf-8")
        old_header = b""
        _, cursor = read_uleb(src, item_off)
        old_header = src[item_off:cursor]
        new_header = encode_uleb(len(new_bytes))
        if len(new_header) != len(old_header):
            raise ValueError(f"ULEB header would change footprint for {old!r}")
        item_end = end + 1
        footprint = item_end - item_off
        if len(new_header) + len(new_bytes) + 1 > footprint:
            raise ValueError(f"replacement does not fit for {old!r}")
        padded = new_header + new_bytes + b"\x00" * (footprint - len(new_header) - len(new_bytes))
        out[item_off:item_end] = padded
        changes.append((old, new))

    out = bytes(out)
    out = bytearray(out)
    out[12:32] = hashlib.sha1(out[32:]).digest()
    struct.pack_into("<I", out, 8, zlib.adler32(out[12:]) & 0xFFFFFFFF)
    return bytes(out), changes


def encode_uleb(value: int) -> bytes:
    out = bytearray()
    while True:
        b = value & 0x7F
        value >>= 7
        if value:
            b |= 0x80
        out.append(b)
        if not value:
            return bytes(out)


def patch_manifest_label(data: bytes) -> bytes:
    if struct.unpack_from("<I", data, 0)[0] == 0:
        raise ValueError("invalid binary XML manifest")
    sp = 8
    sp_type, sp_hs, sp_size = struct.unpack_from("<HHI", data, sp)
    if sp_type != 0x0001:
        raise ValueError("manifest string pool missing")
    string_count = struct.unpack_from("<I", data, sp + 8)[0]
    flags = struct.unpack_from("<I", data, sp + 16)[0]
    strings_start = struct.unpack_from("<I", data, sp + 20)[0]
    if flags & 0x100:
        raise ValueError("unexpected UTF-8 manifest string pool")

    out = bytearray(data)

    def item_pos(index: int) -> tuple[int, int]:
        item_off = struct.unpack_from("<I", out, sp + sp_hs + index * 4)[0]
        p = sp + strings_start + item_off
        length = struct.unpack_from("<H", out, p)[0]
        if length & 0x8000:
            raise ValueError("long UTF-16 manifest string is unsupported")
        return p, length

    def get_string(index: int) -> str:
        p, length = item_pos(index)
        return bytes(out[p + 2:p + 2 + length * 2]).decode("utf-16le", "strict")

    label_pool_index = None
    label_pos = None
    for index in range(string_count):
        value = get_string(index)
        if value == "TikTok":
            label_pool_index = index
            label_pos, length = item_pos(index)
            if length != 6:
                raise ValueError("expected six-character TikTok label string")
            break
    if label_pool_index is None or label_pos is None:
        raise ValueError("manifest pool does not contain the expected label string")

    out[label_pos + 2:label_pos + 14] = "TTAuOI".encode("utf-16le")

    pos = 8 + sp_size
    changed = False
    while pos + 8 <= len(out):
        chunk_type, chunk_header, chunk_size = struct.unpack_from("<HHI", out, pos)
        if chunk_size < 8 or pos + chunk_size > len(out):
            raise ValueError("invalid binary XML chunk")
        if chunk_type == 0x0102:  # RES_XML_START_ELEMENT_TYPE
            name_index = struct.unpack_from("<I", out, pos + 20)[0]
            if get_string(name_index) == "application":
                attr_start, attr_size, attr_count = struct.unpack_from("<HHH", out, pos + 24)
                attrs = pos + 16 + attr_start
                for i in range(attr_count):
                    attr = attrs + i * attr_size
                    attr_name = struct.unpack_from("<I", out, attr + 4)[0]
                    if get_string(attr_name) == "label":
                        struct.pack_into("<I", out, attr + 8, label_pool_index)
                        struct.pack_into("<HBBI", out, attr + 12, 8, 0, 0x03, label_pool_index)
                        changed = True
                        break
                break
        pos += chunk_size

    if not changed:
        raise ValueError("android:label on <application> not found")
    return bytes(out)


def is_signature_entry(name: str) -> bool:
    if not name.startswith("META-INF/"):
        return False
    upper = name.upper()
    leaf = upper.rsplit("/", 1)[-1]
    return leaf == "MANIFEST.MF" or leaf.startswith("SIG-") or upper.endswith(
        (".SF", ".RSA", ".DSA", ".EC")
    )


def raw_zip_rewrite(
    source_apk: Path,
    output_apk: Path,
    replacements: dict[str, bytes],
) -> None:
    with zipfile.ZipFile(source_apk, "r") as zin:
        infos = zin.infolist()
        if len(infos) >= 65535:
            raise ValueError("ZIP has too many entries for this ZIP64-free writer")

        central_start = zin.start_dir
        new_entries = []

        with source_apk.open("rb") as src, output_apk.open("wb") as dst:
            out_pos = 0
            for idx, info in enumerate(infos):
                name = info.filename
                if is_signature_entry(name):
                    continue

                if idx + 1 < len(infos):
                    next_header = infos[idx + 1].header_offset
                else:
                    next_header = central_start

                if name in replacements:
                    payload = replacements[name]
                    if info.compress_type == zipfile.ZIP_STORED:
                        compressed = payload
                    elif info.compress_type == zipfile.ZIP_DEFLATED:
                        compressor = zlib.compressobj(9, zlib.DEFLATED, -15)
                        compressed = compressor.compress(payload) + compressor.flush()
                    else:
                        raise ValueError(f"unsupported compression method for {name}: {info.compress_type}")

                    filename = name.encode("utf-8")
                    extra = info.extra
                    flag = info.flag_bits & ~0x0008
                    crc = zlib.crc32(payload) & 0xFFFFFFFF

                    src.seek(info.header_offset)
                    local = bytearray(src.read(30))
                    if len(local) != 30 or local[:4] != b"PK\x03\x04":
                        raise ValueError(f"bad local header for {name}")
                    fields = list(struct.unpack("<IHHHHHIIIHH", local))
                    fields[3] = flag
                    fields[6] = crc
                    fields[7] = len(compressed)
                    fields[8] = len(payload)
                    fields[9] = len(filename)
                    fields[10] = len(extra)
                    local_bytes = struct.pack("<IHHHHHIIIHH", *fields)

                    dst.write(local_bytes)
                    dst.write(filename)
                    dst.write(extra)
                    dst.write(compressed)

                    new_entries.append((info, name, crc, len(compressed), len(payload), out_pos, flag, filename, extra))
                    out_pos += len(local_bytes) + len(filename) + len(extra) + len(compressed)
                else:
                    raw_len = next_header - info.header_offset
                    src.seek(info.header_offset)
                    blob = src.read(raw_len)
                    if len(blob) != raw_len:
                        raise ValueError(f"short read for {name}")
                    dst.write(blob)
                    new_entries.append(
                        (info, name, info.CRC, info.compress_size, info.file_size, out_pos,
                         info.flag_bits, info.filename.encode("utf-8"), info.extra)
                    )
                    out_pos += raw_len

            cd_offset = out_pos
            for info, name, crc, csize, usize, offset, flag, filename, extra in new_entries:
                made_by = (info.create_system << 8) | info.create_version
                needed = info.extract_version
                method = info.compress_type
                year, month, day, hour, minute, second = info.date_time
                dostime = (hour << 11) | (minute << 5) | (second // 2)
                dosdate = ((year - 1980) << 9) | (month << 5) | day
                comment = info.comment or b""
                central = struct.pack(
                    "<IHHHHHHIIIHHHHHII",
                    0x02014B50,
                    made_by,
                    needed,
                    flag,
                    method,
                    dostime & 0xFFFF,
                    dosdate & 0xFFFF,
                    crc,
                    csize,
                    usize,
                    len(filename),
                    len(extra),
                    len(comment),
                    0,
                    info.internal_attr,
                    info.external_attr,
                    offset,
                )
                dst.write(central)
                dst.write(filename)
                dst.write(extra)
                dst.write(comment)
                out_pos += len(central) + len(filename) + len(extra) + len(comment)

            cd_size = out_pos - cd_offset
            whole_comment = zin.comment or b""
            eocd = struct.pack(
                "<IHHHHIIH",
                0x06054B50,
                0,
                0,
                len(new_entries),
                len(new_entries),
                cd_size,
                cd_offset,
                len(whole_comment),
            )
            dst.write(eocd)
            dst.write(whole_comment)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("apk", type=Path)
    ap.add_argument("--out", type=Path, default=Path(f"TTAuOI-{VERSION}-unsigned.apk"))
    args = ap.parse_args()

    apk = args.apk.resolve()
    out = args.out.resolve()
    if not apk.is_file():
        print(f"error: APK not found: {apk}", file=sys.stderr)
        return 2

    raw = apk.read_bytes() if apk.stat().st_size <= 400_000_000 else None
    actual_sha = hashlib.sha256(apk.read_bytes()).hexdigest() if raw is not None else ""
    if apk.stat().st_size != SOURCE_SIZE or actual_sha != SOURCE_SHA256:
        print(
            f"error: source APK mismatch. size={apk.stat().st_size} sha256={actual_sha}",
            file=sys.stderr,
        )
        return 3

    with zipfile.ZipFile(apk, "r") as zin:
        manifest = zin.read(ANDROID_MANIFEST)
        if PACKAGE_UTF16 not in manifest:
            print(f"error: expected package {PACKAGE_UTF16!r} not found in manifest", file=sys.stderr)
            return 4
        dex = zin.read(BUNDLED_DEX)
        if sha256(dex) != SOURCE_DEX_SHA256:
            print(f"error: unsupported bundled DEX sha256={sha256(dex)}", file=sys.stderr)
            return 5
        if len(dex) != 875436:
            print(f"error: unsupported bundled DEX size={len(dex)}", file=sys.stderr)
            return 6

        patched_dex, changes = patch_dex(dex)
        patched_sha = sha256(patched_dex)
        bundle_manifest = json.loads(zin.read(BUNDLED_MANIFEST).decode("utf-8"))
        bundle_manifest["targetDexSha256"] = patched_sha
        bundle_manifest["targetSize"] = len(patched_dex)
        for entry in bundle_manifest.get("dexFiles", []):
            if entry.get("name") == "classes.dex":
                entry["targetSha256"] = patched_sha
                entry["targetSize"] = len(patched_dex)
        bundle_manifest.setdefault("hashes", {})["classes.dex"] = patched_sha
        patched_bundle_manifest = (json.dumps(bundle_manifest, separators=(",", ":")) + "\n").encode("utf-8")
        patched_android_manifest = patch_manifest_label(manifest)

        # Fast verification before writing the final APK.
        strings = {row[5] for row in dex_strings(patched_dex)}
        missing = [s for s in REQUIRED_FEATURE_STRINGS if s not in strings]
        if missing:
            print(f"error: required feature strings missing after patch: {missing}", file=sys.stderr)
            return 7

    raw_zip_rewrite(
        apk,
        out,
        {
            ANDROID_MANIFEST: patched_android_manifest,
            BUNDLED_DEX: patched_dex,
            BUNDLED_MANIFEST: patched_bundle_manifest,
        },
    )

    with zipfile.ZipFile(out, "r") as zout:
        if zout.testzip() is not None:
            print("error: output ZIP integrity check failed", file=sys.stderr)
            return 8
        check_dex = zout.read(BUNDLED_DEX)
        check_bundle = json.loads(zout.read(BUNDLED_MANIFEST).decode("utf-8"))
        if sha256(check_dex) != check_bundle.get("targetDexSha256"):
            print("error: bundled manifest does not match patched DEX", file=sys.stderr)
            return 9

    print(f"patched: {', '.join(f'{a} -> {b}' for a,b in changes)}")
    print(f"patched DEX sha256: {patched_sha}")
    print(f"unsigned APK: {out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
