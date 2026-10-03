import json
import random
from pathlib import Path

import torch
from tokenizers import Tokenizer
from tqdm import tqdm

from model import AyuronesLM

ROOT = Path(__file__).resolve().parent
CFG = json.loads((ROOT / "config.json").read_text())
CORPUS = ROOT / "data" / "corpus.txt"
TOKENIZER_PATH = ROOT / "tokenizer.json"
OUT = ROOT / "checkpoints"
OUT.mkdir(exist_ok=True)

random.seed(CFG["seed"])
torch.manual_seed(CFG["seed"])

if not CORPUS.exists():
    raise SystemExit("Run: python prepare.py first")

if not TOKENIZER_PATH.exists():
    raise SystemExit("Create training/tokenizer.json first. See tokenizer.py")

tokenizer = Tokenizer.from_file(str(TOKENIZER_PATH))
ids = tokenizer.encode(CORPUS.read_text(encoding="utf-8")).ids
if len(ids) <= CFG["max_seq_len"] + 1:
    raise SystemExit("Corpus is too small for the configured sequence length.")

device = "cuda" if torch.cuda.is_available() else "cpu"
model = AyuronesLM(
    CFG["vocab_size"], CFG["max_seq_len"], CFG["d_model"],
    CFG["n_heads"], CFG["n_layers"], CFG["dropout"]
).to(device)

optimizer = torch.optim.AdamW(model.parameters(), lr=CFG["learning_rate"], weight_decay=0.1)
step = 0

def batch():
    starts = torch.randint(0, len(ids) - CFG["max_seq_len"] - 1, (CFG["batch_size"],))
    x = torch.stack([torch.tensor(ids[s:s+CFG["max_seq_len"]]) for s in starts]).to(device)
    y = torch.stack([torch.tensor(ids[s+1:s+CFG["max_seq_len"]+1]) for s in starts]).to(device)
    return x, y

for epoch in range(CFG["epochs"]):
    model.train()
    optimizer.zero_grad(set_to_none=True)
    progress = tqdm(range(max(1, len(ids) // CFG["max_seq_len"] // CFG["batch_size"])), desc=f"epoch {epoch+1}")
    for _ in progress:
        x, y = batch()
        _, loss = model(x, y)
        (loss / CFG["grad_accumulation"]).backward()
        if (step + 1) % CFG["grad_accumulation"] == 0:
            torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
            optimizer.step()
            optimizer.zero_grad(set_to_none=True)
        step += 1
        progress.set_postfix(loss=f"{loss.item():.4f}")
        if step % CFG["save_every"] == 0:
            torch.save({"model": model.state_dict(), "config": CFG, "step": step}, OUT / f"step-{step}.pt")

torch.save({"model": model.state_dict(), "config": CFG, "step": step}, OUT / "final.pt")
print(f"Saved {OUT / 'final.pt'}")
