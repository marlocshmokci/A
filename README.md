# Ayuron AI

Mobile-first dark chat interface and Cloudflare Worker proxy for Groq. This is a starter build, not yet deployed.

## Current features
- Chat interface inspired by the supplied screenshot
- Local browser chat history (stored in this device/browser)
- Dark/light theme and accent color
- Configurable API endpoint and model
- Worker proxy that keeps the Groq key server-side

## Deploy the API (free tier)
1. Create a Cloudflare Worker named `ayuron-api`.
2. Paste the contents of `worker/index.js` into its editor and deploy.
3. In Worker Settings → Variables and Secrets, add a secret named `GROQ_API_KEY` with a newly created Groq API key.
4. The API endpoint will be `https://YOUR-WORKER.YOUR-SUBDOMAIN.workers.dev/chat`. Put that URL in Ayuron Settings.
5. Do not place API keys in `index.html`, APK assets, GitHub, or chat messages. Revoke any key that was accidentally shared.

Cloudflare Workers Free currently documents 100,000 requests/day, subject to plan limits. The Groq API itself has its own model and rate limits.

## Host the interface
For a web preview, enable GitHub Pages for this repository and the `main` branch / root folder. Then open the published page and set the Worker URL in Settings.

## Not finished yet
- Android APK packaging and verification
- GitHub plugin installer, manifest validation and permission screen (the menu entry is currently a placeholder)
- Server-side chat persistence / accounts (chat history currently stays in local browser storage)
- Image generation and real web search integrations (the visible shortcuts are UI only)

The Worker currently permits cross-origin requests from any origin to make first setup simple. Before public release, restrict allowed origins and add rate limiting/authentication to prevent other people from consuming your API quota.