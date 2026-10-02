# Ayurones VM

Android guest sandbox with an old-Android-style interface.

## Terminal

The `>_` developer terminal contains a catalog of exactly 350 built-in command names. Full command reference:

[COMMANDS.md](COMMANDS.md)

Commands execute from the private guest directory. Unknown shell commands are also passed to the guest shell.

## Safety boundary

The app does not grant host root. The guest data is stored under the application's private storage boundary. Camera access is requested only when the camera is opened, and file access uses the Android system document picker.

Android 4.4 through Android 16 are represented as prepared guest environments in this build; they are not separate hardware-virtualized Android kernels.
