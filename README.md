# AyuronesVPN

Android VPN/TUN client build based on the pinned LxBox/sing-box core.

## Build

GitHub Actions builds an ARM64 release APK automatically on pushes to `main` and can also be started manually from the Actions tab.

The generated artifact is named `AyuronesVPN-arm64`.

## Supported core protocols

The underlying sing-box/LxBox core supports protocols including VLESS, VMess, Trojan, Shadowsocks, Hysteria2, TUIC, WireGuard and XHTTP.

## Reproducibility

LxBox is pinned to commit `bb7c8eeab8315f4454e99399888813f1cd32d230`.

Build toolchain:
- Flutter 3.47.1
- Java 17
- Android NDK 28.0.13004108
- Android API 36

## License

The application incorporates LxBox/sing-box components. Their upstream licenses and notices apply to the corresponding components.
