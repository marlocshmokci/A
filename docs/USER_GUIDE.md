# Ayurones User Guide — English

Ayurones has a feature-rich interface. This guide is intentionally much simpler: follow only the section you need.

## 1. First launch
1. Open Ayurones.
2. If Android asks for notification permission, allow it. A VPN foreground service may use a persistent notification.
3. You do not need to enable every optional background feature just to connect.
4. Open the left navigation drawer for the complete section list.

## 2. Main screen
- Start / Stop — starts or stops the VPN tunnel.
- Server list — imported servers and subscriptions.
- Ping / status — helps identify a responsive server.
- Traffic — shows activity while connected.
- Left menu — opens all sections.

## 3. Adding a server
Open Servers. You can import a subscription URL, a proxy/share link, or another supported configuration source.

Common protocols include VLESS, VMess, Trojan, Shadowsocks, Hysteria2, TUIC, WireGuard and XHTTP.

## 4. Connecting
1. Select a server.
2. Press Start.
3. Accept Android's VPN confirmation if shown.
4. Wait for the connected status.
5. To disconnect, press Stop.

## 5. Buying a server
Open the drawer and choose Buy a server / Купить сервер.

You can specify country, protocol, performance, purpose, monthly traffic, term and additional requirements.

Press Open Telegram and request server. Ayurones opens t.me/eppere with the request prepared in the message field. Telegram documents username links with pre-filled draft text: https://core.telegram.org/api/links

## 6. Routing
Open Routing to decide which traffic uses which direction.

Basic workflow:
1. Open Routing.
2. Create or edit a direction.
3. Select the server/group.
4. Add matching rules.
5. Save or leave the screen according to the current UI.

If you are unsure, keep the defaults.

## 7. DNS
Open DNS Settings to change DNS servers or DNS rules. Change one setting at a time and test the tunnel after each change.

## 8. VPN Settings
VPN Settings contains advanced tunnel/configuration options. Normal users can usually leave advanced values unchanged.

## 9. Speed Test
Use Speed Test to compare servers. Results depend on your current network and load, so they are measurements rather than permanent server properties.

## 10. Statistics
Statistics is most useful while the tunnel is active. It can show traffic and connection information.

## 11. Config Editor
Config Editor is an advanced sing-box JSON editor. Only edit raw configuration if you understand its structure; malformed JSON can prevent a tunnel from starting.

## 12. App Settings
App Settings controls application-level preferences such as appearance and other local options.

## 13. Debug
If something fails:
1. Reproduce the problem once.
2. Open Debug.
3. Read the newest error.
4. Determine whether it concerns the server, DNS, permission or configuration.
5. Avoid changing many settings simultaneously.

## 14. Common problems

### VPN does not start
Check Android VPN permission, check the selected server, and try another server.

### Internet stops after connecting
Stop the VPN, try another server, then check DNS and Routing.

### High latency
Run Speed Test and try another location. Repeat later if network conditions change.

### Telegram does not open
Make sure Telegram is installed. You can also open t.me/eppere in a browser.

## 15. Beginner rule
Do not change raw JSON, complex DNS rules, detours/chains or low-level core options unless you know why you are changing them.

Start with one server and the default configuration.

## 16. Documentation
The repository contains this English guide and a matching Russian guide:
- docs/USER_GUIDE.md
- docs/USER_GUIDE.ru.md
