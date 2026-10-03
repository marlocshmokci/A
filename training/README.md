# Ayurones training

This directory contains a from-scratch educational Transformer training path.

## Pipeline

1. Put legally usable .txt/.jsonl training data into `data/raw/`.
2. Run `python prepare.py`.
3. Run `python tokenizer.py`.
4. Run `python train.py`.
5. Run `python infer.py`.

The starter configuration is intentionally small so it can be tested on modest hardware. Increase `d_model`, `n_layers`, sequence length, dataset size and training compute for a serious model.

For production-scale instruction tuning, add a separate LoRA/QLoRA stage rather than training all weights after every chat message.

Never collect or train on private user conversations automatically. Use data you have the right to process and filter it before training.
