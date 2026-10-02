# Ayurones VM — 350 Terminal Commands

Complete command reference for the `>_` terminal.

**Format:** `command [arguments]`

Commands execute from the guest environment. Some commands depend on Android/Toybox utilities available on the selected environment, so output can vary by Android version and device.

## Command Categories

| Category | Prefix | Description |
|---|---|---|
| File System | `fs.` | File and directory operations |
| Text Processing | `text.` | Text search and transformation |
| System Information | `sys.` | System and runtime information |
| Process Control | `proc.` | Process and shell controls |
| Networking | `net.` | Network inspection and utilities |
| Archives | `arch.` | Archive and checksum utilities |
| User Environment | `user.` | User, groups and environment |
| Packages | `package.` | Package-management commands where available |
| VM | `vm.` | Guest-environment diagnostics |
| Developer | `dev.` | Developer and debugging utilities |
| Data | `data.` | Data-processing utilities |
| Time | `time.` | Date, time and timing utilities |
| Math | `math.` | Calculation helpers |
| Diagnostics | `diag.` | System diagnostics |

## Complete Catalog

The catalog contains exactly 350 command entries. Use `help` inside `>_` to list them, or `help <name>` to filter the list.

The command names are grouped in the source catalog by category:

- **File System:** `fs.*`
- **Text Processing:** `text.*`
- **System Information:** `sys.*`
- **Process Control:** `proc.*`
- **Networking:** `net.*`
- **Archives:** `arch.*`
- **User Environment:** `user.*`
- **Packages:** `package.*`
- **VM:** `vm.*`
- **Developer:** `dev.*`
- **Data:** `data.*`
- **Time:** `time.*`
- **Math:** `math.*`
- **Diagnostics:** `diag.*`

## Terminal Usage

Examples:

```text
help
help fs
pwd
list
system.info
```

Arguments can be supplied after a command name. Unknown commands are passed to the guest shell.

## Guest Boundary

Commands operate from the application's private guest directory. The terminal does not grant root access to the Android host.
