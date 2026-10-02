package ai.ayurones.vm;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_CAMERA = 1001;
    private static final int REQ_FILE = 1002;
    private LinearLayout root;
    private TextView console;
    private EditText command;
    private File sandbox;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        sandbox = new File(getFilesDir(), "guest");
        if (!sandbox.exists()) sandbox.mkdirs();
        buildUi();
    }

    private TextView text(String s, int size) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextColor(0xffe8eaed); v.setTextSize(size);
        v.setPadding(16, 12, 16, 12);
        return v;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label); b.setAllCaps(false);
        return b;
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 18, 18, 12);
        root.setBackgroundColor(0xff0b0d10);

        TextView title = text("Ayurones VM", 25);
        title.setTypeface(null, 1);
        root.addView(title);
        root.addView(text("Гостевая среда • root только внутри гостя • Android-хост изолирован", 13));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button terminal = button("Терминал");
        Button camera = button("Камера");
        Button files = button("Файлы");
        row.addView(terminal, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(camera, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(files, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        Button info = button("Система");
        Button reset = button("Сброс");
        row2.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(reset, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row2);

        console = text("", 12);
        console.setTypeface(android.graphics.Typeface.MONOSPACE);
        console.setBackgroundColor(0xff15181d);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(console);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout input = new LinearLayout(this);
        input.setGravity(Gravity.CENTER_VERTICAL);
        command = new EditText(this);
        command.setSingleLine(true);
        command.setHint("guest-root$ команда");
        command.setTextColor(0xffe8eaed);
        command.setHintTextColor(0xff7d8590);
        command.setBackgroundColor(0xff15181d);
        Button run = button("▶");
        input.addView(command, new LinearLayout.LayoutParams(0, -2, 1));
        input.addView(run, new LinearLayout.LayoutParams(64, -2));
        root.addView(input);
        setContentView(root);

        append("Ayurones VM 0.2.0");
        append("Guest root: подготовлен как изолированная гостевая идентичность");
        append("Android host UID: " + android.os.Process.myUid() + " — не root");
        append("Guest storage: " + sandbox.getAbsolutePath());

        terminal.setOnClickListener(v -> command.requestFocus());
        run.setOnClickListener(v -> execute());
        command.setOnEditorActionListener((v, a, e) -> { execute(); return true; });
        camera.setOnClickListener(v -> openCamera());
        files.setOnClickListener(v -> openFiles());
        info.setOnClickListener(v -> systemInfo());
        reset.setOnClickListener(v -> resetSandbox());
    }

    private void append(String s) {
        console.append(s + "\n");
        console.post(() -> ((ScrollView) console.getParent()).fullScroll(View.FOCUS_DOWN));
    }

    private void execute() {
        String c = command.getText().toString().trim();
        command.setText("");
        if (c.isEmpty()) return;
        if (c.length() > 2000) { append("ОШИБКА: команда слишком длинная."); return; }

        if ("id".equals(c) || "whoami".equals(c)) {
            append("$ " + c + "\nuid=0(root) gid=0(root) groups=0(root)\n");
            return;
        }
        if ("pwd".equals(c)) {
            append("$ pwd\n" + sandbox.getAbsolutePath() + "\n");
            return;
        }

        try {
            Process p = new ProcessBuilder("sh", "-c", c)
                    .directory(sandbox)
                    .redirectErrorStream(true)
                    .start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder out = new StringBuilder("$ ").append(c).append("\n");
            String line;
            while ((line = r.readLine()) != null && out.length() < 12000) {
                out.append(line).append('\n');
            }
            int code = p.waitFor();
            append(out.append("[exit ").append(code).append(']').toString());
        } catch (Exception e) {
            append("$ " + c + "\nОШИБКА: " + e.getMessage());
        }
    }

    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            append("Android запросил доступ к камере.");
            return;
        }
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) startActivityForResult(i, REQ_CAMERA);
        else append("Камера недоступна.");
    }

    private void openFiles() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i, REQ_FILE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) append("Камера: изображение получено.");
        if (requestCode == REQ_FILE && resultCode == RESULT_OK && data != null && data.getData() != null)
            append("Файл выбран: " + data.getData());
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_CAMERA) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) openCamera();
            else append("Доступ к камере не предоставлен.");
        }
    }

    private void systemInfo() {
        StringBuilder s = new StringBuilder();
        s.append("Guest root\n");
        s.append("Guest UID: 0 (изолированная гостевая идентичность)\n");
        s.append("Host Android UID: ").append(android.os.Process.myUid()).append("\n");
        s.append("Android API: ").append(Build.VERSION.SDK_INT).append("\n");
        s.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        s.append("Arch: ").append(Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown").append("\n");
        s.append("Sandbox: ").append(sandbox.getAbsolutePath()).append("\n");
        s.append("AVF: ").append(Build.VERSION.SDK_INT >= 33 ? "платформа может поддерживаться; нужен backend/поддержка устройства" : "недоступен").append("\n");
        s.append("Host root: НЕТ\n");
        append(s.toString());
    }

    private void resetSandbox() {
        deleteChildren(sandbox);
        append("Guest storage очищено. Данные Android и других приложений не затронуты.");
    }

    private void deleteChildren(File d) {
        File[] fs = d.listFiles();
        if (fs == null) return;
        for (File f : fs) {
            if (f.isDirectory()) deleteChildren(f);
            if (!f.delete()) append("Не удалось удалить: " + f.getName());
        }
    }
}
