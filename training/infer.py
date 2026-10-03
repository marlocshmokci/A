import json
from pathlib import Path
import torch
from tokenizers import Tokenizer
from model import AyuronesLM

ROOT=Path(__file__).resolve().parent
CFG=json.loads((ROOT/"config.json").read_text())
tokenizer=Tokenizer.from_file(str(ROOT/"tokenizer.json"))
ckpt=torch.load(ROOT/"checkpoints/final.pt",map_location="cpu")
model=AyuronesLM(CFG["vocab_size"],CFG["max_seq_len"],CFG["d_model"],CFG["n_heads"],CFG["n_layers"],CFG["dropout"])
model.load_state_dict(ckpt["model"])
model.eval()

prompt=input("Ayurones> ")
ids=tokenizer.encode(prompt).ids
x=torch.tensor([ids],dtype=torch.long)
out=model.generate(x,max_new_tokens=120)
print(tokenizer.decode(out[0].tolist()))
