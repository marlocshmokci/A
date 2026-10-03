# Ayuron server contract

The Android client is local-first. A production deployment can connect it to a server implementing these endpoints.

## Authentication
POST /v1/auth/register
POST /v1/auth/login

## Feed
GET /v1/feed?cursor=<cursor>
POST /v1/posts
POST /v1/posts/:id/like
POST /v1/posts/:id/repost
POST /v1/posts/:id/reply

## Messaging
GET /v1/chats
GET /v1/chats/:id/messages?cursor=<cursor>
POST /v1/chats/:id/messages
POST /v1/messages/:id/react

## Channels
GET /v1/channels
GET /v1/channels/:id
POST /v1/channels/:id/join

## Search
GET /v1/search?q=<query>

## Realtime
A WebSocket endpoint at /v1/realtime can emit:
message.new
message.reaction
post.new
post.like
post.reply
notification.new

All timestamps should be ISO-8601 UTC strings.
