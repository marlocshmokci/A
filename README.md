# Ayurones

Ayurones — Android VPN/TUN client with a feature-rich interface and a beginner-friendly bilingual guide.

## Features
- App branding: Ayurones.
- Launcher icon: white drop on black.
- No in-app support/donation button.
- Minimal first-run prompts.
- Buy a server / Купить сервер screen.
- Server request form opens https://t.me/eppere with a prepared message.
- Large English and Russian documentation.

## Documentation
- English: docs/USER_GUIDE.md
- Russian: docs/USER_GUIDE.ru.md

## Build
GitHub Actions checks out a pinned open-source client source tree, applies the Ayurones branding layer, fetches the pinned VPN core and builds an ARM64 release APK.

Artifact: AyuronesVPN-arm64.apk

## Licensing
Ayurones incorporates open-source VPN components. The upstream source and license/notice files remain part of the build process. See THIRD_PARTY_NOTICES.md for the redistribution summary.
