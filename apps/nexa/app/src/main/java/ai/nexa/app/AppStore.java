package ai.nexa.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AppStore {
    private final SharedPreferences p;
    public final ArrayList<Post> posts = new ArrayList<>();
    public final ArrayList<Chat> chats = new ArrayList<>();
    public final ArrayList<Channel> channels = new ArrayList<>();
    public final ArrayList<Notice> notices = new ArrayList<>();
    public String name = "Alex Morgan";
    public String handle = "alexm";
    public String bio = "Building quietly. Sharing ideas loudly.";
    public String accent = "#6C63FF";
    public String theme = "dark";
    public float textScale = 1f;
    public boolean compact = false;
    public boolean reduceMotion = false;
    public boolean showReadReceipts = true;
    public boolean allowMessages = true;
    public boolean notifyMessages = true;
    public boolean notifyMentions = true;
    public String serverUrl = "";

    public AppStore(Context c) {
        p = c.getSharedPreferences("nexa_state", Context.MODE_PRIVATE);
        load();
    }

    public void load() {
        name = p.getString("name", name); handle = p.getString("handle", handle); bio = p.getString("bio", bio);
        accent = p.getString("accent", accent); theme = p.getString("theme", theme);
        textScale = p.getFloat("scale", 1f); compact = p.getBoolean("compact", false);
        reduceMotion = p.getBoolean("motion", false); showReadReceipts = p.getBoolean("receipts", true);
        allowMessages = p.getBoolean("allowMessages", true); notifyMessages = p.getBoolean("notifyMessages", true);
        notifyMentions = p.getBoolean("notifyMentions", true); serverUrl = p.getString("serverUrl", "");
        try {
            posts.clear(); JSONArray a = new JSONArray(p.getString("posts", "[]"));
            for(int i=0;i<a.length();i++) posts.add(Post.from(a.getJSONObject(i)));
            chats.clear(); a = new JSONArray(p.getString("chats", "[]"));
            for(int i=0;i<a.length();i++) chats.add(Chat.from(a.getJSONObject(i)));
            channels.clear(); a = new JSONArray(p.getString("channels", "[]"));
            for(int i=0;i<a.length();i++) channels.add(Channel.from(a.getJSONObject(i)));
            notices.clear(); a = new JSONArray(p.getString("notices", "[]"));
            for(int i=0;i<a.length();i++) notices.add(Notice.from(a.getJSONObject(i)));
        } catch(Exception ignored) {}
        if(posts.isEmpty()) seed();
    }

    private void seed() {
        posts.add(new Post("p1","Maya Chen","mayac","Designing calm software is an engineering problem, not a mood board. Good defaults remove friction before users even notice it.","2h","184","31","12",false));
        posts.add(new Post("p2","Jon Bell","jonb","The best group chats have one magical property: nobody needs to ask for context twice.","5h","92","18","4",false));
        posts.add(new Post("p3","Nora Ali","nora","Hot take: a timeline should feel like a neighborhood, not an airport departure board. Small circles matter.","1d","341","57","22",true));
        posts.add(new Post("p4","Studio North","studionorth","We shipped our tiny photo drop today. No accounts, no ads, just a 24-hour room for friends.","2d","608","73","49",false));
        Chat a = new Chat("c1","Maya Chen","mayac","I sent the build. The new composer feels much faster.","12:41","2",false);
        a.messages.add(new Message("m1","Maya Chen","Perfect. The reactions are really nice too.","12:40",false));
        a.messages.add(new Message("m2","Alex Morgan","Nice. I’m testing the compact layout now.","12:41",true));
        chats.add(a);
        Chat b = new Chat("c2","Product Room","group","Owen: let's move the review to 18:00","11:08","5",true); b.messages.add(new Message("m3","Owen","Let's move the review to 18:00.","11:08",false)); chats.add(b);
        Chat c = new Chat("c3","Lena Park","lenap","Voice note • 0:18","Yesterday","",false); chats.add(c);
        channels.add(new Channel("ch1","NEXA Updates","nexaupdates","2.4K","Release notes, experiments and changelogs.","N"));
        channels.add(new Channel("ch2","Design Signals","designsignals","8.7K","Small ideas for better interfaces.","D"));
        channels.add(new Channel("ch3","Berlin Builders","berlinbuilders","1.1K","Meetups, launches and local projects.","B"));
        notices.add(new Notice("n1","Maya Chen liked your post.","12m","♡",false));
        notices.add(new Notice("n2","Jon Bell mentioned you in Product Room.","47m","@",false));
        notices.add(new Notice("n3","New message from Lena Park.","2h","✉",true));
        save();
    }

    public void save() {
        try {
            JSONArray a = new JSONArray(); for(Post x:posts) a.put(x.toJson());
            JSONArray b = new JSONArray(); for(Chat x:chats) b.put(x.toJson());
            JSONArray c = new JSONArray(); for(Channel x:channels) c.put(x.toJson());
            JSONArray d = new JSONArray(); for(Notice x:notices) d.put(x.toJson());
            p.edit().putString("posts",a.toString()).putString("chats",b.toString()).putString("channels",c.toString()).putString("notices",d.toString())
                .putString("name",name).putString("handle",handle).putString("bio",bio).putString("accent",accent).putString("theme",theme)
                .putFloat("scale",textScale).putBoolean("compact",compact).putBoolean("motion",reduceMotion).putBoolean("receipts",showReadReceipts)
                .putBoolean("allowMessages",allowMessages).putBoolean("notifyMessages",notifyMessages).putBoolean("notifyMentions",notifyMentions)
                .putString("serverUrl",serverUrl).apply();
        } catch(Exception ignored) {}
    }

    public static class Post {
        public String id,name,handle,text,time,likes,replies,reposts; public boolean liked;
        public Post(String i,String n,String h,String t,String tm,String l,String r,String rp,boolean lk){id=i;name=n;handle=h;text=t;time=tm;likes=l;replies=r;reposts=rp;liked=lk;}
        JSONObject toJson(){JSONObject o=new JSONObject(); try{o.put("id",id);o.put("name",name);o.put("handle",handle);o.put("text",text);o.put("time",time);o.put("likes",likes);o.put("replies",replies);o.put("reposts",reposts);o.put("liked",liked);}catch(Exception ignored){} return o;}
        static Post from(JSONObject o){return new Post(o.optString("id"),o.optString("name"),o.optString("handle"),o.optString("text"),o.optString("time"),o.optString("likes"),o.optString("replies"),o.optString("reposts"),o.optBoolean("liked"));}
    }
    public static class Chat {
        public String id,title,handle,preview,time,badge; public boolean online; public ArrayList<Message> messages=new ArrayList<>();
        public Chat(String i,String t,String h,String p,String tm,String b,boolean on){id=i;title=t;handle=h;preview=p;time=tm;badge=b;online=on;}
        JSONObject toJson(){JSONObject o=new JSONObject();try{o.put("id",id);o.put("title",title);o.put("handle",handle);o.put("preview",preview);o.put("time",time);o.put("badge",badge);o.put("online",online); JSONArray a=new JSONArray();for(Message m:messages)a.put(m.toJson());o.put("messages",a);}catch(Exception ignored){}return o;}
        static Chat from(JSONObject o){Chat c=new Chat(o.optString("id"),o.optString("title"),o.optString("handle"),o.optString("preview"),o.optString("time"),o.optString("badge"),o.optBoolean("online"));JSONArray a=o.optJSONArray("messages");if(a!=null)for(int i=0;i<a.length();i++)c.messages.add(Message.from(a.optJSONObject(i)));return c;}
    }
    public static class Message {
        public String id,sender,text,time; public boolean me; public String reaction="";
        public Message(String i,String s,String t,String tm,boolean m){id=i;sender=s;text=t;time=tm;me=m;}
        JSONObject toJson(){JSONObject o=new JSONObject();try{o.put("id",id);o.put("sender",sender);o.put("text",text);o.put("time",time);o.put("me",me);o.put("reaction",reaction);}catch(Exception ignored){}return o;}
        static Message from(JSONObject o){Message m=new Message(o.optString("id"),o.optString("sender"),o.optString("text"),o.optString("time"),o.optBoolean("me"));m.reaction=o.optString("reaction");return m;}
    }
    public static class Channel {
        public String id,name,handle,members,description,initial; public boolean joined;
        public Channel(String i,String n,String h,String m,String d,String in){id=i;name=n;handle=h;members=m;description=d;initial=in;joined=false;}
        JSONObject toJson(){JSONObject o=new JSONObject();try{o.put("id",id);o.put("name",name);o.put("handle",handle);o.put("members",members);o.put("description",description);o.put("initial",initial);o.put("joined",joined);}catch(Exception ignored){}return o;}
        static Channel from(JSONObject o){Channel c=new Channel(o.optString("id"),o.optString("name"),o.optString("handle"),o.optString("members"),o.optString("description"),o.optString("initial"));c.joined=o.optBoolean("joined");return c;}
    }
    public static class Notice {
        public String id,text,time,icon; public boolean unread;
        public Notice(String i,String t,String tm,String ic,boolean u){id=i;text=t;time=tm;icon=ic;unread=u;}
        JSONObject toJson(){JSONObject o=new JSONObject();try{o.put("id",id);o.put("text",text);o.put("time",time);o.put("icon",icon);o.put("unread",unread);}catch(Exception ignored){}return o;}
        static Notice from(JSONObject o){return new Notice(o.optString("id"),o.optString("text"),o.optString("time"),o.optString("icon"),o.optBoolean("unread"));}
    }
}
