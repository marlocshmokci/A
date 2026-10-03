package ai.ttauoi.mod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int VERSION_COLOR = 0xFFFE2C55;
    private ModSettings settings;
    private LinearLayout list;

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        settings = new ModSettings(this);
        showMod();
    }

    private TextView text(String value, float size) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(Color.BLACK);
        v.setPadding(dp(18), dp(12), dp(18), dp(12));
        return v;
    }

    private Button button(String value) {
        Button v = new Button(this);
        v.setText(value);
        v.setAllCaps(false);
        return v;
    }

    private void showMod() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        TextView header = text("TikTok AUOI  ·  TTAuOI", 20);
        header.setTextColor(Color.WHITE);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(Color.BLACK);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(58)));

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(10), dp(8), dp(10), dp(24));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        list.addView(text("TTAuOI Mod", 28));
        list.addView(text("TikTok AUOI · " + ModSettings.VERSION, 14));

        Button telegram = button("TTAuOI Telegram Channel");
        telegram.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/TTAuOI"))));
        list.addView(telegram);

        list.addView(text("Подмена региона", 20));
        Button region = button("Регион: " + settings.region());
        region.setOnClickListener(v -> chooseRegion(region));
        list.addView(region);

        list.addView(text("Фильтрация контента", 20));
        slider("Минимум лайков", settings.likes(), 1_000_000, settings::setLikes);
        slider("Минимум просмотров", settings.views(), 10_000_000, settings::setViews);
        slider("Максимальный возраст видео, дней", settings.ageDays(), 3_650, settings::setAgeDays);

        toggle("Убрать трансляции", settings.hideLive(), settings::setHideLive);
        toggle("Убрать рекламу", settings.hideAds(), settings::setHideAds);
        toggle("Убрать фотографии из ленты", settings.hideFeedPhotos(), settings::setHideFeedPhotos);
        toggle("Убрать фотографии из историй", settings.hideStoryPhotos(), settings::setHideStoryPhotos);

        list.addView(text("Внешний вид", 20));
        list.addView(text("Цвет применяется к лайкам, комментариям и кнопке подписки.", 14));

        String[] names = {"Красный","Розовый","Фиолетовый","Синий","Голубой",
                "Бирюзовый","Зелёный","Оранжевый","Жёлтый","Белый"};
        int[] colors = {0xFFFE2C55,0xFFFF4FA3,0xFF8E44FF,0xFF3478F6,0xFF00AEEF,
                0xFF00BFA5,0xFF20A05A,0xFFFF7A00,0xFFFFB800,0xFF555555};

        for (int i = 0; i < names.length; i++) {
            Button color = button(names[i]);
            final int selected = colors[i];
            color.setTextColor(Color.WHITE);
            color.setBackgroundColor(selected);
            color.setOnClickListener(v -> {
                settings.theme(selected);
                restartPrompt();
            });
            list.addView(color, new LinearLayout.LayoutParams(-1, dp(46)));
        }

        list.addView(text("Версия мода: " + ModSettings.VERSION, 14));
    }

    private void slider(String label, int current, int max, IntSetter setter) {
        TextView value = text(label + ": " + current, 15);
        list.addView(value);

        SeekBar bar = new SeekBar(this);
        bar.setMax(1000);
        bar.setProgress((int) ((long) current * 1000L / max));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int progress, boolean fromUser) {
                int v = (int) ((long) progress * max / 1000L);
                value.setText(label + ": " + v);
                setter.set(v);
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) { restartPrompt(); }
        });
        list.addView(bar);
    }

    private void toggle(String label, boolean checked, BoolSetter setter) {
        Switch sw = new Switch(this);
        sw.setText(label);
        sw.setChecked(checked);
        sw.setPadding(dp(8), dp(8), dp(8), dp(8));
        sw.setOnCheckedChangeListener((button, value) -> {
            setter.set(value);
            restartPrompt();
        });
        list.addView(sw);
    }

    private void chooseRegion(Button target) {
        String[] iso = Locale.getISOCountries();
        ArrayList<String> choices = new ArrayList<>();
        for (String code : iso) {
            Locale locale = new Locale("", code);
            choices.add(code + " — " + locale.getDisplayCountry(Locale.getDefault()));
        }
        Collections.sort(choices);

        String[] items = choices.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Выберите регион")
                .setItems(items, (dialog, which) -> {
                    String code = items[which].substring(0, 2);
                    settings.region(code);
                    target.setText("Регион: " + code);
                    restartPrompt();
                })
                .show();
    }

    private void restartPrompt() {
        new AlertDialog.Builder(this)
                .setTitle("Перезагрузить приложение")
                .setMessage("Изменения сохранены. Для применения модификации требуется перезапуск TikTok.")
                .setPositiveButton("Перезапустить", (dialog, which) -> {
                    Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    finishAffinity();
                    if (launch != null) startActivity(launch);
                })
                .setNegativeButton("Позже", null)
                .show();
    }

    private interface IntSetter { void set(int value); }
    private interface BoolSetter { void set(boolean value); }
}
