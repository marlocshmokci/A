# TTAuOI — TikTok AUOI v2.0.5

TTAuOI is a mod/integration project for a user-supplied, legally obtained TikTok Android APK.

## Requested feature set

- Profile → Privacy: **Mod** entry at the top.
- **TTAuOI Telegram Channel** → https://t.me/TTAuOI
- Region override: all ISO 3166-1 countries.
- Content filters: minimum likes, minimum views, maximum publication age.
- Hide LIVE streams.
- Hide ads.
- Hide photos in the feed.
- Hide photos in Stories.
- Appearance: 10 selectable accent colors affecting like/comment/follow controls.
- Restart prompt after settings that require process recreation.
- Version: **v2.0.5**.

## Important build model

The repository does not contain TikTok's APK. The uploaded base APK must remain outside Git history because it is a large third-party application binary.

The real-mod pipeline is:

1. provide the target TikTok APK to the patch command;
2. decode it with Apktool 3.x;
3. locate the privacy/settings menu and compatible integration points;
4. add the TTAuOI settings layer;
5. rebuild and sign the resulting APK with a user-controlled signing key.

The patcher fails closed when the exact target structure cannot be located; it never silently produces an APK that merely looks modified.

## Standalone settings build

The Android project under `apps/ttauoi` is the TTAuOI settings/core layer and CI smoke-test target. It is not presented as a modified TikTok client.

## Local real-mod build

Install Apktool 3.0.3 and run:

```bash
python3 tools/patch_tiktok.py /path/to/TikTok.apk --out TTAuOI-v2.0.5.apk
```

The script verifies the input package is `com.zhiliaoapp.musically`, verifies the expected privacy-menu anchor, and stops if the target build is structurally incompatible.

Apktool 3.0.3 is the current 3.x release used by this project.
