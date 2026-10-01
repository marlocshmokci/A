package ai.ayurones.messenger;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MediaLibraryActivity extends BaseActivity {
    private TextView selected;
    private String playing = "";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        build();
    }

    private void build() {
        LinearLayout root=root();
        TextView top=Ui.text(this,"‹   Звуки и медиатека",22);
        top.setTypeface(null,android.graphics.Typeface.BOLD);
        top.setOnClickListener(v->{AudioPack.stop();finish();});
        root.addView(top,new LinearLayout.LayoutParams(-1,Ui.dp(this,64)));
        root.addView(Ui.divider(this));

        TextView intro=Ui.text(this,
            "Встроенная офлайн-медиатека Ayurones. Каждый звук реально хранится в APK и используется для отправки сообщений.\n\nВыбранный звук: "+AudioPack.name(Store.sound(this)),14);
        intro.setTextColor(Color.LTGRAY);
        intro.setPadding(Ui.dp(this,16),Ui.dp(this,12),Ui.dp(this,16),Ui.dp(this,12));
        root.addView(intro);
        selected=intro;

        ScrollView sv=new ScrollView(this);
        LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(Ui.dp(this,14),Ui.dp(this,8),Ui.dp(this,14),Ui.dp(this,24));

        for(int i=0;i<AudioPack.IDS.length;i++) addSound(list,AudioPack.IDS[i],AudioPack.NAMES[i],i+1);
        TextView about=Ui.text(this,
            "Медиапак предназначен для офлайн-работы: звуки можно прослушивать без сети и выбирать как звук отправки сообщения. Размер APK увеличен за счёт реальных аудиоресурсов, а не пустых файлов.",13);
        about.setTextColor(Color.GRAY);
        about.setPadding(Ui.dp(this,8),Ui.dp(this,18),Ui.dp(this,8),0);
        list.addView(about);
        sv.addView(list);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void addSound(LinearLayout list,String id,String name,int number) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(this,10),Ui.dp(this,6),Ui.dp(this,8),Ui.dp(this,6));
        row.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));

        TextView icon=Ui.text(this,String.format("%02d",number),16);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(Ui.bg(Color.rgb(30,30,30),Color.rgb(55,55,55),Ui.dp(this,14)));
        row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));

        LinearLayout words=new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        TextView title=Ui.text(this,name,16);
        title.setTypeface(null,android.graphics.Typeface.BOLD);
        TextView detail=Ui.text(this,id.equals(Store.sound(this))?"Выбрано · нажмите ▶ для прослушивания":"Офлайн · нажмите, чтобы выбрать",12);
        detail.setTextColor(Color.LTGRAY);
        words.addView(title);words.addView(detail);
        row.addView(words,new LinearLayout.LayoutParams(0,-2,1));

        TextView play=Ui.text(this,"▶",20);
        play.setGravity(Gravity.CENTER);
        play.setOnClickListener(v->{
            if(playing.equals(id)){AudioPack.stop();playing="";play.setText("▶");}
            else {AudioPack.play(this,id);playing=id;play.setText("■");}
        });
        row.addView(play,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));

        row.setOnClickListener(v->{
            Store.setSound(this,id);
            selected.setText("Выбранный звук: "+name);
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,68));
        lp.bottomMargin=Ui.dp(this,8);
        list.addView(row,lp);
    }

    @Override protected void onDestroy() {
        AudioPack.stop();
        super.onDestroy();
    }
}
