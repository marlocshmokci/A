package com.ayuron.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int PICK_IMAGE = 41;
    private static final int BG = Color.rgb(21,23,25), PANEL = Color.rgb(33,36,39);
    private static final int FG = Color.rgb(244,245,247), MUTED = Color.rgb(162,168,174);
    private static final int ORANGE = Color.rgb(255,150,56);
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private android.content.SharedPreferences prefs;
    private LinearLayout chat;
    private ScrollView scroll;
    private TextView quota;
    private EditText composer;
    private Spinner model;
    private JSONArray history = new JSONArray();
    private String installId;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs = getSharedPreferences("ayuron", MODE_PRIVATE);
        installId = prefs.getString("install_id", null);
        if (installId == null) {
            installId = UUID.randomUUID().toString();
            prefs.edit().putString("install_id", installId).apply();
        }
        try { history = new JSONArray(prefs.getString("history", "[]")); }
        catch (Exception e) { history = new JSONArray(); }
        buildUi();
        restoreHistory();
        if (history.length() == 0) bubble("Привет! Я Ayuron. Все модели доступны без подписки. Напиши, чем помочь.", false);
        refreshUsage();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(15), dp(8), dp(15), dp(10));
        setContentView(root);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("Ayuron");
        title.setTextColor(FG);
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bar.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button settings = button("⚙", PANEL, FG);
        bar.addView(settings, new LinearLayout.LayoutParams(dp(52), dp(44)));
        settings.setOnClickListener(v -> settingsDialog());
        root.addView(bar);

        TextView sub = new TextView(this);
        sub.setText("Твой AI-помощник");
        sub.setTextColor(MUTED);
        sub.setTextSize(13);
        root.addView(sub);

        quota = new TextView(this);
        quota.setText("Текст: —/20     Медиа: —/8");
        quota.setTextColor(ORANGE);
        quota.setTextSize(13);
        quota.setPadding(dp(12),dp(10),dp(12),dp(10));
        quota.setBackground(rounded(PANEL,14));
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1,-2);
        qp.topMargin = dp(10); qp.bottomMargin = dp(9);
        root.addView(quota, qp);

        model = new Spinner(this);
        String[] names = {"Ayuron 3 · базовая","Ayurones · продвинутая","Ayurones 6 · флагман"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        model.setAdapter(adapter);
        model.setBackground(rounded(PANEL,14));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1,dp(48));
        mp.bottomMargin = dp(8);
        root.addView(model,mp);

        scroll = new ScrollView(this);
        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        chat.setPadding(0,dp(5),0,dp(8));
        scroll.addView(chat,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout actions = new LinearLayout(this);
        Button analyze = button("📷 Анализ фото",PANEL,FG);
        Button generate = button("✨ Создать картинку",PANEL,ORANGE);
        actions.addView(analyze,new LinearLayout.LayoutParams(0,dp(45),1));
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(0,dp(45),1);
        gp.leftMargin = dp(7);
        actions.addView(generate,gp);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1,-2);
        ap.topMargin=dp(7); ap.bottomMargin=dp(7);
        root.addView(actions,ap);
        analyze.setOnClickListener(v -> chooseImage());
        generate.setOnClickListener(v -> generateImage());

        LinearLayout sendRow = new LinearLayout(this);
        sendRow.setGravity(Gravity.CENTER_VERTICAL);
        composer = new EditText(this);
        composer.setTextColor(FG);
        composer.setHintTextColor(MUTED);
        composer.setHint("Напиши сообщение…");
        composer.setTextSize(16);
        composer.setMinLines(1);
        composer.setMaxLines(4);
        composer.setPadding(dp(13),dp(10),dp(13),dp(10));
        composer.setBackground(rounded(PANEL,18));
        sendRow.addView(composer,new LinearLayout.LayoutParams(0,-2,1));
        Button send = button("➤",ORANGE,Color.BLACK);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(54),dp(52));
        sp.leftMargin=dp(7);
        sendRow.addView(send,sp);
        root.addView(sendRow);
        send.setOnClickListener(v -> sendText());
    }

    private int dp(float n) { return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
    private GradientDrawable rounded(int color,int r) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(r)); return d;
    }
    private Button button(String text,int bg,int fg) {
        Button b=new Button(this); b.setText(text); b.setTextColor(fg); b.setTextSize(13);
        b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setPadding(dp(5),dp(3),dp(5),dp(3));
        b.setBackground(rounded(bg,14)); return b;
    }
    private String backend() { return prefs.getString("backend_url","").trim().replaceAll("/+$",""); }
    private String modelId() {
        int i=model==null?0:model.getSelectedItemPosition();
        return i==1?"ayurones":i==2?"ayurones6":"ayuron3";
    }
    private boolean needBackend() {
        if (backend().startsWith("https://")) return true;
        new AlertDialog.Builder(this).setTitle("Нужно подключить сервер")
            .setMessage("В настройках укажи HTTPS-адрес Ayuron Worker. Секретный API-ключ сюда вводить не нужно.")
            .setPositiveButton("Настройки",(d,w)->settingsDialog()).setNegativeButton("Позже",null).show();
        return false;
    }
    private void settingsDialog() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setText(backend());
        input.setHint("https://ayuron-api.example.workers.dev");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this).setTitle("Ayuron API")
            .setMessage("Вставь HTTPS-адрес развёрнутого Cloudflare Worker. Не вставляй сюда Gemini API key.")
            .setView(input).setPositiveButton("Сохранить",(d,w)->{
                String u=input.getText().toString().trim().replaceAll("/+$","");
                if(!u.isEmpty()&&!u.startsWith("https://")) {
                    Toast.makeText(this,"Адрес должен начинаться с https://",Toast.LENGTH_LONG).show(); return;
                }
                prefs.edit().putString("backend_url",u).apply(); refreshUsage();
            }).setNegativeButton("Отмена",null).show();
    }

    private void refreshUsage() {
        if(!backend().startsWith("https://")) { showUsage(null); return; }
        io.execute(()->{
            try {
                JSONObject r=api("GET","/usage?userId="+URLEncoder.encode(installId,"UTF-8"),null);
                main.post(()->showUsage(r.optJSONObject("usage")));
            } catch(Exception e) { main.post(()->showUsage(null)); }
        });
    }
    private void showUsage(JSONObject u) {
        if(quota==null)return;
        quota.setText(u==null?"Текст: —/20     Медиа: —/8":
            "Текст: "+u.optInt("textUsed",0)+"/20     Медиа: "+u.optInt("mediaUsed",0)+"/8");
    }
    private JSONArray recentHistory(int max) {
        JSONArray out=new JSONArray();
        for(int i=Math.max(0,history.length()-max);i<history.length();i++) {
            JSONObject o=history.optJSONObject(i); if(o!=null)out.put(o);
        }
        return out;
    }
    private void pushHistory(String role,String text) {
        JSONObject o=new JSONObject();
        try { o.put("role",role); o.put("text",text); } catch(Exception ignored) {}
        history.put(o); history=recentHistory(10);
        prefs.edit().putString("history",history.toString()).apply();
    }
    private void restoreHistory() {
        for(int i=0;i<history.length();i++) {
            JSONObject o=history.optJSONObject(i); if(o==null)continue;
            String role=o.optString("role","assistant"), text=o.optString("text","");
            if(!text.isEmpty()) bubble((role.equals("user")?"Вы: ":"")+text,role.equals("user"));
        }
    }
    private TextView bubble(String text,boolean user) {
        TextView t=new TextView(this); t.setText(text); t.setTextColor(FG); t.setTextSize(15);
        t.setLineSpacing(dp(2),1.0f); t.setPadding(dp(14),dp(11),dp(14),dp(11));
        t.setBackground(rounded(user?Color.rgb(65,49,35):PANEL,18));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(8);
        chat.addView(t,p); bottom(); return t;
    }
    private void bottom() { if(scroll!=null)scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN)); }
    private void hideKeyboard() {
        try { InputMethodManager im=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(im!=null)im.hideSoftInputFromWindow(composer.getWindowToken(),0); } catch(Exception ignored) {}
    }

    private void sendText() {
        String text=composer.getText().toString().trim(); if(text.isEmpty()||!needBackend())return;
        JSONArray old=recentHistory(10); composer.setText(""); hideKeyboard();
        pushHistory("user",text); bubble("Вы: "+text,true); TextView pending=bubble("Думаю…",false);
        JSONObject body=new JSONObject();
        try { body.put("userId",installId); body.put("model",modelId()); body.put("message",text); body.put("history",old); }
        catch(Exception ignored) {}
        io.execute(()->{
            try {
                JSONObject r=api("POST","/chat",body); String answer=r.optString("answer","(пусто)");
                main.post(()->{pending.setText(answer);pushHistory("assistant",answer);bottom();});
            } catch(Exception e) { main.post(()->pending.setText("Ошибка: "+shortError(e))); }
        });
    }

    private void chooseImage() {
        if(!needBackend())return;
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("image/*");
        startActivityForResult(i,PICK_IMAGE);
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==PICK_IMAGE&&result==RESULT_OK&&data!=null&&data.getData()!=null) {
            String prompt=composer.getText().toString().trim();
            if(prompt.isEmpty())prompt="Опиши это изображение и помоги разобраться, что на нём.";
            composer.setText(""); bubble("📷 Анализ фото",true);
            TextView pending=bubble("Анализирую…",false); Uri uri=data.getData(); String finalPrompt=prompt;
            io.execute(()->analyze(uri,finalPrompt,pending));
        }
    }
    private void analyze(Uri uri,String prompt,TextView pending) {
        try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream bytes=new ByteArrayOutputStream()) {
            if(in==null)throw new Exception("Не удалось открыть файл");
            byte[] buf=new byte[8192]; int n;
            while((n=in.read(buf))!=-1){bytes.write(buf,0,n);if(bytes.size()>6*1024*1024)throw new Exception("Изображение больше 6 МБ");}
            String mime=getContentResolver().getType(uri);
            if(!"image/jpeg".equals(mime)&&!"image/png".equals(mime)&&!"image/webp".equals(mime))
                throw new Exception("Поддерживаются JPG, PNG и WebP");
            JSONObject b=new JSONObject();b.put("userId",installId);b.put("prompt",prompt);b.put("mimeType",mime);
            b.put("data",Base64.encodeToString(bytes.toByteArray(),Base64.NO_WRAP));
            JSONObject r=api("POST","/analyze",b);String ans=r.optString("answer","Изображение обработано.");
            main.post(()->{pending.setText(ans);bottom();});
        } catch(Exception e){main.post(()->pending.setText("Ошибка анализа: "+shortError(e)));}
    }
    private void generateImage() {
        String prompt=composer.getText().toString().trim();
        if(prompt.isEmpty()){Toast.makeText(this,"Напиши, какое изображение создать",Toast.LENGTH_SHORT).show();return;}
        if(!needBackend())return;
        composer.setText("");hideKeyboard();bubble("✨ "+prompt,true);TextView pending=bubble("Создаю изображение…",false);
        JSONObject b=new JSONObject();try{b.put("userId",installId);b.put("prompt",prompt);}catch(Exception ignored){}
        io.execute(()->{
            try {
                JSONObject r=api("POST","/generate-image",b);String ans=r.optString("answer","Готово!");
                String encoded=r.optString("image",""); main.post(()->{pending.setText(ans);if(!encoded.isEmpty())showImage(encoded);bottom();});
            } catch(Exception e){main.post(()->pending.setText("Ошибка генерации: "+shortError(e)));}
        });
    }
    private void showImage(String encoded) {
        try {
            byte[] raw=Base64.decode(encoded,Base64.DEFAULT);Bitmap bitmap=BitmapFactory.decodeByteArray(raw,0,raw.length);
            if(bitmap==null){bubble("Не удалось открыть изображение.",false);return;}
            ImageView image=new ImageView(this);image.setImageBitmap(bitmap);image.setAdjustViewBounds(true);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setBackground(rounded(PANEL,15));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);chat.addView(image,p);
        } catch(Exception e){bubble("Не удалось показать изображение.",false);}
    }

    private JSONObject api(String method,String path,JSONObject body)throws Exception {
        String base=backend();if(!base.startsWith("https://"))throw new Exception("Добавь HTTPS-адрес сервера в настройках");
        HttpURLConnection c=null;
        try {
            c=(HttpURLConnection)new URL(base+path).openConnection();c.setRequestMethod(method);
            c.setConnectTimeout(20000);c.setReadTimeout(90000);c.setRequestProperty("Accept","application/json");
            if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");
                try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
            int status=c.getResponseCode();InputStream s=status>=400?c.getErrorStream():c.getInputStream();String text="";
            if(s!=null)try(BufferedReader r=new BufferedReader(new InputStreamReader(s,StandardCharsets.UTF_8))){
                StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null)b.append(line);text=b.toString();}
            JSONObject obj=text.isEmpty()?new JSONObject():new JSONObject(text);
            if(obj.has("usage")){JSONObject u=obj.optJSONObject("usage");main.post(()->showUsage(u));}
            if(status>=400||!obj.optBoolean("ok",false))throw new Exception(obj.optString("error","HTTP "+status));
            return obj;
        } finally {if(c!=null)c.disconnect();}
    }
    private String shortError(Exception e) {
        String s=e.getMessage();if(s==null||s.trim().isEmpty())return "неизвестная ошибка";
        return s.length()>220?s.substring(0,220):s;
    }
    @Override protected void onDestroy(){super.onDestroy();io.shutdown();}
}
