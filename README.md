# TTAuOI — TikTok AUOI v2.0.5

TTAuOI is the mod layer for the supplied TikTok You 4.2 (46.8.3) base.

## Target UI

Profile → Privacy → Mod

- TTAuOI Telegram Channel → https://t.me/TTAuOI
- Region override with ISO 3166-1 country selection
- Content filters: minimum likes, minimum views, publication-age limit
- Hide LIVE
- Hide ads
- Hide feed photos
- Hide Story photos
- Appearance with 10 accent colors for likes, comments and Follow
- Restart prompt after settings that require app recreation
- Version v2.0.5

## Verified local build

Full APK size: 364,881,429 bytes.

SHA-256:
`db15a53082987a6c4f299d764ea853de27d412fbaf5c38585886d8ad112ef3ad`

The local build was checked for ZIP integrity, package identity, TTAuOI launcher label, bundled DEX hash/manifest consistency, the mod entry point, and the requested feature strings. The APK also passes `jarsigner -verify`.

## Source APK

The source APK is intentionally not committed to git. The CI patcher is locked to the exact source build:

- Size: 368,042,912 bytes
- SHA-256: `7528c76344ae2636ed33895699dae441fdc58f87c5370ae97dc6ced8060d5adb`

Run locally:

```bash
python3 tools/patch_tiktok.py /path/to/TikTok.apk --out TTAuOI-v2.0.5-unsigned.apk
```

The patcher fails closed on any source mismatch.

## CI

GitHub Actions now builds the full TikTok-based TTAuOI APK rather than the old standalone settings test app. A manual run takes a direct HTTPS URL to the exact source APK, verifies its SHA-256, patches TTAuOI, signs the result, checks that the APK is at least 265 MB, and uploads the complete APK as a workflow artifact.

The repository does not contain the third-party source APK itself.
