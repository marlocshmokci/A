package ai.ayurones.messenger;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends BaseActivity {
    private TextView serverStatus;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}

    private void build(){
        LinearLayout root=root();
        TextView top=Ui.text(this,"‹   Настройки",22);
        top.setTypeface(null,android.graphics.Typeface.BOLD);
        top.setOnClickListener(v->finish());
        root.addView(top,new LinearLayout.LayoutParams(-1,Ui.dp(this,64)));
        root.addView(Ui.divider(this));

        LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,24));

        add(list,"Профиль","Фото, имя и оформление",v->startActivity(new android.content.Intent(this,ProfileActivity.class)));
        add(list,"Украшения","Покупка и экипировка",v->startActivity(new android.content.Intent(this,DecorationsActivity.class)));

        TextView pts=Ui.text(this,"Баллы",16);
        pts.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));
        pts.setOnClickListener(v->showPoints());
        list.addView(pts,new LinearLayout.LayoutParams(-1,Ui.dp(this,60)));

        list.addView(Ui.text(this,"Подключение к тестовой среде",13),
            new LinearLayout.LayoutParams(-1,Ui.dp(this,34)));

        EditText host=new EditText(this);
        host.setTextColor(Color.WHITE);
        host.setHintTextColor(Color.GRAY);
        host.setHint("http://192.168.1.10:8787");
        host.setSingleLine(true);
        host.setText(Store.p(this).getString("server","http://127.0.0.1:8787"));
        list.addView(host,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));

        TextView save=Ui.text(this,"Сохранить адрес и проверить",14);
        save.setGravity(Gravity.CENTER);
        save.setBackground(Ui.bg(Color.rgb(255,255,255),Color.WHITE,Ui.dp(this,16)));
        save.setTextColor(Color.BLACK);
        save.setOnClickListener(v->ping(host));
        list.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));

        serverStatus=Ui.text(this,"Статус: не проверен",13);
        serverStatus.setTextColor(Color.LTGRAY);
        list.addView(serverStatus);

        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void add(LinearLayout l,String title,String sub,android.view.View.OnClickListener click){
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(Ui.dp(this,14),Ui.dp(this,5),Ui.dp(this,14),Ui.dp(this,5));
        r.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));
        TextView a=Ui.text(this,title,16);
        a.setTypeface(null,android.graphics.Typeface.BOLD);
        TextView s=Ui.text(this,sub,12);
        s.setTextColor(Color.LTGRAY);
        r.addView(a);r.addView(s);r.setOnClickListener(click);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,68));
        lp.bottomMargin=Ui.dp(this,9);
        l.addView(r,lp);
    }

    private void showPoints(){
        Store.dailyBonus(this);
        new AlertDialog.Builder(this)
            .setTitle("Баллы и активность")
            .setMessage("Баланс: "+(Store.unlimited(this)?"∞":Store.points(this))
                +"\n\nСообщение  +5\nНовое фото  +10\nЕжедневный вход  +20"
                +"\n\nБаллы тратятся на украшения.")
            .setPositiveButton("Закрыть",null).show();
    }

    private void ping(EditText host){
        String base=host.getText().toString().trim();
        if(base.isEmpty())return;
        Store.p(this).edit().putString("server",base).apply();
        serverStatus.setText("Проверка…");

        new Thread(()->{
            boolean health=false;
            boolean unlimited=false;
            boolean allDecorations=false;
            try{
                HttpURLConnection h=(HttpURLConnection)new URL(base+"/health").openConnection();
                h.setConnectTimeout(2500);h.setReadTimeout(2500);
                health=h.getResponseCode()==200;
                h.disconnect();

                if(health){
                    HttpURLConnection s=(HttpURLConnection)new URL(base+"/status").openConnection();
                    s.setConnectTimeout(2500);s.setReadTimeout(2500);
                    if(s.getResponseCode()==200){
                        BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
                        StringBuilder jsonText=new StringBuilder();
                        String line;while((line=br.readLine())!=null)jsonText.append(line);
                        br.close();
                        JSONObject o=new JSONObject(jsonText.toString());
                        unlimited="unlimited".equals(o.optString("points"));
                        allDecorations="all".equals(o.optString("decorations"));
                    }
                    s.disconnect();
                }
            }catch(Exception ignored){}

            final boolean hOk=health;
            final boolean uOk=unlimited;
            final boolean dOk=allDecorations;

            if(uOk)Store.setUnlimited(this,true);
            if(dOk)Store.grantAllDecorations(this,DecorationsActivity.ITEMS);

            runOnUiThread(()->{
                if(!hOk){
                    serverStatus.setText("Статус: нет ответа");
                }else if(uOk||dOk){
                    serverStatus.setText("Статус: Test Server онлайн · тестовый режим активен");
                }else{
                    serverStatus.setText("Статус: Test Server онлайн");
                }
            });
        }).start();
    }
}
