from pathlib import Path
import re

root = Path("build/lxbox/app")

warp = root / "lib/services/warp/warp_account.dart"
if warp.exists():
    s = warp.read_text(encoding="utf-8")
    s = re.sub(
        r"return hasAwg \? '[^']*WARP\$plus \\(AWG 1\.5\\)' : '[^']*WARP\$plus';",
        "return hasAwg ? 'Ayurones WARP\$plus (AWG 1.5)' : 'Ayurones WARP\$plus';",
        s,
        count=1,
    )
    warp.write_text(s, encoding="utf-8")

controller = root / "lib/controllers/subscription_controller.dart"
if controller.exists():
    s = controller.read_text(encoding="utf-8")
    s = s.replace("int? persistentKeepalive,", "int? persistentKeepalive = 25,", 1)

    start = "  UserServer _autoEmoji(UserServer us) {"
    i = s.find(start)
    if i >= 0:
        depth = 0
        brace = s.find("{", i)
        end = None
        for k in range(brace, len(s)):
            if s[k] == "{":
                depth += 1
            elif s[k] == "}":
                depth -= 1
                if depth == 0:
                    end = k + 1
                    break
        if end is not None:
            s = s[:i] + "  UserServer _autoEmoji(UserServer us) {\n    return us;\n  }" + s[end:]

    marker = "    final lists = await SettingsStorage.getServerLists();\n    _entries = lists.map((l) => SubscriptionEntry(list: l)).toList();"
    replacement = """    final lists = await SettingsStorage.getServerLists();
    var warpMigrationChanged = false;
    final migratedLists = <ServerList>[];
    for (final list in lists) {
      if (list is! UserServer || list.rawBody.isEmpty ||
          !list.nodes.any((n) => n.tag.toUpperCase().contains('WARP'))) {
        migratedLists.add(list);
        continue;
      }
      var raw = list.rawBody;
      raw = raw
          .replaceAll('#%F0%9F%94%A5%E2%98%81%EF%B8%8F%20WARP', '#Ayurones%20WARP')
          .replaceAll('#%F0%9F%94%A5%E2%9B%88%EF%B8%8F%20WARP', '#Ayurones%20WARP')
          .replaceAll('🔥☁️ WARP', 'Ayurones WARP')
          .replaceAll('🔥⛈️ WARP', 'Ayurones WARP')
          .replaceAll('# WARP+', '# Ayurones WARP+')
          .replaceAll('# WARP', '# Ayurones WARP');

      final lower = raw.toLowerCase();
      if (lower.startsWith('wireguard://') && !lower.contains('keepalive=')) {
        final hash = raw.indexOf('#');
        if (hash >= 0) {
          raw = '${raw.substring(0, hash)}&keepalive=25${raw.substring(hash)}';
        } else {
          raw = '$raw&keepalive=25';
        }
      } else if (raw.contains('[Peer]') &&
          !lower.contains('persistentkeepalive')) {
        final endpointLine = RegExp(r'(?m)^Endpoint\s*=.*$').firstMatch(raw);
        if (endpointLine != null) {
          final at = endpointLine.end;
          raw = '${raw.substring(0, at)}\nPersistentKeepalive = 25${raw.substring(at)}';
        }
      }

      if (raw != list.rawBody) {
        try {
          final nodes = parseAll(decode(raw), own: true);
          migratedLists.add(list.copyWith(rawBody: raw, nodes: nodes));
          warpMigrationChanged = true;
        } catch (_) {
          migratedLists.add(list);
        }
      } else {
        migratedLists.add(list);
      }
    }
    _entries = migratedLists.map((l) => SubscriptionEntry(list: l)).toList();
    if (warpMigrationChanged) {
      configDirty = true;
      await SettingsStorage.saveServerLists(_entries.map((e) => e.list).toList());
    }"""
    if marker in s:
        s = s.replace(marker, replacement, 1)

    controller.write_text(s, encoding="utf-8")
