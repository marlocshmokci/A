const TEXT_LIMIT = 20;
const MEDIA_LIMIT = 8;
const MODEL_IDS = {
  ayuron3: "gemini-3.5-flash-lite",
  ayurones: "gemini-3.8-flash",
  ayurones6: "gemini-4-argon"
};
const SYSTEM = "Ты — Ayuron, дружелюбный AI-помощник. Отвечай на языке пользователя, понятно и по делу. Не утверждай, что умеешь то, чего не умеешь.";

function reply(data, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: {
    "content-type":"application/json; charset=utf-8",
    "access-control-allow-origin":"*",
    "access-control-allow-methods":"GET, POST, OPTIONS",
    "access-control-allow-headers":"content-type"
  }});
}
const today = () => new Date().toISOString().slice(0,10);
const limits = row => {
  const current = row && row.day === today();
  return { textUsed:current?Number(row.text_used||0):0,
    mediaUsed:current?Number(row.media_used||0):0,
    textLimit:TEXT_LIMIT, mediaLimit:MEDIA_LIMIT, resetsAt:"00:00 UTC" };
};
async function ensureDb(env) {
  if (!env.DB) throw new Error("Missing D1 binding DB; see README.");
  await env.DB.prepare("CREATE TABLE IF NOT EXISTS usage (user_id TEXT PRIMARY KEY, day TEXT NOT NULL, text_used INTEGER NOT NULL DEFAULT 0, media_used INTEGER NOT NULL DEFAULT 0)").run();
}
async function readUsage(env,id) {
  await ensureDb(env);
  const row = await env.DB.prepare("SELECT user_id,day,text_used,media_used FROM usage WHERE user_id=?").bind(id).first();
  return limits(row);
}
async function reserve(env,id,kind) {
  await ensureDb(env);
  const sql = kind==="text"
    ? "INSERT INTO usage(user_id,day,text_used,media_used) VALUES(?,?,1,0) ON CONFLICT(user_id) DO UPDATE SET text_used=CASE WHEN usage.day!=excluded.day THEN 1 ELSE usage.text_used+1 END, media_used=CASE WHEN usage.day!=excluded.day THEN 0 ELSE usage.media_used END, day=excluded.day WHERE usage.day!=excluded.day OR usage.text_used<"+TEXT_LIMIT+" RETURNING user_id,day,text_used,media_used"
    : "INSERT INTO usage(user_id,day,text_used,media_used) VALUES(?,?,0,1) ON CONFLICT(user_id) DO UPDATE SET text_used=CASE WHEN usage.day!=excluded.day THEN 0 ELSE usage.text_used END, media_used=CASE WHEN usage.day!=excluded.day THEN 1 ELSE usage.media_used+1 END, day=excluded.day WHERE usage.day!=excluded.day OR usage.media_used<"+MEDIA_LIMIT+" RETURNING user_id,day,text_used,media_used";
  const row = await env.DB.prepare(sql).bind(id,today()).first();
  return row ? {allowed:true,usage:limits(row)} : {allowed:false,usage:await readUsage(env,id)};
}
function userId(body) {
  if(!body||typeof body.userId!=="string")return "";
  const id=body.userId.trim();return id.length>=8&&id.length<=100?id:"";
}
function contentsFromHistory(history) {
  if(!Array.isArray(history))return [];
  return history.slice(-10).flatMap(x=>{
    if(!x||typeof x.text!=="string"||!x.text.trim())return [];
    if(x.role==="user")return [{role:"user",parts:[{text:x.text.slice(0,8000)}]}];
    if(x.role==="assistant"||x.role==="model")return [{role:"model",parts:[{text:x.text.slice(0,8000)}]}];
    return [];
  });
}
async function callGemini(env,model,body) {
  if(!env.GEMINI_API_KEY)return {ok:false,status:500,error:"Worker secret GEMINI_API_KEY is not configured."};
  const r=await fetch("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent",{
    method:"POST",headers:{"content-type":"application/json","x-goog-api-key":env.GEMINI_API_KEY},body:JSON.stringify(body)
  });
  let data={};try{data=await r.json();}catch(_){}
  if(!r.ok) {
    const detail=data&&data.error&&data.error.message;
    return {ok:false,status:r.status===429?429:502,error:r.status===429
      ?"Gemini rate limit reached. Try again later."
      :"Gemini API request failed. Check model access and API settings."+(detail?" "+String(detail).slice(0,160):"")};
  }
  return {ok:true,data};
}
function partsOf(data) {
  const c=data&&Array.isArray(data.candidates)?data.candidates[0]:null;
  return c&&c.content&&Array.isArray(c.content.parts)?c.content.parts:[];
}
export default {
  async fetch(request,env) {
    if(request.method==="OPTIONS")return reply({ok:true});
    const url=new URL(request.url);
    if(request.method==="GET"&&url.pathname==="/health")return reply({ok:true,service:"Ayuron API",version:"1.0.0"});
    if(request.method==="GET"&&url.pathname==="/usage") {
      const id=(url.searchParams.get("userId")||"").trim();
      if(id.length<8||id.length>100)return reply({ok:false,error:"Invalid userId"},400);
      try{return reply({ok:true,usage:await readUsage(env,id)});}
      catch(e){return reply({ok:false,error:e.message||"D1 error"},500);}
    }
    const path=url.pathname;
    if(request.method!=="POST"||!["/chat","/analyze","/generate-image"].includes(path))
      return reply({ok:false,error:"Not found"},404);
    const length=Number(request.headers.get("content-length")||0);
    if(length>9000000)return reply({ok:false,error:"Request is too large (max 9 MB)"},413);
    let b;try{b=await request.json();}catch(_){return reply({ok:false,error:"Invalid JSON"},400);}
    const id=userId(b);if(!id)return reply({ok:false,error:"Invalid userId"},400);
    if(path==="/chat") {
      if(typeof b.message!=="string"||!b.message.trim()||b.message.length>12000)return reply({ok:false,error:"Message must contain 1–12000 characters"},400);
      if(!Object.prototype.hasOwnProperty.call(MODEL_IDS,b.model))return reply({ok:false,error:"Unknown model"},400);
    } else if(path==="/analyze") {
      if(typeof b.data!=="string"||b.data.length>8200000)return reply({ok:false,error:"Image missing or too large (max about 6 MB)"},400);
      if(!["image/jpeg","image/png","image/webp"].includes(b.mimeType))return reply({ok:false,error:"Supported image types: JPG, PNG, WebP"},400);
    } else if(typeof b.prompt!=="string"||!b.prompt.trim()||b.prompt.length>3000) {
      return reply({ok:false,error:"Prompt must contain 1–3000 characters"},400);
    }
    const kind=path==="/chat"?"text":"media";
    let reserved;try{reserved=await reserve(env,id,kind);}
    catch(e){return reply({ok:false,error:e.message||"Usage database unavailable"},500);}
    if(!reserved.allowed)return reply({ok:false,error:kind==="text"
      ?"Daily allowance used: 20 text requests. Resets at 00:00 UTC."
      :"Daily allowance used: 8 media requests. Resets at 00:00 UTC.",
      usage:reserved.usage},429);

    if(path==="/chat") {
      const contents=contentsFromHistory(b.history);
      contents.push({role:"user",parts:[{text:b.message.trim()}]});
      const r=await callGemini(env,MODEL_IDS[b.model],{
        systemInstruction:{parts:[{text:SYSTEM}]},contents,
        generationConfig:{temperature:0.7,maxOutputTokens:4096}
      });
      if(!r.ok)return reply({ok:false,error:r.error,usage:reserved.usage},r.status);
      const answer=partsOf(r.data).filter(p=>typeof p.text==="string").map(p=>p.text).join("\n").trim();
      return reply({ok:true,answer:answer||"(Модель вернула пустой ответ.)",usage:reserved.usage});
    }
    if(path==="/analyze") {
      const model=MODEL_IDS[b.model]||MODEL_IDS.ayurones;
      const r=await callGemini(env,model,{
        systemInstruction:{parts:[{text:SYSTEM}]},
        contents:[{role:"user",parts:[
          {text:String(b.prompt||"Опиши изображение").slice(0,3000)},
          {inlineData:{mimeType:b.mimeType,data:b.data}}
        ]}],
        generationConfig:{temperature:0.5,maxOutputTokens:2048}
      });
      if(!r.ok)return reply({ok:false,error:r.error,usage:reserved.usage},r.status);
      const answer=partsOf(r.data).filter(p=>typeof p.text==="string").map(p=>p.text).join("\n").trim();
      return reply({ok:true,answer:answer||"Изображение обработано.",usage:reserved.usage});
    }
    const r=await callGemini(env,"gemini-3.1-flash-image",{
      systemInstruction:{parts:[{text:"Generate the image requested by the user and return the image with a short description."}]},
      contents:[{role:"user",parts:[{text:b.prompt.trim()}]}],
      generationConfig:{responseModalities:["TEXT","IMAGE"]}
    });
    if(!r.ok)return reply({ok:false,error:r.error,usage:reserved.usage},r.status);
    const ps=partsOf(r.data),im=ps.find(p=>p.inlineData&&typeof p.inlineData.data==="string");
    const text=ps.filter(p=>typeof p.text==="string").map(p=>p.text).join("\n").trim();
    if(!im)return reply({ok:false,error:text||"The model did not return an image. Check image model access.",usage:reserved.usage},502);
    return reply({ok:true,answer:text||"Готово! Вот изображение.",image:im.inlineData.data,
      mimeType:im.inlineData.mimeType||"image/png",usage:reserved.usage});
  }
};
