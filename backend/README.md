# Ayuron server

A small reference backend for the Android client's REST/WebSocket contract.

Run:

```
npm install
npm start
```

Default port: 8787

Realtime endpoint: ws://HOST:8787/v1/realtime

The server persists its development data in `data.json`. For production, replace that store with a database, add real authentication/session rotation, object storage, rate limiting, moderation and TLS.
