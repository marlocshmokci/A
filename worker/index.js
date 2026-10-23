export default {
  async fetch(request, env) {
    const cors = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
      "Content-Type": "application/json; charset=utf-8"
    };
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    if (new URL(request.url).pathname !== "/chat") return Response.json({ error: "Not found" }, { status: 404, headers: cors });
    if (request.method !== "POST") return Response.json({ error: "Method not allowed" }, { status: 405, headers: cors });
    if (!env.GROQ_API_KEY) return Response.json({ error: "Server is missing GROQ_API_KEY secret" }, { status: 503, headers: cors });
    try {
      const body = await request.json();
      if (!Array.isArray(body.messages) || body.messages.length < 1) return Response.json({ error: "messages must be a non-empty array" }, { status: 400, headers: cors });
      const messages = body.messages.slice(-30).map(m => ({
        role: ["user", "assistant"].includes(m.role) ? m.role : "user",
        content: String(m.content || "").slice(0, 12000)
      }));
      const upstream = await fetch("https://api.groq.com/openai/v1/chat/completions", {
        method: "POST",
        headers: { "Authorization": "Bearer " + env.GROQ_API_KEY, "Content-Type": "application/json" },
        body: JSON.stringify({ model: String(body.model || "llama-3.3-70b-versatile").slice(0, 100), messages, temperature: 0.7, max_tokens: 2048 })
      });
      const data = await upstream.json();
      if (!upstream.ok) return Response.json({ error: data.error?.message || "Groq API error" }, { status: upstream.status, headers: cors });
      return Response.json({ reply: data.choices?.[0]?.message?.content || "" }, { headers: cors });
    } catch {
      return Response.json({ error: "Invalid request or upstream service unavailable" }, { status: 400, headers: cors });
    }
  }
};