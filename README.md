# Ayurones

Ayurones is becoming a minimal, strict messenger.

## Apps

### Ayurones
Android messenger with chats, contacts, local message history, profile photo selection, activity points, a decoration store, and a Test Server connection check.

### Test Server
A separate Android testing application. It can run a local HTTP test endpoint on port 8787, expose health/status responses, generate test counters, and unlock unlimited test modes for future features.

### Ayurones Desktop
Windows companion with chats, profile, activity points and decorations.

### Капельки
Standalone endless 2D mini-game with persistent best score.

## Design
The messenger interface is deliberately restrained: black background, white type, quiet borders and compact controls. The **Украшения** section is intentionally more expressive.

## Repository structure
- `apps/ayurones-messenger` — Android messenger
- `apps/test-server` — Android Test Server
- `desktop/ayurones` — Windows messenger
- `games/ayurones-drops` — Капельки
- `docs/USER_GUIDE.md` — English guide
- `docs/USER_GUIDE.ru.md` — Russian guide

The current product direction is messaging; old network-client branding and documentation are no longer part of the active application surface.
