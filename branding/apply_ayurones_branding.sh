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
s = p.read_text(encoding="utf-8")
s = s.replace("name: lxbox", "name: ayurones")
s = re.sub(r"^version:.*$", "version: 0.2.0+20", s, flags=re.MULTILINE)
s = re.sub(r"^description:.*$", "description: Ayurones — Android VPN client powered by sing-box.", s, flags=re.MULTILINE)
s = s.replace("    - assets/donate.json\n", "")
s = s.replace("    - assets/support.json\n", "")
p.write_text(s, encoding="utf-8")

for p in root.glob("android/app/src/main/res/**/strings.xml"):
    s = p.read_text(encoding="utf-8")
    s = re.sub(r'(<string\s+name="app_name"[^>]*>).*?(</string>)', r"\1Ayurones\2", s)
    p.write_text(s, encoding="utf-8")

p = root / "lib/services/project_links.dart"
if p.exists():
    s = p.read_text(encoding="utf-8")
    replacements = {
        "https://github.com/Leadaxe/LxBox": "https://github.com/marlocshmokci/A",
        "https://github.com/Leadaxe/sing-box-lx": "https://github.com/SagerNet/sing-box",
        "https://github.com/Leadaxe/singbox-launcher": "https://github.com/SagerNet/sing-box",
        "https://t.me/singbox_launcher/4317": "https://t.me/eppere",
        "https://t.me/singbox_launcher/340/3621": "https://t.me/eppere",
        "https://github.com/Leadaxe/LxBox/releases/latest": "https://github.com/marlocshmokci/A/releases/latest",
        "https://github.com/Leadaxe/LxBox/releases/tag/$tag": "https://github.com/marlocshmokci/A/releases/tag/$tag",
        "https://github.com/Leadaxe/LxBox/issues": "https://github.com/marlocshmokci/A/issues",
        "https://github.com/Leadaxe/LxBox/blob/main/docs/USER_GUIDE.md": "https://github.com/marlocshmokci/A/blob/main/docs/USER_GUIDE.md",
        "https://github.com/Leadaxe/LxBox/blob/main/docs/USER_GUIDE.ru.md": "https://github.com/marlocshmokci/A/blob/main/docs/USER_GUIDE.ru.md",
    }
    for a, b in replacements.items():
        s = s.replace(a, b)
    p.write_text(s, encoding="utf-8")

skip_names = {"THIRD_PARTY_NOTICES.md", "LICENSE", "LICENSING.md"}
extensions = {".dart", ".xml", ".json", ".yaml", ".md", ".txt"}
for p in root.rglob("*"):
    if not p.is_file() or p.name in skip_names or p.suffix.lower() not in extensions:
        continue
    try:
        s = p.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        continue
    old = s
    s = s.replace("L×Box", "Ayurones")
    s = s.replace("LxBox", "Ayurones")
    if s != old:
        p.write_text(s, encoding="utf-8")
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
    im = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0) if transparent else (0, 0, 0, 255))
    d = ImageDraw.Draw(im)
    d.ellipse((290, 180, 734, 624), fill=(255, 255, 255, 255))
    d.polygon([(300, 470), (724, 470), (512, 850)], fill=(255, 255, 255, 255))
    im.save(out / path, optimize=True)

make("app_icon.png")
make("app_icon_ios.png")
make("ic_launcher_foreground.png", transparent=True)
make("ic_launcher_background.png")
PY

python3 "$ROOT/branding/apply_network_fixes.py"

echo "Ayurones branding applied."
