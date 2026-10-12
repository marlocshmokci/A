# Ballistic Lab

GitHub Actions is configured to build an Android debug APK from `BallisticLab.zip`.

## First build

1. Upload the original `BallisticLab.zip` file to the repository root (the same level as this README).
2. Commit the upload to `main`.
3. Open **Actions** and wait for **Build Ballistic Lab APK** to finish.
4. Download the APK from **Releases** (or from the workflow's Artifacts section).

The workflow uses Java 17 and Gradle 8.7. The app runs its local HTML canvas inside Android WebView.
