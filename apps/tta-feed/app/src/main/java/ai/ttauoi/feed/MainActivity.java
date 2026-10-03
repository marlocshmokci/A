package ai.ttauoi.feed;

import android.app.*;
import android.os.*;
import android.content.*;
import android.webkit.*;
import android.graphics.Color;
import java.util.*;
import java.util.regex.*;

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
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(true);
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Bridge(), "Android");
        setContentView(web);
        handleIntent(getIntent());
        render();
    }

    void loadSaved() {
        String s = prefs.getString("ids", "");
        if (!s.isEmpty()) for (String x : s.split(",")) if (!x.trim().isEmpty()) ids.add(x.trim());
    }

    void save() {
        StringBuilder s = new StringBuilder();
        for (String id : ids) { if (s.length()>0) s.append(","); s.append(id); }
        prefs.edit().putString("ids", s.toString()).apply();
    }

    void handleIntent(Intent i) {
        if (i != null && Intent.ACTION_SEND.equals(i.getAction())) {
            String t = i.getStringExtra(Intent.EXTRA_TEXT);
            if (t != null) addTikTok(t);
        }
    }

    void addTikTok(String input) {
        Matcher m = Pattern.compile("(?:video/|player/v1/)(\\d{10,25})").matcher(input);
        if (m.find()) {
            String id = m.group(1);
            if (!ids.contains(id)) { ids.add(0, id); save(); render(); }
        } else {
            web.evaluateJavascript("alert('Нужна ссылка на TikTok-видео');", null);
        }
    }

    void removeTikTok(String id) {
        ids.remove(id); save(); render();
    }

    void render() {
        StringBuilder cards = new StringBuilder();
        if (ids.isEmpty()) {
            cards.append("<section class='empty'><div class='logo'>TTA</div><h1>Твоя лента</h1><p>Отправь видео из TikTok через «Поделиться → TTA» или вставь ссылку сверху.</p></section>");
        }
        for (String id : ids) {
            cards.append("<section class='card'><iframe src='https://www.tiktok.com/player/v1/")
                .append(id).append("?controls=1&description=1&music_info=1&loop=1' allow='fullscreen; autoplay' loading='lazy'></iframe>")
                .append("<button class='del' onclick=\"Android.remove('" ).append(id).append("')\">Удалить</button></section>");
        }
        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><style>"
          + "*{box-sizing:border-box}html,body{margin:0;background:#000;color:#fff;font-family:Arial,sans-serif}body{padding-bottom:70px}.top{position:sticky;top:0;z-index:5;background:#000;padding:12px 14px;border-bottom:1px solid #222}.brand{font-size:22px;font-weight:800;margin-bottom:9px}.row{display:flex;gap:8px}.input{flex:1;background:#171717;border:1px solid #333;color:#fff;border-radius:12px;padding:12px;font-size:14px}.add{background:#fff;color:#000;border:0;border-radius:12px;padding:0 15px;font-weight:700}.card{height:calc(100vh - 125px);min-height:520px;position:relative;scroll-snap-align:start;display:flex;align-items:center;justify-content:center;border-bottom:1px solid #222}.feed{scroll-snap-type:y mandatory}.card iframe{width:100%;height:100%;border:0}.del{position:absolute;right:12px;bottom:14px;background:#000cc;color:#fff;border:1px solid #555;border-radius:10px;padding:8px 12px}.empty{height:70vh;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;padding:30px}.logo{font-size:64px;font-weight:900}.empty p{color:#aaa;max-width:340px;line-height:1.5}</style></head><body>"
          + "<div class='top'><div class='brand'>TTA</div><div class='row'><input id='u' class='input' placeholder='Вставь ссылку TikTok'><button class='add' onclick='add()'>Добавить</button></div></div><main class='feed'>"
          + cards + "</main><script>function add(){let u=document.getElementById('u').value;Android.add(u);document.getElementById('u').value=''} </script></body></html>";
        web.loadDataWithBaseURL("https://www.tiktok.com/", html, "text/html", "UTF-8", null);
    }

    public class Bridge {
        @JavascriptInterface public void add(String s) { runOnUiThread(() -> addTikTok(s)); }
        @JavascriptInterface public void remove(String id) { runOnUiThread(() -> removeTikTok(id)); }
    }
}