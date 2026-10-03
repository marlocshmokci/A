package com.ayurones.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(0, 0, 0);
    private static final int PANEL = Color.rgb(18, 18, 18);
    private static final int PANEL_2 = Color.rgb(26, 26, 26);
    private static final int TEXT = Color.rgb(242, 242, 242);
    private static final int MUTED = Color.rgb(150, 150, 150);
    private static final int BORDER = Color.rgb(65, 65, 65);
    private static final int USER_BUBBLE = Color.rgb(35, 35, 35);
    private static final int AI_BUBBLE = Color.rgb(18, 18, 18);

    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Handler main = new Handler(Looper.getMainLooper());

    private SharedPreferences prefs;
    private LinearLayout messages;
    private ScrollView chatScroll;
    private EditText input;
    private TextView statusView;
    private TextView titleView;
    private FrameLayout drawer;
    private LinearLayout historyList;
    private View drawerDim;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);

        prefs = getSharedPreferences("ayurones", MODE_PRIVATE);
        conversationId = prefs.getString("current_conversation", UUID.randomUUID().toString());
        prefs.edit().putString("current_conversation", conversationId).apply();

        buildUi();
        addWelcome();
        refreshStatus();
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(BG);
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(10), dp(8), dp(12), dp(8));

        TextView menu = text("☰", 26, TEXT);
        menu.setGravity(Gravity.CENTER);
        menu.setOnClickListener(v -> openDrawer());
        header.addView(menu, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setPadding(dp(7), 0, 0, 0);
        titleView = text("Ayurones", 20, TEXT);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.addView(titleView);

        statusView = text("проверка соединения…", 12, MUTED);
        heading.addView(statusView);
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));

        TextView more = text("⋮", 28, MUTED);
        more.setGravity(Gravity.CENTER);
        more.setOnClickListener(v -> showActions());
        header.addView(more, new LinearLayout.LayoutParams(dp(40), dp(44)));

        page.addView(header, new LinearLayout.LayoutParams(-1, dp(62)));

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(28, 28, 28));
        page.addView(line, new LinearLayout.LayoutParams(-1, 1));

        chatScroll = new ScrollView(this);
        chatScroll.setFillViewport(true);
        chatScroll.setClipToPadding(false);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(dp(14), dp(14), dp(14), dp(18));
        chatScroll.addView(messages);
        page.addView(chatScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL);
        composer.setPadding(dp(10), dp(6), dp(10), dp(10));

        input = new EditText(this);
        input.setTextColor(TEXT);
        input.setHintTextColor(Color.rgb(115, 115, 115));
        input.setHint("Напишите Ayurones…");
        input.setTextSize(16);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setPadding(dp(16), dp(12), dp(16), dp(12));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setMaxLines(5);
        input.setMinHeight(dp(48));
        input.setBackground(round(BG, BORDER, 2, 22));
        composer.addView(input, new LinearLayout.LayoutParams(0, dp(52), 1));

        Button send = button("➤", PANEL_2, TEXT);
        send.setOnClickListener(v -> sendMessage());
        composer.addView(send, new LinearLayout.LayoutParams(dp(54), dp(52)));
        ((LinearLayout.LayoutParams) send.getLayoutParams()).setMargins(dp(8), 0, 0, 0);

        page.addView(composer, new LinearLayout.LayoutParams(-1, dp(68)));

        drawer = new FrameLayout(this);
        drawer.setVisibility(View.GONE);
        drawer.setBackgroundColor(PANEL);

        drawerDim = new View(this);
        drawerDim.setBackgroundColor(Color.argb(150, 0, 0, 0));
        drawerDim.setOnClickListener(v -> closeDrawer());
        FrameLayout.LayoutParams dimParams = new FrameLayout.LayoutParams(-1, -1);
        dimParams.gravity = Gravity.END;
        dimParams.leftMargin = dp(292);
        drawer.addView(drawerDim, dimParams);

        LinearLayout drawerPanel = new LinearLayout(this);
        drawerPanel.setOrientation(LinearLayout.VERTICAL);
        drawerPanel.setPadding(dp(14), dp(32), dp(14), dp(14));
        drawerPanel.setBackgroundColor(PANEL);
        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(dp(292), -1);
        panelParams.gravity = Gravity.START;
        drawer.addView(drawerPanel, panelParams);

        TextView dTitle = text("Ayurones", 24, TEXT);
        dTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        drawerPanel.addView(dTitle, new LinearLayout.LayoutParams(-1, dp(48)));

        Button newChat = button("+  Новый чат", PANEL_2, TEXT);
        newChat.setOnClickListener(v -> newChat());
        drawerPanel.addView(newChat, gapParams(48, 8));

        Button learn = button("✦  Обучить", PANEL, TEXT);
        learn.setOnClickListener(v -> {
            closeDrawer();
            showLearnDialog();
        });
        drawerPanel.addView(learn, gapParams(48, 4));

        Button settings = button("⚙  Подключение", PANEL, TEXT);
        settings.setOnClickListener(v -> {
            closeDrawer();
            showConnectionDialog();
        });
        drawerPanel.addView(settings, gapParams(48, 12));

        TextView historyHeader = text("История", 13, MUTED);
        historyHeader.setPadding(dp(8), dp(8), dp(8), dp(6));
        drawerPanel.addView(historyHeader);

        ScrollView historyScroll = new ScrollView(this);
        historyList = new LinearLayout(this);
        historyList.setOrientation(LinearLayout.VERTICAL);
        historyScroll.addView(historyList);
        drawerPanel.addView(historyScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        root.addView(drawer, new FrameLayout.LayoutParams(-1, -1));

        root.setFitsSystemWindows(true);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom()
            );
            return insets.consumeSystemWindowInsets();
        });

        setContentView(root);
        root.requestApplyInsets();
        renderHistory();
    }

    private LinearLayout.LayoutParams gapParams(int height, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(height));
        p.setMargins(0, 0, 0, dp(bottom));
        return p;
    }

    private void addWelcome() {
        addMessage(false, "Я Ayurones. Подключите локальный сервер и задайте вопрос — я отвечу, используя модель и вашу базу знаний.");
    }

    private void sendMessage() {
        String message = input.getText().toString().trim();
        if (message.isEmpty()) return;

        input.setText("");
        hideKeyboard();
        addMessage(true, message);
        saveConversationTitle(conversationId, shorten(message));
        renderHistory();
        setBusy(true);

        JSONObject body = new JSONObject();
        try {
            body.put("message", message);
            body.put("conversation_id", conversationId);
        } catch (Exception ignored) {}

        executor.execute(() -> {
            try {
                JSONObject result = postJson("/api/chat", body);
                String answer = result.optString("answer", "Модель не вернула ответ.");
                main.post(() -> {
                    addMessage(false, answer);
                    setBusy(false);
                });
            } catch (Exception e) {
                main.post(() -> {
                    addMessage(false, "Не удалось получить ответ.\n\n" + readableError(e));
                    setBusy(false);
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        input.setEnabled(!busy);
        input.setHint(busy ? "Ayurones думает…" : "Напишите Ayurones…");
    }

    private void addMessage(boolean user, String content) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(user ? Gravity.END : Gravity.START);
        row.setPadding(0, dp(4), 0, dp(4));

        TextView bubble = text(content, 16, TEXT);
        bubble.setTextIsSelectable(true);
        bubble.setLineSpacing(0f, 1.12f);
        bubble.setPadding(dp(14), dp(11), dp(14), dp(11));
        bubble.setBackground(round(user ? USER_BUBBLE : AI_BUBBLE, user ? USER_BUBBLE : BORDER, user ? 0 : 1, 18));

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(315), -2);
        row.addView(bubble, bp);
        messages.addView(row);
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void openDrawer() {
        renderHistory();
        drawer.setVisibility(View.VISIBLE);
    }

    private void closeDrawer() {
        drawer.setVisibility(View.GONE);
    }

    private void newChat() {
        conversationId = UUID.randomUUID().toString();
        prefs.edit().putString("current_conversation", conversationId).apply();
        messages.removeAllViews();
        addWelcome();
        closeDrawer();
    }

    private void renderHistory() {
        if (historyList == null) return;
        historyList.removeAllViews();

        try {
            JSONArray array = new JSONArray(prefs.getString("history", "[]"));
            for (int i = array.length() - 1; i >= 0; i--) {
                String id = array.optString(i);
                if (TextUtils.isEmpty(id)) continue;
                String savedTitle = prefs.getString("title_" + id, "Чат");
                Button item = button(savedTitle, id.equals(conversationId) ? PANEL_2 : PANEL, TEXT);
                item.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                item.setPadding(dp(12), 0, dp(8), 0);
                item.setOnClickListener(v -> loadConversation(id));
                historyList.addView(item, gapParams(48, 4));
            }
        } catch (Exception ignored) {}

        if (historyList.getChildCount() == 0) {
            TextView empty = text("Здесь появятся ваши диалоги.", 14, MUTED);
            empty.setPadding(dp(10), dp(12), dp(10), 0);
            historyList.addView(empty);
        }
    }

    private void loadConversation(String id) {
        conversationId = id;
        prefs.edit().putString("current_conversation", id).apply();
        closeDrawer();
        messages.removeAllViews();

        executor.execute(() -> {
            try {
                JSONObject data = getJson("/api/history/" + id);
                JSONArray items = data.optJSONArray("messages");
                main.post(() -> {
                    if (items == null || items.length() == 0) {
                        addWelcome();
                        return;
                    }
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.optJSONObject(i);
                        if (item != null) {
                            addMessage("user".equals(item.optString("role")), item.optString("content"));
                        }
                    }
                });
            } catch (Exception e) {
                main.post(() -> addMessage(false, "Не удалось загрузить историю: " + readableError(e)));
            }
        });
    }

    private void refreshStatus() {
        statusView.setText("проверка соединения…");
        executor.execute(() -> {
            try {
                JSONObject data = getJson("/api/status");
                boolean online = data.optBoolean("online", false);
                String model = data.optString("model", "неизвестно");
                main.post(() -> statusView.setText(online ? "● онлайн · " + model : "○ сервер недоступен"));
            } catch (Exception e) {
                main.post(() -> statusView.setText("○ сервер недоступен"));
            }
        });
    }

    private void showActions() {
        new AlertDialog.Builder(this)
                .setTitle("Ayurones")
                .setItems(new String[]{"Обучить базу знаний", "Настройки сервера", "Проверить соединение"}, (dialog, which) -> {
                    if (which == 0) showLearnDialog();
                    else if (which == 1) showConnectionDialog();
                    else refreshStatus();
                })
                .show();
    }

    private void showConnectionDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(4), dp(18), 0);

        EditText url = edit("URL API, например http://192.168.1.10:8000");
        url.setSingleLine(true);
        url.setText(prefs.getString("base_url", "http://10.0.2.2:8000"));
        box.addView(url, new LinearLayout.LayoutParams(-1, dp(54)));

        TextView hint = text("На физическом телефоне укажите IP компьютера в одной Wi‑Fi сети.\nНа Android Emulator подойдет http://10.0.2.2:8000.", 13, MUTED);
        hint.setPadding(0, dp(10), 0, dp(4));
        box.addView(hint);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Подключение")
                .setView(box)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", null)
                .create();

        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = url.getText().toString().trim();
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                url.setError("URL должен начинаться с http:// или https://");
                return;
            }
            prefs.edit().putString("base_url", trimSlash(value)).apply();
            dialog.dismiss();
            refreshStatus();
        }));
        dialog.show();
    }

    private void showLearnDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), 0, dp(16), 0);

        EditText title = edit("Название материала");
        title.setSingleLine(true);
        box.addView(title, new LinearLayout.LayoutParams(-1, dp(54)));

        EditText content = edit("Текст для базы знаний");
        content.setGravity(Gravity.TOP | Gravity.START);
        content.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        content.setMinLines(8);
        content.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(210));
        cp.setMargins(0, dp(8), 0, 0);
        box.addView(content, cp);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Обучить Ayurones")
                .setView(box)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Добавить", null)
                .create();

        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t = title.getText().toString().trim();
            String c = content.getText().toString().trim();
            if (c.isEmpty()) {
                content.setError("Введите материал");
                return;
            }
            JSONObject body = new JSONObject();
            try {
                body.put("title", t.isEmpty() ? "Материал" : t);
                body.put("text", c);
            } catch (Exception ignored) {}

            dialog.dismiss();
            executor.execute(() -> {
                try {
                    postJson("/api/learn", body);
                    main.post(() -> toast("Материал добавлен в базу знаний."));
                } catch (Exception e) {
                    main.post(() -> toast("Ошибка обучения: " + readableError(e)));
                }
            });
        }));
        dialog.show();
    }

    private EditText edit(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(TEXT);
        e.setHintTextColor(MUTED);
        e.setTextSize(15);
        e.setBackground(round(BG, BORDER, 2, 14));
        e.setPadding(dp(14), dp(10), dp(14), dp(10));
        return e;
    }

    private TextView text(String value, float size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    private Button button(String label, int background, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(color);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setPadding(dp(10), 0, dp(10), 0);
        b.setBackground(round(background, background == PANEL ? Color.TRANSPARENT : background, 0, 14));
        return b;
    }

    private GradientDrawable round(int fill, int stroke, int strokeWidth, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        if (strokeWidth > 0) d.setStroke(dp(strokeWidth), stroke);
        return d;
    }

    private JSONObject postJson(String path, JSONObject body) throws Exception {
        return requestJson("POST", path, body == null ? null : body.toString());
    }

    private JSONObject getJson(String path) throws Exception {
        return requestJson("GET", path, null);
    }

    private JSONObject requestJson(String method, String path, String body) throws Exception {
        String base = prefs.getString("base_url", "http://10.0.2.2:8000");
        URL url = new URL(trimSlash(base) + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(7000);
        c.setReadTimeout(300000);
        c.setUseCaches(false);
        c.setRequestProperty("Accept", "application/json");
        if (body != null) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream os = c.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        int code = c.getResponseCode();
        InputStream stream = code >= 400 ? c.getErrorStream() : c.getInputStream();
        String response = readAll(stream);
        c.disconnect();

        if (code >= 400) {
            throw new Exception("HTTP " + code + ": " + response);
        }
        return response.isEmpty() ? new JSONObject() : new JSONObject(response);
    }

    private String readAll(InputStream input) throws Exception {
        if (input == null) return "";
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) out.append(line);
        }
        return out.toString();
    }

    private String readableError(Exception e) {
        String m = e.getMessage();
        if (m == null || m.isEmpty()) return "неизвестная ошибка";
        if (m.contains("Failed to connect") || m.contains("Connection refused")) return "сервер не отвечает";
        if (m.contains("UnknownHost")) return "не удалось найти сервер";
        return m;
    }

    private void saveConversationTitle(String id, String title) {
        try {
            JSONArray array = new JSONArray(prefs.getString("history", "[]"));
            boolean exists = false;
            for (int i = 0; i < array.length(); i++) {
                if (id.equals(array.optString(i))) {
                    exists = true;
                    break;
                }
            }
            if (!exists) array.put(id);
            prefs.edit()
                    .putString("history", array.toString())
                    .putString("title_" + id, title)
                    .apply();
        } catch (Exception ignored) {}
    }

    private String shorten(String value) {
        value = value.replace("\n", " ").trim();
        return value.length() <= 38 ? value : value.substring(0, 38) + "…";
    }

    private String trimSlash(String value) {
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return value;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    private void toast(String value) {
        android.widget.Toast.makeText(this, value, android.widget.Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.getVisibility() == View.VISIBLE) {
            closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
