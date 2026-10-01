package ai.ayurones.messenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
    private static final int EXPORT=301;
    private static final int IMPORT=302;

    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}

    private void build(){
        LinearLayout root=root();
        TextView top=Ui.text(this,"‹   Настройки",22);
        top.setTypeface(null,android.graphics.Typeface.BOLD);top.setOnClickListener(v->finish());
        root.addView(top,new LinearLayout.LayoutParams(-1,Ui.dp(this,64)));
        root.addView(Ui.divider(this));

        android.widget.ScrollView scroll=new android.widget.ScrollView(this);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,28));

        add(list,"Профиль","Имя, username, описание и фото",v->startActivity(new Intent(this,ProfileActivity.class)));
        add(list,"Звуки и медиатека","Выбор реального офлайн-звука отправки",v->startActivity(new Intent(this,MediaLibraryActivity.class)));

        TextView sound=Ui.text(this,"Звук отправки: "+(Store.sendSound(this)?"включён":"выключен"),15);
        sound.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));
        sound.setOnClickListener(v->{Store.setSendSound(this,!Store.sendSound(this));sound.setText("Звук отправки: "+(Store.sendSound(this)?"включён":"выключен"));});
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,Ui.dp(this,58));slp.bottomMargin=Ui.dp(this,9);list.addView(sound,slp);

        add(list,"Украшения","Покупка и экипировка",v->startActivity(new Intent(this,DecorationsActivity.class)));
        TextView pts=Ui.text(this,"Баллы",16);pts.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));pts.setOnClickListener(v->showPoints());
        LinearLayout.LayoutParams plp=new LinearLayout.LayoutParams(-1,Ui.dp(this,58));plp.bottomMargin=Ui.dp(this,9);list.addView(pts,plp);

        TextView export=Ui.text(this,"Резервная копия → экспорт",16);styleAction(export);export.setOnClickListener(v->exportBackup());list.addView(export,new LinearLayout.LayoutParams(-1,Ui.dp(this,58)));
        TextView imp=Ui.text(this,"Резервная копия → импорт",16);styleAction(imp);imp.setOnClickListener(v->importBackup());LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(-1,Ui.dp(this,58));ilp.topMargin=Ui.dp(this,8);list.addView(imp,ilp);

        list.addView(Ui.text(this,"Подключение к тестовой среде",13),new LinearLayout.LayoutParams(-1,Ui.dp(this,34)));
        EditText host=new EditText(this);host.setTextColor(Color.WHITE);host.setHintTextColor(Color.GRAY);host.setHint("http://192.168.1.10:8787");host.setSingleLine(true);
        host.setText(Store.p(this).getString("server","http://127.0.0.1:8787"));list.addView(host,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));

        TextView save=Ui.text(this,"Сохранить адрес и проверить",14);save.setGravity(Gravity.CENTER);save.setBackground(Ui.bg(Color.WHITE,Color.WHITE,Ui.dp(this,16)));save.setTextColor(Color.BLACK);save.setOnClickListener(v->ping(host));
        list.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));
        serverStatus=Ui.text(this,"Статус: не проверен",13);serverStatus.setTextColor(Color.LTGRAY);list.addView(serverStatus);

        TextView note=Ui.text(this,"Данные мессенджера хранятся локально на устройстве. Экспорт позволяет перенести контакты, профили, чаты, настройки и баллы на другое устройство.",12);
        note.setTextColor(Color.GRAY);note.setPadding(0,Ui.dp(this,18),0,0);list.addView(note);

        scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void styleAction(TextView v){v.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));}

    private void add(LinearLayout l,String title,String sub,android.view.View.OnClickListener click){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(Ui.dp(this,14),Ui.dp(this,5),Ui.dp(this,14),Ui.dp(this,5));
        r.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,16)));
        TextView a=Ui.text(this,title,16);a.setTypeface(null,android.graphics.Typeface.BOLD);
        TextView s=Ui.text(this,sub,12);s.setTextColor(Color.LTGRAY);r.addView(a);r.addView(s);r.setOnClickListener(click);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,68));lp.bottomMargin=Ui.dp(this,9);l.addView(r,lp);
    }

    private void showPoints(){
        Store.dailyBonus(this);
        new AlertDialog.Builder(this).setTitle("Баллы и активность")
            .setMessage("Баланс: "+(Store.unlimited(this)?"∞":Store.points(this))+"\n\nСообщение +5\nНовое фото +10\nЕжедневный вход +20")
            .setPositiveButton("Закрыть",null).show();
    }

    private void exportBackup(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"ayurones-backup.json");
        startActivityForResult(i,EXPORT);
    }

    private void importBackup(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");
        startActivityForResult(i,IMPORT);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=Activity.RESULT_OK||data==null||data.getData()==null)return;
        Uri u=data.getData();
        if(requestCode==EXPORT)writeBackup(u);
        if(requestCode==IMPORT)readBackup(u);
    }

    private void writeBackup(Uri u){
        try{
            java.io.OutputStream out=getContentResolver().openOutputStream(u);
            out.write(Store.exportBackup(this).getBytes(java.nio.charset.StandardCharsets.UTF_8));out.close();
            new AlertDialog.Builder(this).setTitle("Готово").setMessage("Резервная копия сохранена.").setPositiveButton("ОК",null).show();
        }catch(Exception e){new AlertDialog.Builder(this).setTitle("Ошибка").setMessage(e.toString()).setPositiveButton("ОК",null).show();}
    }

    private void readBackup(Uri u){
        try{
            java.io.InputStream in=getContentResolver().openInputStream(u);
            BufferedReader br=new BufferedReader(new InputStreamReader(in));
            StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line).append('\n');br.close();
            new AlertDialog.Builder(this).setTitle("Импортировать резервную копию?")
                .setMessage("Текущие локальные данные будут заменены данными из файла.")
                .setNegativeButton("Отмена",null)
                .setPositiveButton("Импорт",(d,w)->{
                    if(Store.importBackup(this,sb.toString())){new AlertDialog.Builder(this).setTitle("Готово").setMessage("Данные импортированы. Откройте нужный раздел заново.").setPositiveButton("ОК",null).show();}
                    else new AlertDialog.Builder(this).setTitle("Ошибка").setMessage("Файл не похож на резервную копию Ayurones.").setPositiveButton("ОК",null).show();
                }).show();
        }catch(Exception e){new AlertDialog.Builder(this).setTitle("Ошибка").setMessage(e.toString()).setPositiveButton("ОК",null).show();}
    }

    private void ping(EditText host){
        String base=host.getText().toString().trim();if(base.isEmpty())return;
        Store.p(this).edit().putString("server",base).apply();serverStatus.setText("Проверка…");
        new Thread(()->{
            boolean health=false,unlimited=false,allDecorations=false;
            try{
                HttpURLConnection h=(HttpURLConnection)new URL(base+"/health").openConnection();
                h.setConnectTimeout(2500);h.setReadTimeout(2500);health=h.getResponseCode()==200;h.disconnect();
                if(health){
                    HttpURLConnection s=(HttpURLConnection)new URL(base+"/status").openConnection();
                    s.setConnectTimeout(2500);s.setReadTimeout(2500);
                    if(s.getResponseCode()==200){
                        BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
                        StringBuilder text=new StringBuilder();String line;while((line=br.readLine())!=null)text.append(line);br.close();
                        JSONObject o=new JSONObject(text.toString());unlimited="unlimited".equals(o.optString("points"));allDecorations="all".equals(o.optString("decorations"));
                    }s.disconnect();
                }
            }catch(Exception ignored){}
            final boolean hOk=health,uOk=unlimited,dOk=allDecorations;
            if(uOk)Store.setUnlimited(this,true);if(dOk)Store.grantAllDecorations(this,DecorationsActivity.ITEMS);
            runOnUiThread(()->serverStatus.setText(!hOk?"Статус: нет ответа":(uOk||dOk?"Статус: Test Server онлайн · тестовый режим активен":"Статус: Test Server онлайн")));
        }).start();
    }
}
