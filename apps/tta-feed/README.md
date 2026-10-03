# TTA — standalone TikTok feed

TTA is a separate Android app, not a patched TikTok APK.

## What it does
- Vertical full-screen feed.
- Import a TikTok video using Android Share → TTA.
- Paste a TikTok video URL into the app.
- Stores imported video IDs locally.
- Plays the videos with TikTok's official embedded player.
- No TikTok APK is bundled.

The embedded player is the official TikTok player endpoint. A future version can add TikTok Login + Display API to synchronize authorized users' public videos automatically.

## Build
From this directory:

`gradle assembleDebug`

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
