# Ayurones VM

Android guest sandbox with an old-Android-style interface.

## Terminal

The `>_` developer terminal contains exactly 350 built-in command names.

Full reference:

[COMMANDS.md](COMMANDS.md)

Commands run from the private guest directory. Unknown shell commands are also passed to the guest shell.

## Safety boundary

The app does not grant root access to the Android host. Guest data is stored inside the application's private storage boundary. Camera access is requested only when the camera is opened, and file access uses the Android system document picker.

Android 4.4 through Android 16 are represented as prepared guest environments in this build; they are not separate hardware-virtualized Android kernels.
