package ai.ttauoi.feed;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.webkit.*;
import android.graphics.Color;
import android.view.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;

public class MainActivity extends Activity {
    WebView web;
    ArrayList<String> ids = new ArrayList<>();
    android.content.SharedPreferences prefs;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("tta", 0);
        loadSaved();
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Bridge(), "Android");
        setContentView(web);
        handleIntent(getIntent());
        render();
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    void loadSaved() {
        String s = prefs.getString("ids", "");
        if (!s.isEmpty()) for (String x : s.split(",")) {
            if (!x.trim().isEmpty() && !ids.contains(x.trim())) ids.add(x.trim());
        }
    }

    void save() {
        StringBuilder s = new StringBuilder();
        for (String id : ids) { if (s.length()>0) s.append(","); s.append(id); }
        prefs.edit().putString("ids", s.toString()).apply();
    }

    void handleIntent(Intent i) {
        if (i == null) return;
        String value = null;
        if (Intent.ACTION_SEND.equals(i.getAction())) {
            value = i.getStringExtra(Intent.EXTRA_TEXT);
        } else if (Intent.ACTION_VIEW.equals(i.getAction()) && i.getData() != null) {
            value = i.getData().toString();
        }
        if (value != null) addTikTok(value);
    }

    void addTikTok(String input) {
        if (input == null) return;
        Matcher direct = Pattern.compile("(?i)(?:video/|player/v1/|/video/)(\\d{10,25})").matcher(input);
        if (direct.find()) {
            addId(direct.group(1));
            return;
        }
        if (input.contains("tiktok.com/")) {
            new Thread(() -> {
                String resolved = resolve(input);
                Matcher m = Pattern.compile("(?i)(?:video/)(\\d{10,25})").matcher(resolved == null ? input : resolved);
                if (m.find()) runOnUiThread(() -> addId(m.group(1)));
                else runOnUiThread(() -> web.evaluateJavascript("toast('Не удалось распознать TikTok-видео');", null));
            }).start();
        } else {
            web.evaluateJavascript("toast('Нужна ссылка на TikTok-видео');", null);
        }
    }

    String resolve(String input) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(input.trim()).openConnection();
            c.setInstanceFollowRedirects(true);
            c.setConnectTimeout(4500);
            c.setReadTimeout(4500);
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) TTA/1.0");
            c.setRequestMethod("GET");
            c.connect();
            String u = c.getURL().toString();
            c.disconnect();
            return u;
        } catch (Exception e) { return input; }
    }

    void addId(String id) {
        if (ids.contains(id)) {
            web.evaluateJavascript("toast('Видео уже в ленте');", null);
            return;
        }
        ids.add(0, id);
        save();
        render();
    }

    void removeTikTok(String id) {
        ids.remove(id);
        save();
        render();
    }

    void clearAll() {
        ids.clear();
        save();
        render();
    }

    String esc(String s) {
        return s.replace("\\","\\\\").replace("'","\\'");
    }

    void render() {
        StringBuilder cards = new StringBuilder();
        if (ids.isEmpty()) {
            cards.append("<section class='empty'><div class='logo'>TTA</div><h1>Твоя лента</h1><p>В TikTok нажми «Поделиться» → TTA. Ссылка приходит сюда сразу, без копирования и вставки.</p><div class='hint'>TTA принимает TikTok Share автоматически</div></section>");
        }
        for (String id : ids) {
            cards.append("<section class='card'><iframe src='https://www.tiktok.com/player/v1/")
                .append(id).append("?controls=1&description=1&music_info=1&loop=1' allow='fullscreen; autoplay' loading='lazy'></iframe>")
                .append("<div class='tools'><button onclick="Android.remove('").append(esc(id)).append("')">Убрать</button><button onclick="Android.open('").append(esc(id)).append("')">TikTok</button></div></section>");
        }

        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,user-scalable=no'><style>"
          + "*{box-sizing:border-box}html,body{margin:0;background:#000;color:#fff;font-family:Arial,sans-serif}body{padding-bottom:76px}.top{position:sticky;top:0;z-index:20;background:rgba(0,0,0,.96);padding:12px 14px;border-bottom:1px solid #202020;backdrop-filter:blur(12px)}.brand{font-size:24px;font-weight:900;letter-spacing:-.7px}.sub{font-size:11px;color:#777;margin-top:2px}.row{display:flex;gap:8px;margin-top:10px}.input{flex:1;background:#151515;border:1px solid #333;color:#fff;border-radius:14px;padding:12px;font-size:14px;outline:none}.add{background:#fff;color:#000;border:0;border-radius:14px;padding:0 15px;font-weight:800}.feed{scroll-snap-type:y mandatory}.card{height:calc(100vh - 174px);min-height:500px;position:relative;scroll-snap-align:start;display:flex;align-items:center;justify-content:center;border-bottom:1px solid #151515;background:#050505}.card iframe{width:100%;height:100%;border:0}.tools{position:absolute;bottom:14px;right:12px;display:flex;gap:8px}.tools button{background:#000c;color:#fff;border:1px solid #555;border-radius:12px;padding:8px 11px}.empty{height:66vh;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;padding:28px}.logo{font-size:72px;font-weight:950;letter-spacing:-5px}.empty h1{font-size:25px;margin:7px 0}.empty p{color:#aaa;line-height:1.45;max-width:340px}.hint{margin-top:12px;color:#666;font-size:12px}.tabs{position:fixed;z-index:30;bottom:0;left:0;right:0;height:68px;background:#080808;border-top:1px solid #222;display:flex;align-items:center;justify-content:space-around}.tab{font-size:11px;color:#888;text-align:center;padding:10px 12px}.tab b{display:block;font-size:20px;margin-bottom:2px}.tab.active{color:#fff}.page{display:none;padding-bottom:80px}.page.show{display:block}.section{padding:16px}.section h2{font-size:22px;margin:8px 0 16px}.item{display:flex;align-items:center;justify-content:space-between;padding:14px 0;border-bottom:1px solid #202020}.label{font-size:15px}.small{font-size:12px;color:#777;margin-top:3px}.switch{accent-color:#fff}.select,.num{background:#151515;color:#fff;border:1px solid #333;border-radius:10px;padding:9px}.colors{display:grid;grid-template-columns:repeat(5,1fr);gap:8px}.c{height:36px;border-radius:10px;border:1px solid #333}.profile{padding:28px 18px}.avatar{width:76px;height:76px;border-radius:50%;border:1px solid #333;display:flex;align-items:center;justify-content:center;font-size:27px;font-weight:900}.danger{color:#ff6b6b}.toast{position:fixed;left:50%;bottom:84px;transform:translateX(-50%);background:#222;color:#fff;padding:10px 13px;border-radius:12px;display:none;z-index:50}</style></head><body>"
          + "<div class='top'><div class='brand'>TTA</div><div class='sub'>TikTok-контент, твоя лента и твои настройки</div><div class='row'><input id='u' class='input' placeholder='Ссылка TikTok'><button class='add' onclick='add()'>Добавить</button></div></div>"
          + "<div id='feed' class='page show'><main class='feed'>"+cards+"</main></div>"
          + "<div id='following' class='page'><div class='section'><h2>Подписки</h2><div class='empty' style='height:55vh'><h2>Пока пусто</h2><p>Позже здесь появятся профили, на которые ты подпишешься в TTA.</p></div></div></div>"
          + "<div id='inbox' class='page'><div class='section'><h2>Входящие</h2><div class='item'><div><div class='label'>Новые видео</div><div class='small'>Видео из TikTok приходят через системную кнопку «Поделиться».</div></div></div></div></div>"
          + "<div id='profile' class='page'><div class='profile'><div class='avatar'>TTA</div><h2>Профиль TTA</h2><div class='small'>Локальная лента: "+ids.length+" видео</div></div><div class='section'><h2>Настройки</h2>"
          + "<div class='item'><div><div class='label'>Регион</div><div class='small'>Применяется после перезапуска клиента</div></div><select id='region' class='select' onchange='savePref()'><option>DE</option><option>US</option><option>GB</option><option>FR</option><option>PL</option><option>TR</option><option>JP</option><option>KR</option><option>BR</option><option>OTHER</option></select></div>"
          + "<div class='item'><div><div class='label'>Минимум лайков</div><div class='small'>Фильтр для синхронизированной ленты</div></div><input id='likes' class='num' type='number' min='0' value='0' onchange='savePref()'></div>"
          + "<div class='item'><div><div class='label'>Минимум просмотров</div><div class='small'>Фильтр для синхронизированной ленты</div></div><input id='views' class='num' type='number' min='0' value='0' onchange='savePref()'></div>"
          + "<div class='item'><div><div class='label'>Максимальный возраст</div><div class='small'>Дни; 0 = без фильтра</div></div><input id='age' class='num' type='number' min='0' value='0' onchange='savePref()'></div>"
          + "<div class='item'><div><div class='label'>Скрывать LIVE</div></div><input id='live' class='switch' type='checkbox' onchange='savePref()'></div>"
          + "<div class='item'><div><div class='label'>Скрывать рекламу</div></div><input id='ads' class='switch' type='checkbox' checked onchange='savePref()'></div>"
          + "<div class='item'><div><div class='label'>Скрывать фото в ленте</div></div><input id='photos' class='switch' type='checkbox' onchange='savePref()'></div>"
          + "<div class='item'><div><div class='label'>Скрывать фото в Stories</div></div><input id='stories' class='switch' type='checkbox' onchange='savePref()'></div>"
          + "<div class='item' style='display:block'><div class='label'>Цвет TTA</div><div class='small' style='margin-bottom:10px'>Цвет кнопок и акцентов</div><div class='colors'><button class='c' style='background:#ffffff' onclick='accent(\'#fff\')'></button><button class='c' style='background:#ff0050' onclick='accent(\'#ff0050\')'></button><button class='c' style='background:#00f2ea' onclick='accent(\'#00f2ea\')'></button><button class='c' style='background:#7c4dff' onclick='accent(\'#7c4dff\')'></button><button class='c' style='background:#00c853' onclick='accent(\'#00c853\')'></button><button class='c' style='background:#ff9800' onclick='accent(\'#ff9800\')'></button><button class='c' style='background:#ffd600' onclick='accent(\'#ffd600\')'></button><button class='c' style='background:#00b8d4' onclick='accent(\'#00b8d4\')'></button><button class='c' style='background:#e040fb' onclick='accent(\'#e040fb\')'></button><button class='c' style='background:#8bc34a' onclick='accent(\'#8bc34a\')'></button></div></div>"
          + "<div class='item' onclick='clearAll()'><div><div class='label danger'>Очистить ленту</div><div class='small'>Удалить все импортированные видео с устройства</div></div></div>"
          + "</div></div>"
          + "<nav class='tabs'><div class='tab active' onclick='page(\'feed\',this)'><b>⌂</b>Для тебя</div><div class='tab' onclick='page(\'following\',this)'><b>＋</b>Подписки</div><div class='tab' onclick='page(\'inbox\',this)'><b>♡</b>Входящие</div><div class='tab' onclick='page(\'profile\',this)'><b>◉</b>Профиль</div></nav><div id='toast' class='toast'></div>"
          + "<script>function add(){let u=document.getElementById('u').value.trim();if(u)Android.add(u);document.getElementById('u').value=''}function toast(t){let e=document.getElementById('toast');e.innerText=t;e.style.display='block';setTimeout(()=>e.style.display='none',1800)}function page(id,el){document.querySelectorAll('.page').forEach(x=>x.classList.remove('show'));document.getElementById(id).classList.add('show');document.querySelectorAll('.tab').forEach(x=>x.classList.remove('active'));el.classList.add('active')}function clearAll(){Android.clear()}function accent(c){document.documentElement.style.setProperty('--accent',c);document.querySelectorAll('.add').forEach(x=>{x.style.background=c;x.style.color='#000'});localStorage.setItem('accent',c);toast('Цвет сохранён')}function savePref(){let p={region:region.value,likes:likes.value,views:views.value,age:age.value,live:live.checked,ads:ads.checked,photos:photos.checked,stories:stories.checked};localStorage.setItem('prefs',JSON.stringify(p));toast('Сохранено — некоторые изменения требуют перезапуска')}try{let a=localStorage.getItem('accent');if(a)accent(a);let p=JSON.parse(localStorage.getItem('prefs')||'{}');if(p.region)region.value=p.region;if(p.likes)likes.value=p.likes;if(p.views)views.value=p.views;if(p.age)age.value=p.age;if(typeof p.live==='boolean')live.checked=p.live;if(typeof p.ads==='boolean')ads.checked=p.ads;if(typeof p.photos==='boolean')photos.checked=p.photos;if(typeof p.stories==='boolean')stories.checked=p.stories}catch(e){}</script></body></html>";
        web.loadDataWithBaseURL("https://www.tiktok.com/", html, "text/html", "UTF-8", null);
    }

    public class Bridge {
        @JavascriptInterface public void add(String s) { runOnUiThread(() -> addTikTok(s)); }
        @JavascriptInterface public void remove(String id) { runOnUiThread(() -> removeTikTok(id)); }
        @JavascriptInterface public void clear() { runOnUiThread(() -> clearAll()); }
        @JavascriptInterface public void open(String id) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/player/v1/"+id))); } catch(Exception ignored) {}
        }
    }
}