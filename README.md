👌# Ayurones

Ayurones — Android VPN/TUN client with a feature-rich interface and a beginner-friendly bilingual guide.

## Downloads

### Android
- **Ayurones v0.2.0 ARM64 APK:** open the latest successful Android build in GitHub Actions and download the APK artifact.

### Windows PC
- **Ayurones Desktop v0.2.0 (Windows x64 ZIP):** [Download page](https://github.com/marlocshmokci/A/actions/runs/36895146754)
- On the run page, open **Artifacts** → **Ayurones-Desktop-v0.2.0-windows-x64**.
- Extract the ZIP and run the Ayurones executable. No Android Studio or Android SDK is needed.

GitHub Actions stores workflow artifacts for a limited retention period, so the PC download page points to the build artifact rather than pretending it is a permanent release.

## Features
- App branding: Ayurones.
- Launcher icon: white drop on black.
- No in-app support/donation button.
- Minimal first-run prompts.
- Optional mini-guide on first launch with **Skip**.
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

## Rainwater note

Do not drink rainwater directly: it can contain pollutants and contaminants picked up from the atmosphere and surfaces. The phrase about “chemicals in clouds” is intentionally kept simple for the game/documentation and should not be read as a scientific claim that all clouds contain a specific chemical.
