package ai.ayurones.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Store {
    private static final String PREF = "ayurones";
    private Store() {}

    static SharedPreferences p(Context c) { return c.getSharedPreferences(PREF, Context.MODE_PRIVATE); }
    static int points(Context c) { return p(c).getInt("points", 250); }
    static void addPoints(Context c,int amount){p(c).edit().putInt("points",Math.max(0,points(c)+amount)).apply();}

    static Set<String> contacts(Context c){
        Set<String>s=p(c).getStringSet("contacts",null);return s==null?new HashSet<>():new HashSet<>(s);
    }
    static void addContact(Context c,String name){name=name.trim();if(name.isEmpty())return;Set<String>s=contacts(c);s.add(name);p(c).edit().putStringSet("contacts",s).apply();}
    static void removeContact(Context c,String name){Set<String>s=contacts(c);s.remove(name);p(c).edit().putStringSet("contacts",s).apply();}

    static final class Message{
        final String id; String text; long time; boolean mine; String fileUri,fileName; long fileSize; boolean edited; String reply,reaction;
        Message(String id){this.id=id;}
        JSONObject toJson(){JSONObject o=new JSONObject();try{o.put("id",id);o.put("text",text==null?"":text);o.put("time",time);o.put("mine",mine);o.put("fileUri",fileUri==null?"":fileUri);o.put("fileName",fileName==null?"":fileName);o.put("fileSize",fileSize);o.put("edited",edited);o.put("reply",reply==null?"":reply);o.put("reaction",reaction==null?"":reaction);}catch(Exception ignored){}return o;}
        static Message fromJson(JSONObject o){Message m=new Message(o.optString("id",UUID.randomUUID().toString()));m.text=o.optString("text","");m.time=o.optLong("time",System.currentTimeMillis());m.mine=o.optBoolean("mine",true);m.fileUri=o.optString("fileUri","");m.fileName=o.optString("fileName","");m.fileSize=o.optLong("fileSize",0);m.edited=o.optBoolean("edited",false);m.reply=o.optString("reply","");m.reaction=o.optString("reaction","");return m;}
    }

    static List<Message> messagesDetailed(Context c,String chat){
        List<Message>out=new ArrayList<>();try{JSONArray a=new JSONArray(p(c).getString("chat_"+chat,"[]"));for(int i=0;i<a.length();i++){Object v=a.get(i);if(v instanceof JSONObject)out.add(Message.fromJson((JSONObject)v));else{Message m=new Message(UUID.randomUUID().toString());m.text=String.valueOf(v);m.time=System.currentTimeMillis();m.mine=true;out.add(m);}}}catch(Exception ignored){}return out;
    }
    static List<String> messages(Context c,String chat){List<String>out=new ArrayList<>();for(Message m:messagesDetailed(c,chat))out.add(m.text==null?"":m.text);return out;}

    static String previewText(Message m){if(m.fileName!=null&&!m.fileName.isEmpty())return "📎 "+m.fileName;return m.text==null||m.text.isEmpty()?"Сообщение":m.text;}
    private static void writeMessages(Context c,String chat,List<Message>all){
        JSONArray a=new JSONArray();for(Message m:all)a.put(m.toJson());
        p(c).edit().putString("chat_"+chat,a.toString()).putString("preview_"+chat,all.isEmpty()?"":previewText(all.get(all.size()-1))).putLong("last_time_"+chat,all.isEmpty()?0:all.get(all.size()-1).time).apply();
    }
    static String addMessage(Context c,String chat,String text,String reply){List<Message>a=messagesDetailed(c,chat);Message m=new Message(UUID.randomUUID().toString());m.text=text.trim();m.time=System.currentTimeMillis();m.mine=true;m.reply=reply==null?"":reply;a.add(m);writeMessages(c,chat,a);p(c).edit().putInt("unread_"+chat,0).apply();return m.id;}
    static String addIncoming(Context c,String chat,String text){List<Message>a=messagesDetailed(c,chat);Message m=new Message(UUID.randomUUID().toString());m.text=text.trim();m.time=System.currentTimeMillis();m.mine=false;a.add(m);writeMessages(c,chat,a);p(c).edit().putInt("unread_"+chat,unread(c,chat)+1).apply();return m.id;}
    static String addFileMessage(Context c,String chat,Uri uri,String name,long size,String reply){List<Message>a=messagesDetailed(c,chat);Message m=new Message(UUID.randomUUID().toString());m.time=System.currentTimeMillis();m.mine=true;m.fileUri=uri==null?"":uri.toString();m.fileName=name==null?"":name;m.fileSize=size;m.reply=reply==null?"":reply;a.add(m);writeMessages(c,chat,a);return m.id;}
    static void editMessage(Context c,String chat,String id,String text){List<Message>a=messagesDetailed(c,chat);for(Message m:a)if(m.id.equals(id)){m.text=text.trim();m.edited=true;break;}writeMessages(c,chat,a);}
    static void deleteMessage(Context c,String chat,String id){List<Message>a=messagesDetailed(c,chat);for(int i=a.size()-1;i>=0;i--)if(a.get(i).id.equals(id)){a.remove(i);break;}writeMessages(c,chat,a);}
    static void toggleReaction(Context c,String chat,String id,String emoji){List<Message>a=messagesDetailed(c,chat);for(Message m:a)if(m.id.equals(id)){m.reaction=emoji.equals(m.reaction)?"":emoji;break;}writeMessages(c,chat,a);}

    static String preview(Context c,String chat){String d=draft(c,chat);if(!d.isEmpty())return "Черновик: "+d;String s=p(c).getString("preview_"+chat,"");return s.isEmpty()?"Новый чат":s;}
    static long lastTime(Context c,String chat){return p(c).getLong("last_time_"+chat,0);}
    static int unread(Context c,String chat){return p(c).getInt("unread_"+chat,0);}
    static void markRead(Context c,String chat){p(c).edit().putInt("unread_"+chat,0).apply();}
    static boolean pinned(Context c,String chat){return p(c).getBoolean("pinned_"+chat,false);}
    static void setPinned(Context c,String chat,boolean v){p(c).edit().putBoolean("pinned_"+chat,v).apply();}
    static boolean muted(Context c,String chat){return p(c).getBoolean("muted_"+chat,false);}
    static void setMuted(Context c,String chat,boolean v){p(c).edit().putBoolean("muted_"+chat,v).apply();}
    static boolean archived(Context c,String chat){return p(c).getBoolean("archived_"+chat,false);}
    static void setArchived(Context c,String chat,boolean v){p(c).edit().putBoolean("archived_"+chat,v).apply();}
    static void clearChat(Context c,String chat){p(c).edit().remove("chat_"+chat).remove("preview_"+chat).remove("last_time_"+chat).remove("unread_"+chat).remove("pinned_"+chat).remove("muted_"+chat).remove("archived_"+chat).remove("draft_"+chat).apply();}
    static String draft(Context c,String chat){return p(c).getString("draft_"+chat,"");}
    static void saveDraft(Context c,String chat,String v){p(c).edit().putString("draft_"+chat,v==null?"":v).apply();}

    static String avatarName(Context c){return p(c).getString("name","Ayurones User");}
    static void setProfile(Context c,String name,String username,String bio){p(c).edit().putString("name",name==null||name.trim().isEmpty()?"Ayurones User":name.trim()).putString("username",username==null?"":username.trim().replace(" ","_")).putString("bio",bio==null?"":bio.trim()).apply();}
    static String username(Context c){return p(c).getString("username","ayurones_user");}
    static String bio(Context c){return p(c).getString("bio","");}

    static void dailyBonus(Context c){Calendar now=Calendar.getInstance();String today=now.get(Calendar.YEAR)+"-"+now.get(Calendar.DAY_OF_YEAR);String last=p(c).getString("daily","");if(!today.equals(last)){addPoints(c,20);p(c).edit().putString("daily",today).apply();}}
    static Uri avatar(Context c){String v=p(c).getString("avatar","");return v.isEmpty()?null:Uri.parse(v);}
    static void setAvatar(Context c,Uri uri){if(uri==null)return;try{c.getContentResolver().takePersistableUriPermission(uri,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}p(c).edit().putString("avatar",uri.toString()).apply();}

    static boolean owned(Context c,String id){return p(c).getBoolean("owned_"+id,false);}
    static void buy(Context c,String id){p(c).edit().putBoolean("owned_"+id,true).apply();}
    static String equipped(Context c){return p(c).getString("decoration","");}
    static void equip(Context c,String id){p(c).edit().putString("decoration",id).apply();}
    static boolean unlimited(Context c){return p(c).getBoolean("unlimited",false);}
    static void setUnlimited(Context c,boolean v){p(c).edit().putBoolean("unlimited",v).apply();}
    static void grantAllDecorations(Context c,Decoration[]items){SharedPreferences.Editor e=p(c).edit();for(Decoration d:items)e.putBoolean("owned_"+d.id,true);e.apply();}
    static boolean sendSound(Context c){return p(c).getBoolean("send_sound",true);}
    static void setSendSound(Context c,boolean v){p(c).edit().putBoolean("send_sound",v).apply();}
    static String sound(Context c){return p(c).getString("sound","send_chime");}
    static void setSound(Context c,String id){p(c).edit().putString("sound",id).apply();}

    static String exportBackup(Context c){JSONObject out=new JSONObject();try{JSONArray contactsJson=new JSONArray();for(String n:contacts(c))contactsJson.put(n);out.put("contacts",contactsJson);for(Map.Entry<String,?>e:p(c).getAll().entrySet()){String k=e.getKey();if(k.equals("contacts"))continue;Object v=e.getValue();if(v instanceof Set){JSONArray a=new JSONArray();for(Object item:(Set<?>)v)a.put(String.valueOf(item));out.put(k,a);}else out.put(k,v);}}catch(Exception ignored){}return out.toString();}
    static boolean importBackup(Context c,String json){try{JSONObject in=new JSONObject(json);SharedPreferences.Editor e=p(c).edit().clear();JSONArray cj=in.optJSONArray("contacts");if(cj!=null){Set<String>s=new HashSet<>();for(int i=0;i<cj.length();i++)s.add(cj.optString(i));e.putStringSet("contacts",s);}JSONArray names=in.names();if(names!=null)for(int i=0;i<names.length();i++){String k=names.optString(i);if(k.equals("contacts"))continue;Object v=in.get(k);if(v instanceof Boolean)e.putBoolean(k,(Boolean)v);else if(v instanceof Integer)e.putInt(k,(Integer)v);else if(v instanceof Long)e.putLong(k,(Long)v);else if(v instanceof Double)e.putLong(k,((Double)v).longValue());else if(v instanceof String)e.putString(k,(String)v);else if(v instanceof JSONArray)e.putString(k,v.toString());}e.apply();return true;}catch(Exception ex){return false;}}

    static final class Decoration{final String id,title,icon;final int cost;final String detail;Decoration(String id,String title,String icon,int cost,String detail){this.id=id;this.title=title;this.icon=icon;this.cost=cost;this.detail=detail;}}
}
