# TTAuOI — TikTok AUOI v2.0.5

TTAuOI is the mod layer for a user-supplied TikTok Android APK.

## TTAuOI v2.0.5

Полная сборка: 348 МБ.

SHA-256:
`a7d04e254bf853d3db4241dc6e56667e919e3499b5cededb0c6ef6997cdda30f`

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

## Source APK

The uploaded TikTok APK is not committed to this repository. It is a third-party binary and is intentionally kept outside Git history.

The local integration command is:

```bash
python3 tools/patch_tiktok.py /path/to/TikTok.apk --out TTAuOI-v2.0.5.apk
```

The patcher is version-gated and fails closed when the target structure is not verified.

## CI

GitHub Actions builds the TTAuOI settings/core project as a smoke-test APK. It does not pretend that this standalone APK is the modified TikTok client.
