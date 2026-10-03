from pathlib import Path
from tokenizers import Tokenizer, models, pre_tokenizers, trainers

ROOT = Path(__file__).resolve().parent
corpus = ROOT / "data" / "corpus.txt"
out = ROOT / "tokenizer.json"

if not corpus.exists():
    raise SystemExit("Run: python prepare.py first")

tokenizer = Tokenizer(models.BPE(unk_token="<unk>"))
tokenizer.pre_tokenizer = pre_tokenizers.ByteLevel(add_prefix_space=False)
trainer = trainers.BpeTrainer(
    vocab_size=16000,
    min_frequency=2,
    special_tokens=["<pad>", "<unk>", "<bos>", "<eos>"],
)
tokenizer.train([str(corpus)], trainer)
tokenizer.save(str(out))
print(f"Saved {out}")
