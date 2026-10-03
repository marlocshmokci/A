import express from "express";
import http from "http";
import { WebSocketServer } from "ws";
import fs from "fs";
import crypto from "crypto";

const app=express(); app.use(express.json({limit:"2mb"}));
const DATA="./data.json";
const db=fs.existsSync(DATA)?JSON.parse(fs.readFileSync(DATA,"utf8")):{
  users:[{id:"u1",handle:"alexm",name:"Alex Morgan",bio:"Building quietly. Sharing ideas loudly."}],
  posts:[], chats:[], messages:[], channels:[], notifications:[]
};
const save=()=>fs.writeFileSync(DATA,JSON.stringify(db,null,2));

app.get("/health",(req,res)=>res.json({ok:true,name:"NEXA",time:new Date().toISOString()}));
app.post("/v1/auth/register",(req,res)=>{const {handle,name}=req.body;if(!handle)return res.status(400).json({error:"handle required"});if(db.users.some(x=>x.handle===handle))return res.status(409).json({error:"handle taken"});const u={id:crypto.randomUUID(),handle,name:name||handle,bio:""};db.users.push(u);save();res.json({user:u,token:u.id});});
app.post("/v1/auth/login",(req,res)=>{const u=db.users.find(x=>x.handle===req.body.handle);if(!u)return res.status(404).json({error:"not found"});res.json({user:u,token:u.id});});

app.get("/v1/feed",(req,res)=>res.json({items:db.posts.slice().reverse(),nextCursor:null}));
app.post("/v1/posts",(req,res)=>{const p={id:crypto.randomUUID(),userId:req.body.userId||"u1",text:String(req.body.text||""),createdAt:new Date().toISOString(),likes:0,replies:0,reposts:0};if(!p.text.trim())return res.status(400).json({error:"text required"});db.posts.push(p);save();broadcast({type:"post.new",post:p});res.status(201).json(p);});
app.post("/v1/posts/:id/like",(req,res)=>{const p=db.posts.find(x=>x.id===req.params.id);if(!p)return res.sendStatus(404);p.likes++;save();broadcast({type:"post.like",postId:p.id,likes:p.likes});res.json(p);});
app.post("/v1/posts/:id/repost",(req,res)=>{const p=db.posts.find(x=>x.id===req.params.id);if(!p)return res.sendStatus(404);p.reposts++;save();res.json(p);});

app.get("/v1/chats",(req,res)=>res.json(db.chats));
app.get("/v1/chats/:id/messages",(req,res)=>res.json({items:db.messages.filter(x=>x.chatId===req.params.id),nextCursor:null}));
app.post("/v1/chats/:id/messages",(req,res)=>{const m={id:crypto.randomUUID(),chatId:req.params.id,senderId:req.body.senderId||"u1",text:String(req.body.text||""),createdAt:new Date().toISOString()};if(!m.text.trim())return res.status(400).json({error:"text required"});db.messages.push(m);save();broadcast({type:"message.new",message:m});res.status(201).json(m);});
app.post("/v1/messages/:id/react",(req,res)=>{const m=db.messages.find(x=>x.id===req.params.id);if(!m)return res.sendStatus(404);m.reaction=String(req.body.reaction||"");save();broadcast({type:"message.reaction",messageId:m.id,reaction:m.reaction});res.json(m);});

app.get("/v1/channels",(req,res)=>res.json(db.channels));
app.get("/v1/channels/:id",(req,res)=>{const c=db.channels.find(x=>x.id===req.params.id);c?res.json(c):res.sendStatus(404);});
app.post("/v1/channels/:id/join",(req,res)=>{const c=db.channels.find(x=>x.id===req.params.id);if(!c)return res.sendStatus(404);c.members=(c.members||0)+1;save();res.json(c);});

app.get("/v1/search",(req,res)=>{const q=String(req.query.q||"").toLowerCase();res.json({posts:db.posts.filter(x=>x.text.toLowerCase().includes(q)),users:db.users.filter(x=>(x.name+" "+x.handle).toLowerCase().includes(q)),channels:db.channels.filter(x=>(x.name+" "+x.handle).toLowerCase().includes(q))});});

const server=http.createServer(app);const wss=new WebSocketServer({server,path:"/v1/realtime"});const clients=new Set();wss.on("connection",ws=>{clients.add(ws);ws.send(JSON.stringify({type:"ready"}));ws.on("close",()=>clients.delete(ws));});
function broadcast(x){const s=JSON.stringify(x);for(const ws of clients)if(ws.readyState===1)ws.send(s);}
const port=process.env.PORT||8787;server.listen(port,()=>console.log("NEXA server listening on "+port));
