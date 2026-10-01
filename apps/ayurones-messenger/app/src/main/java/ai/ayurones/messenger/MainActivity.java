package ai.ayurones.messenger;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseActivity {
    private LinearLayout list;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Store.dailyBonus(this);
        showChats();
    }

    private TextView title(String value) {
        TextView t = Ui.text(this, value, 24);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(Ui.dp(this,20), Ui.dp(this,18), Ui.dp(this,8), Ui.dp(this,18));
        return t;
    }

    void showChats() {
        LinearLayout root = root();
        LinearLayout top = row();
        TextView h = title("Ayurones");
        top.addView(h, new LinearLayout.LayoutParams(0, Ui.dp(this,66), 1));
        TextView search = Ui.text(this, "⌕", 28);
        search.setGravity(Gravity.CENTER);
        search.setOnClickListener(v -> searchChats());
        top.addView(search, new LinearLayout.LayoutParams(Ui.dp(this,54), Ui.dp(this,66)));
        TextView settings = Ui.text(this, "⋮", 28);
        settings.setGravity(Gravity.CENTER);
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        top.addView(settings, new LinearLayout.LayoutParams(Ui.dp(this,54), Ui.dp(this,66)));
        root.addView(top);
        root.addView(Ui.divider(this));

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));
        populateChats();
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1,0,1));

        TextView nav = Ui.text(this, "Чаты                 Контакты                 Профиль", 14);
        nav.setGravity(Gravity.CENTER);
        nav.setBackground(Ui.bg(Color.rgb(12,12,12), Color.rgb(40,40,40), Ui.dp(this,18)));
        nav.setPadding(4, Ui.dp(this,16), 4, Ui.dp(this,16));
        nav.setOnClickListener(v -> {});
        root.addView(nav);
        nav.setOnLongClickListener(v -> { startActivity(new Intent(this, ProfileActivity.class)); return true; });

        TextView add = Ui.text(this, "+", 28);
        add.setGravity(Gravity.CENTER);
        add.setTextColor(Color.WHITE);
        add.setBackground(Ui.bg(Color.WHITE, Color.WHITE, Ui.dp(this,22)));
        add.setTextColor(Color.BLACK);
        add.setOnClickListener(v -> newChat());
        android.widget.FrameLayout overlay = new android.widget.FrameLayout(this);
        root.addView(overlay, new LinearLayout.LayoutParams(-1, Ui.dp(this,1)));
    }

    private void populateChats() {
        list.removeAllViews();
        addChat("Сохранённые", "Ваше личное хранилище");
        addChat("Ayurones Test Server", "Тестовая среда");
        List<String> names = new ArrayList<>(Store.contacts(this));
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        for (String n : names) addChat(n, Store.preview(this, n));
    }

    private void addChat(String name, String preview) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(Ui.dp(this,8),Ui.dp(this,7),Ui.dp(this,8),Ui.dp(this,7));
        card.setBackground(Ui.bg(Color.rgb(16,16,16), Color.rgb(34,34,34), Ui.dp(this,18)));
        TextView avatar = Ui.text(this, avatarFor(name), 23);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(Ui.bg(Color.rgb(32,32,32), 0, Ui.dp(this,28)));
        card.addView(avatar, new LinearLayout.LayoutParams(Ui.dp(this,52), Ui.dp(this,52)));
        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        TextView n = Ui.text(this, name, 16);
        n.setTypeface(null, android.graphics.Typeface.BOLD);
        TextView p = Ui.text(this, preview, 13);
        p.setTextColor(Color.LTGRAY);
        words.addView(n); words.addView(p);
        card.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
        card.setOnClickListener(v -> {
            if (name.equals("Сохранённые")) startActivity(ChatActivity.intent(this,name));
            else if (name.equals("Ayurones Test Server")) startActivity(ChatActivity.intent(this,name));
            else startActivity(ChatActivity.intent(this,name));
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.dp(this,72));
        lp.bottomMargin = Ui.dp(this,9);
        list.addView(card, lp);
    }

    private String avatarFor(String name) {
        if (name.equals("Ayurones Test Server")) return "TS";
        if (name.equals("Сохранённые")) return "★";
        return name.isEmpty() ? "A" : name.substring(0,1).toUpperCase();
    }

    private void newChat() {
        EditText e = new EditText(this);
        e.setTextColor(Color.WHITE); e.setHintTextColor(Color.GRAY);
        e.setHint("Имя или @username");
        e.setSingleLine(true);
        AlertDialog d = new AlertDialog.Builder(this)
            .setTitle("Новый чат")
            .setView(e)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Открыть", null).create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = e.getText().toString().trim();
            if (name.isEmpty()) return;
            Store.addContact(this,name);
            d.dismiss();
            startActivity(ChatActivity.intent(this,name));
        }));
        d.show();
    }

    private void searchChats() {
        EditText e = new EditText(this);
        e.setTextColor(Color.WHITE); e.setHintTextColor(Color.GRAY);
        e.setHint("Поиск по чатам");
        new AlertDialog.Builder(this).setTitle("Поиск").setView(e)
            .setNegativeButton("Закрыть", null)
            .setPositiveButton("Найти", (d,w) -> {
                String q=e.getText().toString().toLowerCase();
                String found=null;
                for(String n: Store.contacts(this)) if(n.toLowerCase().contains(q)){found=n;break;}
                if(found!=null) startActivity(ChatActivity.intent(this,found));
                else if("сохран".contains(q)) startActivity(ChatActivity.intent(this,"Сохранённые"));
            }).show();
    }
}
