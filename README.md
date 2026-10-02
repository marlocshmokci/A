 # Ayurones VM

Android sandbox application replacing the old Messenger project.

## Features
- Terminal running under the app's own Linux UID.
- Private guest filesystem inside the app's internal storage.
- File inspection, system diagnostics and one-tap guest reset.
- No storage, network, camera, microphone, contacts or root permissions.
- Guest commands cannot become root through this app.

This first version is a safe sandbox, not a claim of full hardware virtualization. Android Virtualization Framework (AVF) and protected KVM are platform/device dependent. The architecture leaves room for an AVF backend on compatible devices.

Build project: apps/ayurones-vm
