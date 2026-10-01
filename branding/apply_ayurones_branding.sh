#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP="$ROOT/build/lxbox/app"

echo "Applying Ayurones branding..."

python3 - <<'PY'
from pathlib import Path
import re

root = Path("build/lxbox/app")

p = root / "pubspec.yaml"
s = p.read_text()
s = s.replace("name: lxbox", "name: ayurones")
s = s.replace("description: L×Box — Android VPN client (sing-box / libbox CommandClient).",
              "description: Ayurones — Android VPN client powered by sing-box.")
s = s.replace("    - assets/donate.json\n", "")
s = s.replace("    - assets/support.json\n", "")
p.write_text(s)

for p in root.glob("android/app/src/main/res/**/strings.xml"):
    s = p.read_text()
    s = re.sub(r'(<string\s+name="app_name"[^>]*>).*?(</string>)', r'\1Ayurones\2', s)
    p.write_text(s)

p = root / "lib/services/project_links.dart"
s = p.read_text()
for a,b in {
    "https://github.com/Leadaxe/LxBox": "https://github.com/marlocshmokci/A",
    "https://github.com/Leadaxe/sing-box-lx": "https://github.com/SagerNet/sing-box",
    "https://github.com/Leadaxe/singbox-launcher": "https://github.com/SagerNet/sing-box",
    "https://t.me/singbox_launcher/4317": "https://t.me/eppere",
    "https://t.me/singbox_launcher/340/3621": "https://t.me/eppere",
}.items():
    s=s.replace(a,b)
s=s.replace("https://github.com/Leadaxe/LxBox/releases/latest",
            "https://github.com/marlocshmokci/A/releases/latest")
s=s.replace("https://github.com/Leadaxe/LxBox/releases/tag/$tag",
            "https://github.com/marlocshmokci/A/releases/tag/$tag")
s=s.replace("https://github.com/Leadaxe/LxBox/issues",
            "https://github.com/marlocshmokci/A/issues")
s=s.replace("https://github.com/Leadaxe/LxBox/blob/main/docs/USER_GUIDE.md",
            "https://github.com/marlocshmokci/A/blob/main/docs/USER_GUIDE.md")
s=s.replace("https://github.com/Leadaxe/LxBox/blob/main/docs/USER_GUIDE.ru.md",
            "https://github.com/marlocshmokci/A/blob/main/docs/USER_GUIDE.ru.md")
p.write_text(s)

p = root / "lib/screens/home/widgets/home_drawer.dart"
s = p.read_text()
if "ayurones_server_screen.dart" not in s:
    s=s.replace("import '../../subscriptions_screen.dart';",
                "import '../../subscriptions_screen.dart';\nimport '../../ayurones_server_screen.dart';")
marker = """            ListTile(
              leading: const Icon(Icons.alt_route_outlined),"""
tile = """            ListTile(
              leading: const Icon(Icons.shopping_cart_outlined),
              title: const Text('Buy a server / Купить сервер'),
              subtitle: const Text('Choose location, protocol and traffic'),
              onTap: () => _go(context, const AyuronesServerScreen()),
            ),
"""
s=s.replace(marker, tile+marker)
p.write_text(s)

p = root / "lib/screens/home_screen.dart"
s = p.read_text().replace("      unawaited(_maybeShowSupport());\n", "")
p.write_text(s)
PY

cp "$ROOT/branding/ayurones_server_screen.dart" "$APP/lib/screens/ayurones_server_screen.dart"
cp "$ROOT/branding/about_screen.dart" "$APP/lib/screens/about_screen.dart"
cp "$ROOT/branding/startup_wizard.dart" "$APP/lib/screens/home/startup_wizard.dart"

python3 -m pip install --disable-pip-version-check --quiet pillow
python3 - <<'PY'
from PIL import Image, ImageDraw
from pathlib import Path

out = Path("build/lxbox/app/assets/icons")
out.mkdir(parents=True, exist_ok=True)

def make(path, transparent=False):
    im = Image.new("RGBA", (1024,1024), (0,0,0,0) if transparent else (0,0,0,255))
    d = ImageDraw.Draw(im)
    d.ellipse((290,180,734,624), fill=(255,255,255,255))
    d.polygon([(300,470),(724,470),(512,850)], fill=(255,255,255,255))
    im.save(out / path, optimize=True)

make("app_icon.png")
make("app_icon_ios.png")
make("ic_launcher_foreground.png", transparent=True)
make("ic_launcher_background.png")
PY

echo "Ayurones branding applied."
