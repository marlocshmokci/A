package ai.ayurones.messenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ChatActivity extends BaseActivity {
    private String chat;
    private LinearLayout messages;
    private EditText input;
    private LinearLayout replyBar;
    private TextView replyText;
    private String replyId = "";

    static Intent intent(Context c, String name) {
        Intent i = new Intent(c, ChatActivity.class);
        i.putExtra("chat", name);
        return i;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        chat = getIntent().getStringExtra("chat");
        if (chat == null) chat = "Сохранённые";
        Store.markRead(this, chat);
        build();
    }

    @Override protected void onResume() {
        super.onResume();
        Store.markRead(this, chat);
        if (messages != null) load();
    }

    private void build() {
        LinearLayout root = root();

        LinearLayout top = row();
        TextView back = Ui.text(this, "‹", 34);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        TextView name = Ui.text(this, chat, 19);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        TextView state = Ui.text(this, chat.equals("Сохранённые") ? "Личное пространство" : "Локальный чат", 11);
        state.setTextColor(Color.GRAY);
        titleBox.addView(name);
        titleBox.addView(state);
        top.addView(titleBox,new LinearLayout.LayoutParams(0,Ui.dp(this,64),1));

        TextView info = Ui.text(this, "⋮", 27);
        info.setGravity(Gravity.CENTER);
        info.setOnClickListener(v -> showChatMenu());
        top.addView(info,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));
        root.addView(top);
        root.addView(Ui.divider(this));

        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(Ui.dp(this,14),Ui.dp(this,10),Ui.dp(this,14),Ui.dp(this,10));
        scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        replyBar = row();
        replyBar.setPadding(Ui.dp(this,12),0,Ui.dp(this,8),0);
        replyBar.setBackground(Ui.bg(Color.rgb(14,14,14),Color.rgb(45,45,45),Ui.dp(this,14)));
        replyText = Ui.text(this,"",12);
        replyText.setTextColor(Color.LTGRAY);
        replyBar.addView(replyText,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1));
        TextView cancel = Ui.text(this,"×",24);
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(v -> clearReply());
        replyBar.addView(cancel,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));
        replyBar.setVisibility(View.GONE);
        root.addView(replyBar);

        LinearLayout composer = row();
        composer.setPadding(Ui.dp(this,4),Ui.dp(this,5),Ui.dp(this,4),Ui.dp(this,5));

        TextView attach=Ui.text(this,"＋",24);
        attach.setGravity(Gravity.CENTER);
        attach.setOnClickListener(v -> pickFile());
        composer.addView(attach,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,54)));

        input = new EditText(this);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setHint("Сообщение");
        input.setSingleLine(false);
        input.setMaxLines(5);
        input.setText(Store.draft(this,chat));
        input.setPadding(Ui.dp(this,16),Ui.dp(this,8),Ui.dp(this,16),Ui.dp(this,8));
        input.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(60,60,60),Ui.dp(this,22)));
        input.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){ Store.saveDraft(ChatActivity.this,chat,s.toString()); }
            public void afterTextChanged(android.text.Editable e){}
        });
        composer.addView(input,new LinearLayout.LayoutParams(0,Ui.dp(this,54),1));

        TextView send=Ui.text(this,"➤",24);
        send.setGravity(Gravity.CENTER);
        send.setOnClickListener(v -> send());
        composer.addView(send,new LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,54)));
        root.addView(composer);

        load();
    }

    private void load() {
        messages.removeAllViews();
        List<Store.Message> all=Store.messagesDetailed(this,chat);
        if(all.isEmpty() && chat.equals("Ayurones Test Server"))
            addSystem("Test Server подключается из раздела настроек. Все остальные сообщения сохраняются локально.");
        for(Store.Message m:all) addBubble(m);
    }

    private void addSystem(String text) {
        TextView v=Ui.text(this,text,13);
        v.setTextColor(Color.GRAY);
        v.setGravity(Gravity.CENTER);
        v.setPadding(Ui.dp(this,20),Ui.dp(this,14),Ui.dp(this,20),Ui.dp(this,14));
        messages.addView(v,new LinearLayout.LayoutParams(-1,-2));
    }

    private void addBubble(Store.Message m) {
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(this,14),Ui.dp(this,9),Ui.dp(this,14),Ui.dp(this,8));
        box.setBackground(Ui.bg(m.mine?Color.rgb(48,48,48):Color.rgb(23,23,23),Color.rgb(60,60,60),Ui.dp(this,18)));

        if(m.reply!=null && !m.reply.isEmpty()){
            TextView q=Ui.text(this,"↩  "+m.reply,11);
            q.setTextColor(Color.LTGRAY);
            q.setPadding(Ui.dp(this,8),0,Ui.dp(this,8),Ui.dp(this,5));
            box.addView(q);
        }

        if(m.fileUri!=null && !m.fileUri.isEmpty()){
            LinearLayout file=row();
            TextView icon=Ui.text(this,"📎",24);
            icon.setGravity(Gravity.CENTER);
            file.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));
            LinearLayout words=new LinearLayout(this);
            words.setOrientation(LinearLayout.VERTICAL);
            TextView fn=Ui.text(this,m.fileName==null?"Файл":m.fileName,14);
            fn.setTypeface(null,android.graphics.Typeface.BOLD);
            TextView fs=Ui.text(this,formatBytes(m.fileSize)+" · открыть",11);
            fs.setTextColor(Color.LTGRAY);
            words.addView(fn);words.addView(fs);
            file.addView(words,new LinearLayout.LayoutParams(0,-2,1));
            file.setOnClickListener(v->openFile(m.fileUri,m.fileName));
            box.addView(file);
        }

        if(m.text!=null && !m.text.isEmpty()){
            TextView text=Ui.text(this,m.text,15);
            text.setPadding(0,0,0,0);
            box.addView(text);
        }

        LinearLayout meta=row();
        TextView time=Ui.text(this,DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(m.time))+(m.edited?" · изменено":""),10);
        time.setTextColor(Color.GRAY);
        meta.addView(time,new LinearLayout.LayoutParams(0,Ui.dp(this,24),1));
        TextView react=Ui.text(this,(m.reaction==null||m.reaction.isEmpty())?"♡":m.reaction,13);
        react.setGravity(Gravity.CENTER);
        react.setOnClickListener(v->{Store.toggleReaction(this,chat,m.id,"♥");load();});
        meta.addView(react,new LinearLayout.LayoutParams(Ui.dp(this,34),Ui.dp(this,24)));
        box.addView(meta);

        box.setOnLongClickListener(v->{messageMenu(m);return true;});
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
        lp.gravity=m.mine?Gravity.END:Gravity.START;
        lp.topMargin=Ui.dp(this,6);
        lp.leftMargin=Ui.dp(this,16);
        lp.rightMargin=Ui.dp(this,16);
        messages.addView(box,lp);
    }

    private void send() {
        String s=input.getText().toString().trim();
        if(s.isEmpty()) return;
        Store.addMessage(this,chat,s,replyId.isEmpty()?"":replyPreview(replyId));
        Store.addPoints(this,5);
        Store.saveDraft(this,chat,"");
        input.setText("");
        clearReply();
        AudioPack.play(this, Store.sound(this));
        load();
    }

    private String replyPreview(String id) {
        for(Store.Message m:Store.messagesDetailed(this,chat))
            if(m.id.equals(id)) return Store.previewText(m);
        return "";
    }

    private void messageMenu(Store.Message m) {
        List<String> items = new java.util.ArrayList<>();
        items.add("Ответить");
        items.add("Скопировать");
        items.add("♥ Реакция");
        if(m.mine && m.fileUri.isEmpty()) items.add("Изменить");
        items.add("Удалить");
        final String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setItems(a,(d,w)->{
            String action=a[w];
            if(action.equals("Ответить")) {
                replyId=m.id;
                replyText.setText("Ответ: "+Store.previewText(m));
                replyBar.setVisibility(View.VISIBLE);
                input.requestFocus();
            } else if(action.equals("Скопировать")) {
                ClipboardManager cb=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                cb.setPrimaryClip(ClipData.newPlainText("Ayurones",m.text==null?"":m.text));
            } else if(action.startsWith("♥")) {
                Store.toggleReaction(this,chat,m.id,"♥");load();
            } else if(action.equals("Изменить")) {
                editMessage(m);
            } else {
                new AlertDialog.Builder(this).setTitle("Удалить сообщение?")
                    .setMessage("Сообщение будет удалено только из локальной истории.")
                    .setNegativeButton("Отмена",null)
                    .setPositiveButton("Удалить",(x,y)->{Store.deleteMessage(this,chat,m.id);load();}).show();
            }
        }).show();
    }

    private void editMessage(Store.Message m) {
        EditText e=new EditText(this);
        e.setText(m.text);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY);
        e.setSingleLine(false);e.setMaxLines(6);
        new AlertDialog.Builder(this).setTitle("Изменить сообщение").setView(e)
            .setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{
                String value=e.getText().toString().trim();
                if(!value.isEmpty()){Store.editMessage(this,chat,m.id,value);load();}
            }).show();
    }

    private void clearReply() {
        replyId="";
        if(replyBar!=null) replyBar.setVisibility(View.GONE);
    }

    private void pickFile() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,70);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==70&&resultCode==Activity.RESULT_OK&&data!=null&&data.getData()!=null){
            Uri u=data.getData();
            try{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
            String name=fileName(u);
            long size=fileSize(u);
            Store.addFileMessage(this,chat,u,name,size,replyId.isEmpty()?"":replyPreview(replyId));
            Store.addPoints(this,5);
            clearReply();
            AudioPack.play(this, Store.sound(this));
            load();
        }
    }

    private long fileSize(Uri u){
        android.database.Cursor c=null;
        try{
            c=getContentResolver().query(u,new String[]{OpenableColumns.SIZE},null,null,null);
            if(c!=null&&c.moveToFirst()) return c.getLong(0);
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        return 0;
    }

    private String fileName(Uri u) {
        android.database.Cursor c=null;
        try{
            c=getContentResolver().query(u,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null);
            if(c!=null&&c.moveToFirst()) return c.getString(0);
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        return "";
    }

    private void openFile(String value,String name){
        try{
            Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(value));
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        }catch(Exception e){
            new AlertDialog.Builder(this).setTitle("Не удалось открыть файл")
                .setMessage(name==null?"Файл недоступен":"Файл: "+name)
                .setPositiveButton("ОК",null).show();
        }
    }

    private void showChatMenu(){
        String[] items={"Поиск в этом чате","Закрепить чат","Очистить историю","Информация"};
        new AlertDialog.Builder(this).setTitle(chat).setItems(items,(d,w)->{
            if(w==0) searchHere();
            if(w==1) {Store.setPinned(this,chat,!Store.pinned(this,chat));}
            if(w==2) new AlertDialog.Builder(this).setTitle("Очистить историю?")
                .setNegativeButton("Отмена",null).setPositiveButton("Очистить",(x,y)->{Store.clearChat(this,chat);load();}).show();
            if(w==3) showInfo();
        }).show();
    }

    private void searchHere(){
        EditText e=new EditText(this);
        e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY);e.setHint("Текст");
        new AlertDialog.Builder(this).setTitle("Поиск").setView(e)
            .setPositiveButton("Найти",(d,w)->{
                String q=e.getText().toString().trim().toLowerCase();
                if(q.isEmpty())return;
                for(Store.Message m:Store.messagesDetailed(this,chat))
                    if((m.text+" "+m.fileName).toLowerCase().contains(q)){
                        new AlertDialog.Builder(this).setTitle("Найдено")
                            .setMessage(Store.previewText(m)+"\n"+DateFormat.getDateTimeInstance().format(new Date(m.time)))
                            .setPositiveButton("ОК",null).show();return;
                    }
                new AlertDialog.Builder(this).setTitle("Не найдено").setPositiveButton("ОК",null).show();
            }).setNegativeButton("Закрыть",null).show();
    }

    private void showInfo(){
        new AlertDialog.Builder(this).setTitle(chat)
            .setMessage("Сообщений: "+Store.messagesDetailed(this,chat).size()+
                "\nПоследняя активность: "+(Store.lastTime(this,chat)==0?"нет":DateFormat.getDateTimeInstance().format(new Date(Store.lastTime(this,chat))))+
                "\nЗакреплено: "+(Store.pinned(this,chat)?"да":"нет")+
                "\nБез звука: "+(Store.muted(this,chat)?"да":"нет"))
            .setPositiveButton("Закрыть",null).show();
    }

    private String formatBytes(long n){
        if(n<1024)return n+" B";
        if(n<1024*1024)return (n/1024)+" KB";
        return String.format(java.util.Locale.US,"%.1f MB",n/(1024f*1024f));
    }
}
