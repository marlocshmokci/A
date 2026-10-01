from pathlib import Path

root = Path("build/lxbox/app")

warp = root / "lib/services/warp/warp_account.dart"
if warp.exists():
    s = warp.read_text(encoding="utf-8")
    s = s.replace("return hasAwg ? '🔥⛈️ WARP$plus (AWG 1.5)' : '🔥☁️ WARP$plus';", "return hasAwg ? 'Ayurones WARP$plus (AWG 1.5)' : 'Ayurones WARP$plus';")
    warp.write_text(s, encoding="utf-8")

controller = root / "lib/controllers/subscription_controller.dart"
if controller.exists():
    s = controller.read_text(encoding="utf-8")
    s = s.replace("int? persistentKeepalive,", "int? persistentKeepalive = 25,", 1)
    start = "  UserServer _autoEmoji(UserServer us) {"
    i = s.find(start)
    if i >= 0:
        j = s.find("\n  }\n", i)
        if j >= 0:
            j += len("\n  }")
            s = s[:i] + "  UserServer _autoEmoji(UserServer us) {\n    return us;\n  }" + s[j:]
    controller.write_text(s, encoding="utf-8")
