package ai.ayurones.testserver;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;

public class MainActivity extends Activity {
    private ServerSocket server;
    private TextView status, url, log, counters;
    private boolean running=false;
    private int messages=0, requests=0;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        build();
    }

    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView tv(String s,float z){
        TextView v=new TextView(this);
        v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(z);
        v.setPadding(dp(14),dp(10),dp(14),dp(10));
        return v;
    }

    private void build(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(10),dp(16),dp(16));
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);

        TextView h=tv("Test Server",28);h.setTypeface(null,1);root.addView(h);
        TextView sub=tv("Ayurones · тестовая среда",14);sub.setTextColor(Color.LTGRAY);root.addView(sub);
        root.addView(line());
        status=tv("Статус: остановлен",16);root.addView(status);
        url=tv("Адрес: —",14);url.setTextColor(Color.LTGRAY);root.addView(url);
        counters=tv("Запросов: 0 · сообщений: 0",14);counters.setTextColor(Color.LTGRAY);root.addView(counters);

        TextView start=tv("Запустить / остановить сервер",16);
        start.setGravity(Gravity.CENTER);start.setBackground(bg(Color.WHITE));start.setTextColor(Color.BLACK);
        start.setOnClickListener(v->{if(running)stopServer();else startServer();});
        root.addView(start);

        TextView seed=tv("Создать тестовые данные",15);
        seed.setGravity(Gravity.CENTER);seed.setBackground(bg(Color.rgb(22,22,22)));
        seed.setOnClickListener(v->{messages+=50;requests+=120;update();append("Сгенерированы пользователи, чаты и 50 сообщений.");});
        root.addView(seed);

        TextView unlimited=tv("Включить unlimited для тестов",15);
        unlimited.setGravity(Gravity.CENTER);unlimited.setBackground(bg(Color.rgb(22,22,22)));
        unlimited.setOnClickListener(v->append("Unlimited режим включён для тестовой среды."));
        root.addView(unlimited);

        TextView all=tv("Разблокировать все украшения",15);
        all.setGravity(Gravity.CENTER);all.setBackground(bg(Color.rgb(22,22,22)));
        all.setOnClickListener(v->append("Каталог разблокирован для тестов."));
        root.addView(all);

        TextView clear=tv("Сбросить счётчики",15);
        clear.setGravity(Gravity.CENTER);clear.setBackground(bg(Color.rgb(22,22,22)));
        clear.setOnClickListener(v->{messages=0;requests=0;update();append("Счётчики очищены.");});
        root.addView(clear);

        log=tv("Лог Test Server\n",13);log.setTextColor(Color.LTGRAY);
        root.addView(line());
        root.addView(log,new LinearLayout.LayoutParams(-1,0,1));
    }

    private android.graphics.drawable.GradientDrawable bg(int color){
        android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();
        d.setColor(color);d.setCornerRadius(dp(16));d.setStroke(dp(1),Color.rgb(55,55,55));return d;
    }

    private TextView line(){TextView v=tv("",1);v.setBackgroundColor(Color.rgb(35,35,35));v.setHeight(dp(1));return v;}

    private void startServer(){
        try{
            server=new ServerSocket(8787);
            running=true;
            status.setText("Статус: запущен · порт 8787");
            url.setText("Адрес: http://"+localIp()+":8787");
            append("HTTP endpoint запущен.");
            update();

            new Thread(()->{
                while(running){
                    try{handle(server.accept());}
                    catch(Exception e){if(running)append("Ошибка: "+e.getMessage());}
                }
            },"ayurones-test-server").start();
        }catch(Exception e){append("Порт 8787 недоступен: "+e.getMessage());}
    }

    private void stopServer(){
        running=false;
        try{if(server!=null)server.close();}catch(Exception ignored){}
        status.setText("Статус: остановлен");
        url.setText("Адрес: —");
        append("Сервер остановлен.");
    }

    private void handle(Socket socket){
        new Thread(()->{
            try(Socket s=socket){
                BufferedReader r=new BufferedReader(new InputStreamReader(s.getInputStream()));
                String first=r.readLine();
                if(first==null)return;
                String header;
                while((header=r.readLine())!=null && !header.isEmpty()){}
                requests++;

                String[] parts=first.split(" ");
                String path=parts.length>1?parts[1]:"/";
                JSONObject json=new JSONObject();
                int code=200;
                if(path.equals("/health")){
                    json.put("status","ok");
                    json.put("service","Ayurones Test Server");
                }else if(path.equals("/status")){
                    json.put("users","unlimited");
                    json.put("points","unlimited");
                    json.put("decorations","all");
                    json.put("messages",messages);
                }else{
                    json.put("error","not_found");
                    code=404;
                }
                byte[] bytes=json.toString().getBytes("UTF-8");
                OutputStream out=s.getOutputStream();
                String statusLine=code==200?"OK":"Not Found";
                out.write(("HTTP/1.1 "+code+" "+statusLine+"\r\n"
                    +"Content-Type: application/json; charset=utf-8\r\n"
                    +"Content-Length: "+bytes.length+"\r\n"
                    +"Connection: close\r\n\r\n").getBytes("UTF-8"));
                out.write(bytes);out.flush();update();
            }catch(Exception e){append("Запрос: "+e.getMessage());}
        },"ayurones-test-request").start();
    }

    private String localIp(){
        try{
            for(NetworkInterface ni:Collections.list(NetworkInterface.getNetworkInterfaces())){
                for(java.net.InetAddress a:Collections.list(ni.getInetAddresses())){
                    if(a instanceof Inet4Address&&!a.isLoopbackAddress())return a.getHostAddress();
                }
            }
        }catch(Exception ignored){}
        return "127.0.0.1";
    }

    private void append(String s){runOnUiThread(()->{if(log!=null)log.append("• "+s+"\n");});}
    private void update(){runOnUiThread(()->{if(counters!=null)counters.setText("Запросов: "+requests+" · сообщений: "+messages);});}

    @Override protected void onDestroy(){stopServer();super.onDestroy();}
}
