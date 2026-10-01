package ai.ayurones.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

public class ChatActivity extends BaseActivity {
    private String chat;
    private LinearLayout messages;
    private EditText input;

    static Intent intent(Context c, String name) {
        Intent i = new Intent(c, ChatActivity.class); i.putExtra("chat", name); return i;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        chat = getIntent().getStringExtra("chat");
        if (chat == null) chat = "Сохранённые";
        build();
    }

    private void build() {
        LinearLayout root = root();
        LinearLayout top = row();
        TextView back = Ui.text(this, "‹", 34); back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));
        TextView name = Ui.text(this, chat, 20);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        top.addView(name, new LinearLayout.LayoutParams(0,Ui.dp(this,64),1));
        TextView info = Ui.text(this, "i", 20); info.setGravity(Gravity.CENTER);
        info.setOnClickListener(v -> showInfo());
        top.addView(info,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));
        root.addView(top); root.addView(Ui.divider(this));

        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this); messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14));
        scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout composer = row();
        input = new EditText(this);
        input.setTextColor(Color.WHITE); input.setHintTextColor(Color.GRAY);
        input.setHint("Сообщение");
        input.setSingleLine(false);
        input.setMaxLines(4);
        input.setPadding(Ui.dp(this,16),Ui.dp(this,8),Ui.dp(this,16),Ui.dp(this,8));
        input.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(60,60,60),Ui.dp(this,22)));
        composer.addView(input,new LinearLayout.LayoutParams(0,Ui.dp(this,54),1));
        TextView attach=Ui.text(this,"＋",24); attach.setGravity(Gravity.CENTER);
        attach.setOnClickListener(v -> pickFile());
        composer.addView(attach,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,54)));
        TextView send=Ui.text(this,"➤",24); send.setGravity(Gravity.CENTER);
        send.setOnClickListener(v -> send());
        composer.addView(send,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,54)));
        root.addView(composer);
        load();
    }

    private void load() {
        messages.removeAllViews();
        List<String> all=Store.messages(this,chat);
        if(all.isEmpty() && chat.equals("Ayurones Test Server"))
            addBubble("Система: Test Server доступен для проверки новых функций.", false);
        for(String m:all) addBubble(m,true);
    }

    private void addBubble(String text, boolean mine) {
        TextView v=Ui.text(this,text,15);
        v.setTextColor(Color.WHITE);
        v.setGravity(mine?Gravity.END:Gravity.START);
        v.setPadding(Ui.dp(this,14),Ui.dp(this,10),Ui.dp(this,14),Ui.dp(this,10));
        v.setBackground(Ui.bg(mine?Color.rgb(55,55,55):Color.rgb(25,25,25),Color.rgb(58,58,58),Ui.dp(this,18)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
        lp.gravity=mine?Gravity.END:Gravity.START; lp.topMargin=Ui.dp(this,7); lp.leftMargin=Ui.dp(this,20); lp.rightMargin=Ui.dp(this,20);
        messages.addView(v,lp);
    }

    private void send() {
        String s=input.getText().toString().trim();
        if(s.isEmpty()) return;
        Store.addMessage(this,chat,s);
        Store.addPoints(this,5);
        input.setText("");
        load();
    }

    private void pickFile() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
        startActivityForResult(i,70);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==70 && resultCode==Activity.RESULT_OK && data!=null && data.getData()!=null){
            Uri u=data.getData();
            String name=fileName(u);
            Store.addMessage(this,chat,"📎 " + (name.isEmpty()?u.toString():name));
            Store.addPoints(this,5);
            load();
        }
    }

    private String fileName(Uri u) {
        Cursor c=null;
        try{
            c=getContentResolver().query(u,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null);
            if(c!=null&&c.moveToFirst()) return c.getString(0);
        }catch(Exception ignored){} finally{if(c!=null)c.close();}
        return "";
    }

    private void showInfo(){
        new android.app.AlertDialog.Builder(this)
            .setTitle(chat)
            .setMessage(chat.equals("Ayurones Test Server")?
                "Тестовый чат для проверки функций мессенджера.":"Личный чат Ayurones.")
            .setPositiveButton("Закрыть",null).show();
    }
}
