package ai.ayurones.messenger;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends BaseActivity {
    private TextView serverStatus;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LinearLayout root=root();
        TextView top=Ui.text(this,"‹   Настройки",22);top.setTypeface(null,android.graphics.Typeface.BOLD);top.setOnClickListener(v->finish());
        root.addView(top,new LinearLayout.LayoutParams(-1,Ui.dp(this,64)));root.addView(Ui.divider(this));
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,24));
        add(list,"Профиль","Фото, имя и оформление",v->startActivity(new android.content.Intent(this,ProfileActivity.class)));
        add(list,"Украшения","Покупка и экипировка",v->startActivity(new android.content.Intent(this,DecorationsActivity.class)));
        TextView pts=Ui.text(this,"Баллы",16);pts.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));pts.setOnClickListener(v->showPoints());list.addView(pts,new LinearLayout.LayoutParams(-1,Ui.dp(this,60)));
        TextView contact=Ui.text(this,"Контакты","".length());
        EditText host=new EditText(this);host.setTextColor(Color.WHITE);host.setHintTextColor(Color.GRAY);host.setHint("http://192.168.1.10:8787");host.setSingleLine(true);host.setText(Store.p(this).getString("server","http://127.0.0.1:8787"));
        TextView server=Ui.text(this,"Test Server",17);server.setTypeface(null,android.graphics.Typeface.BOLD);
        list.addView(Ui.text(this,"Подключение к тестовой среде",13),new LinearLayout.LayoutParams(-1,Ui.dp(this,34)));
        list.addView(host,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));
        TextView save=Ui.text(this,"Сохранить адрес и проверить",14);save.setGravity(Gravity.CENTER);save.setBackground(Ui.bg(Color.rgb(255,255,255),Color.WHITE,Ui.dp(this,16)));save.setTextColor(Color.BLACK);
        save.setOnClickListener(v->ping(host));list.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));
        serverStatus=Ui.text(this,"Статус: не проверен",13);serverStatus.setTextColor(Color.LTGRAY);list.addView(serverStatus);
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
    }
    private void add(LinearLayout l,String title,String sub,android.view.View.OnClickListener click){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(Ui.dp(this,14),Ui.dp(this,5),Ui.dp(this,14),Ui.dp(this,5));r.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));
        TextView a=Ui.text(this,title,16);a.setTypeface(null,android.graphics.Typeface.BOLD);TextView s=Ui.text(this,sub,12);s.setTextColor(Color.LTGRAY);
        r.addView(a);r.addView(s);r.setOnClickListener(click);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,68));lp.bottomMargin=Ui.dp(this,9);l.addView(r,lp);
    }
    private void showPoints(){
        Store.dailyBonus(this);
        new AlertDialog.Builder(this).setTitle("Баллы и активность").setMessage("Баланс: "+Store.points(this)+"\n\nСообщение  +5\nНовое фото  +10\nЕжедневный вход  +20\n\nБаллы тратятся на украшения.").setPositiveButton("Закрыть",null).show();
    }
    private void ping(EditText host){
        String base=host.getText().toString().trim();if(base.isEmpty())return;
        Store.p(this).edit().putString("server",base).apply();
        serverStatus.setText("Проверка…");
        new Thread(()->{
            boolean ok=false;
            try{HttpURLConnection c=(HttpURLConnection)new URL(base+"/health").openConnection();c.setConnectTimeout(2500);c.setReadTimeout(2500);ok=c.getResponseCode()==200;c.disconnect();}catch(Exception ignored){}
            final boolean result=ok;runOnUiThread(()->serverStatus.setText(result?"Статус: Test Server онлайн":"Статус: нет ответа"));
        }).start();
    }
}
