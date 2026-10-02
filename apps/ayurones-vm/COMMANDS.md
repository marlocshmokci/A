# Ayurones VM — 350 Terminal Commands

Complete command reference for the `>_` terminal.

**Format:** `command [arguments]`

Commands execute from the guest environment. Some commands depend on Android/Toybox utilities available on the selected environment, so output can vary by Android version and device.

## Categories

- **File System** — `fs.*`
- **Text Processing** — `text.*`
- **System Information** — `sys.*`
- **Process Control** — `proc.*`
- **Networking** — `net.*`
- **Archives** — `arch.*`
- **User Environment** — `user.*`
- **Packages** — `package.*`
- **VM** — `vm.*`
- **Developer** — `dev.*`
- **Data** — `data.*`
- **Time** — `time.*`
- **Math** — `math.*`
- **Diagnostics** — `diag.*`

The catalog contains exactly 350 command entries. Use `help` inside `>_` to list them, or `help <name>` to filter the list.

## Terminal Usage

Examples:

```text
help
help fs
pwd
list
vm.info
```

Arguments can be supplied after a command name. Unknown commands are passed to the guest shell.

## Guest Boundary

Commands operate from the application's private guest directory. The terminal does not grant root access to the Android host.
