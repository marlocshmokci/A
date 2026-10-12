# Ballistic Lab

Android project packaged from the supplied `BallisticLab.zip`.

## Build APK

A GitHub Actions workflow builds the debug APK on pushes to `main` and publishes the APK as a GitHub Release.

- Open the **Actions** tab to inspect build logs.
- Open **Releases** to download the APK.
- The app uses a local HTML canvas inside Android WebView; it does not need an internet connection for the main simulation.

The project archive is stored as base64 parts under `.build/` so Actions can reconstruct the exact uploaded ZIP before building.
