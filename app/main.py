import os
import sqlite3
import math
import re
from pathlib import Path

import httpx
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

load_dotenv()

ROOT = Path(__file__).resolve().parent.parent
DB_PATH = Path(os.getenv("DB_PATH", str(ROOT / "data" / "ayurones.db")))
DB_PATH.parent.mkdir(parents=True, exist_ok=True)

OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://127.0.0.1:11434").rstrip("/")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "qwen3:8b")
MAX_CONTEXT_MESSAGES = int(os.getenv("MAX_CONTEXT_MESSAGES", "12"))
KNOWLEDGE_TOP_K = int(os.getenv("KNOWLEDGE_TOP_K", "6"))

app = FastAPI(title="Ayurones AI", version="1.0.0")
app.mount("/static", StaticFiles(directory=str(ROOT / "web")), name="static")


class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=20000)
    conversation_id: str = Field(default="default", min_length=1, max_length=120)


class LearnRequest(BaseModel):
    title: str = Field(default="Untitled", max_length=300)
    text: str = Field(min_length=1, max_length=200000)


def db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    with db() as conn:
        conn.execute("""
        CREATE TABLE IF NOT EXISTS messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            conversation_id TEXT NOT NULL,
            role TEXT NOT NULL,
            content TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
        """)
        conn.execute("""
        CREATE TABLE IF NOT EXISTS knowledge (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
        """)


def tokenize(text):
    return set(re.findall(r"[\wа-яА-ЯёЁ-]{2,}", text.lower()))


def knowledge_search(query, limit=KNOWLEDGE_TOP_K):
    q = tokenize(query)
    if not q:
        return []
    with db() as conn:
        rows = conn.execute("SELECT id, title, content FROM knowledge ORDER BY id DESC").fetchall()

    scored = []
    for row in rows:
        words = tokenize(row["content"])
        if not words:
            continue
        overlap = len(q & words)
        if overlap:
            score = overlap / math.sqrt(len(words))
            scored.append((score, row))

    scored.sort(key=lambda x: x[0], reverse=True)
    return [row for _, row in scored[:limit]]


def save_message(conversation_id, role, content):
    with db() as conn:
        conn.execute(
            "INSERT INTO messages(conversation_id, role, content) VALUES (?, ?, ?)",
            (conversation_id, role, content),
        )


def recent_messages(conversation_id):
    with db() as conn:
        rows = conn.execute(
            "SELECT role, content FROM messages WHERE conversation_id=? ORDER BY id DESC LIMIT ?",
            (conversation_id, MAX_CONTEXT_MESSAGES),
        ).fetchall()
    return list(reversed(rows))


async def ollama_chat(messages):
    payload = {
        "model": OLLAMA_MODEL,
        "messages": messages,
        "stream": False,
        "options": {"temperature": 0.65, "num_ctx": 8192},
    }
    async with httpx.AsyncClient(timeout=300) as client:
        response = await client.post(f"{OLLAMA_BASE_URL}/api/chat", json=payload)
        response.raise_for_status()
        data = response.json()
    return data.get("message", {}).get("content", "").strip()


@app.on_event("startup")
def startup():
    init_db()


@app.get("/")
def index():
    return FileResponse(ROOT / "web" / "index.html")


@app.get("/api/status")
async def status():
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            response = await client.get(f"{OLLAMA_BASE_URL}/api/tags")
            response.raise_for_status()
            models = response.json().get("models", [])
        return {"online": True, "model": OLLAMA_MODEL, "models": [m.get("name") for m in models]}
    except Exception:
        return {"online": False, "model": OLLAMA_MODEL, "models": []}


@app.post("/api/chat")
async def chat(request: ChatRequest):
    knowledge = knowledge_search(request.message)
    knowledge_text = "\n\n".join(
        f"[{item['title']}]\n{item['content'][:5000]}" for item in knowledge
    )

    system = """Ты — Ayurones, локальный искусственный интеллект.
Отвечай точно, ясно и содержательно.
Используй русский язык, если пользователь пишет по-русски.
Не выдумывай факты. Если данных недостаточно — прямо скажи об этом.
У тебя есть долговременная база знаний. Используй её как дополнительный источник,
но не выдавай найденный текст за собственные воспоминания.
Пользователь может обучать систему новыми материалами; не считай каждое сообщение
пользователя истинным фактом для базы знаний."""

    if knowledge_text:
        system += "\n\nРелевантные материалы из базы знаний:\n" + knowledge_text

    history = recent_messages(request.conversation_id)
    messages = [{"role": "system", "content": system}]
    messages.extend({"role": row["role"], "content": row["content"]} for row in history)
    messages.append({"role": "user", "content": request.message})

    save_message(request.conversation_id, "user", request.message)

    try:
        answer = await ollama_chat(messages)
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=503,
            detail=f"Локальная модель недоступна. Запустите Ollama и установите {OLLAMA_MODEL}.",
        ) from exc

    save_message(request.conversation_id, "assistant", answer)
    return {"answer": answer, "model": OLLAMA_MODEL}


@app.post("/api/learn")
def learn(request: LearnRequest):
    with db() as conn:
        conn.execute("INSERT INTO knowledge(title, content) VALUES (?, ?)", (request.title, request.text))
    return {"ok": True, "message": "Материал добавлен в базу знаний."}


@app.get("/api/knowledge")
def list_knowledge():
    with db() as conn:
        rows = conn.execute("SELECT id, title, created_at FROM knowledge ORDER BY id DESC").fetchall()
    return {"items": [dict(row) for row in rows]}


@app.delete("/api/knowledge/{item_id}")
def delete_knowledge(item_id: int):
    with db() as conn:
        conn.execute("DELETE FROM knowledge WHERE id=?", (item_id,))
    return {"ok": True}


@app.get("/api/history/{conversation_id}")
def history(conversation_id: str):
    with db() as conn:
        rows = conn.execute(
            "SELECT role, content, created_at FROM messages WHERE conversation_id=? ORDER BY id ASC",
            (conversation_id,),
        ).fetchall()
    return {"messages": [dict(row) for row in rows]}
