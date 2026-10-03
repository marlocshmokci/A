const messages=document.querySelector("#messages");
const input=document.querySelector("#input");
const form=document.querySelector("#chatForm");
const status=document.querySelector("#status");
const modelName=document.querySelector("#modelName");
const dialog=document.querySelector("#learnDialog");
const conversationId=crypto.randomUUID();

function addMessage(role,content){
 const welcome=document.querySelector(".welcome");if(welcome)welcome.remove();
 const row=document.createElement("div");row.className="message "+role;
 const bubble=document.createElement("div");bubble.className="bubble";bubble.textContent=content;
 row.appendChild(bubble);messages.appendChild(row);messages.scrollTop=messages.scrollHeight;return bubble;
}
async function checkStatus(){
 try{const r=await fetch("/api/status");const d=await r.json();status.textContent=d.online?"● Модель подключена":"○ Ollama не запущен";modelName.textContent=d.model||"локальная модель"}
 catch{status.textContent="○ Сервер недоступен"}
}
form.addEventListener("submit",async e=>{
 e.preventDefault();const text=input.value.trim();if(!text)return;
 addMessage("user",text);input.value="";input.style.height="auto";const bubble=addMessage("assistant","Думаю…");
 try{const r=await fetch("/api/chat",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({message:text,conversation_id:conversationId})});const d=await r.json();if(!r.ok)throw new Error(d.detail||"Ошибка");bubble.textContent=d.answer}
 catch(err){bubble.textContent="Ошибка: "+err.message}
});
input.addEventListener("input",()=>{input.style.height="auto";input.style.height=Math.min(input.scrollHeight,180)+"px"});
document.querySelector("#menu").addEventListener("click",()=>document.body.classList.toggle("sidebar-open"));
document.querySelector("#newChat").addEventListener("click",()=>location.reload());
document.querySelector("#learnOpen").addEventListener("click",()=>dialog.showModal());
document.querySelector("#learnClose").addEventListener("click",()=>dialog.close());
document.querySelector("#learnForm").addEventListener("submit",async e=>{
 e.preventDefault();const title=document.querySelector("#learnTitle").value.trim()||"Untitled";const text=document.querySelector("#learnText").value.trim();if(!text)return;
 const r=await fetch("/api/learn",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({title,text})});
 if(r.ok){dialog.close();document.querySelector("#learnTitle").value="";document.querySelector("#learnText").value="";alert("Материал добавлен в знания Ayurones.")}else alert("Не удалось добавить материал.")
});
checkStatus();