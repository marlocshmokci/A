# NEXA — social messenger

NEXA is a standalone Android messenger that combines a public microblog feed with Telegram-style private chats and channels.

## Included in the first build
- Home feed with posts, likes, replies, reposts, bookmarks and sharing.
- Post composer with character counter.
- Private conversations with message history and reactions.
- Channel directory and channel pages.
- Search across posts, people, chats and channels.
- Notifications inbox.
- Editable local profile: display name, handle, bio.
- Appearance: Dark, AMOLED and Light modes; 10 accent colors; font scale; compact cards; reduced motion.
- Privacy toggles and notification preferences.
- Android share target: text shared from another app opens the NEXA composer.
- Local-first persistence. Demo content is included so the app is useful immediately.
- Backend-ready settings page for a future REST/WebSocket server; no private credentials are embedded.

## Architecture
The Android client is intentionally dependency-light and uses the platform UI toolkit. App state is stored locally with SharedPreferences/JSON. The next backend layer can implement the documented REST contract in `backend/API.md`.

## Build
The GitHub Actions workflow builds a debug APK from `apps/nexa`.

This is a new application; the previous TikTok project has been removed from the repository.
