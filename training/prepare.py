"""Prepare plain text/JSONL training data.

Input: .txt or .jsonl files in training/data/raw.
Output: training/data/corpus.txt.
The script intentionally does not scrape the web or silently collect user chats.
"""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
RAW = ROOT / "data" / "raw"
OUT = ROOT / "data" / "corpus.txt"

def clean(text: str) -> str:
    return " ".join(text.replace("\x00", " ").split())

def main():
    RAW.mkdir(parents=True, exist_ok=True)
    texts = []
    for path in sorted(RAW.rglob("*")):
        if path.suffix.lower() == ".txt":
            texts.append(clean(path.read_text(encoding="utf-8")))
        elif path.suffix.lower() == ".jsonl":
            for line in path.read_text(encoding="utf-8").splitlines():
                if line.strip():
                    item = json.loads(line)
                    if isinstance(item.get("text"), str):
                        texts.append(clean(item["text"]))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n\n".join(x for x in texts if x), encoding="utf-8")
    print(f"Prepared {len(texts)} documents -> {OUT}")

if __name__ == "__main__":
    main()
