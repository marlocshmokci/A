# A — Ayurones AI

Локальное AI-приложение без OpenAI API.

Стек: FastAPI + SQLite + Ollama + Web UI. Модель запускается локально.

## Запуск

1. Установите Python 3.11+ и Ollama.
2. Выполните: ollama pull qwen3:8b
3. Выполните: pip install -r requirements.txt
4. Запустите: uvicorn app.main:app --host 0.0.0.0 --port 8000
5. Откройте http://localhost:8000

## Обучение

Кнопка «Обучить» добавляет новые материалы в базу знаний. Runtime использует RAG, поэтому знания можно расширять без полного переобучения весов после каждого сообщения. Для настоящего LoRA/QLoRA обучения подготовлена папка training.

OpenAI API не используется.