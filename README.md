# Ayurones VM

Android sandbox / virtual-device interface.

## Ayurones VM 0.2.1

Old-Android-style guest environment with a built-in developer terminal.

### Download APK

**[📥 Download Ayurones VM 0.2.1 APK](https://github.com/marlocshmokci/A/releases/download/v0.2.1/Ayurones-VM-v0.2.1-debug.apk)**

This link points directly to the APK Release asset.

### Documentation

[📚 350 Terminal Commands](apps/ayurones-vm/COMMANDS.md)

### Features

- Old-Android-style desktop.
- Hold an empty area for 5 seconds to enter the VM menu.
- Localized waiting screen.
- Android 4.4 through Android 16 guest environment options.
- Background guest-environment preparation.
- Virtual-device settings.
- Tap the device model four times to unlock the developer terminal.
- Draggable `>_` terminal button.
- Built-in catalog of 350 terminal commands.
- Camera permission is requested only when the camera is opened.
- Files are opened through the Android system document picker.

### Isolation

The application does not receive root access to the Android host. Guest data is stored inside the application's private storage.

> Note: Android versions in this build are prepared guest environments, not separate hardware-virtualized Android kernels. True hardware virtualization requires an Android Virtualization Framework/pKVM backend supported by the device.

Project: `apps/ayurones-vm`
