package ai.ayurones.vm;

import android.app.Activity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.os.Bundle;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
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
        root.addView(text("Изолированная гостевая среда • без root • без разрешений устройства", 13));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button terminal = button("Терминал");
        Button camera = button("Камера");
        Button files = button("Файлы");
        Button info = button("Система");
        Button reset = button("Сброс");
        row.addView(terminal, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(camera, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(files, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(reset, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row);

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
        command.setHint("guest$ команда");
        command.setTextColor(0xffe8eaed);
        command.setHintTextColor(0xff7d8590);
        command.setBackgroundColor(0xff15181d);
        Button run = button("▶");
        input.addView(command, new LinearLayout.LayoutParams(0, -2, 1));
        input.addView(run, new LinearLayout.LayoutParams(64, -2));
        root.addView(input);

        setContentView(root);
        append("Ayurones VM 0.1.0");
        append("Гостевой каталог: " + sandbox.getAbsolutePath());
        append("Команды выполняются от UID приложения внутри его sandbox.");
        append("Сетевой доступ и опасные разрешения приложению не выданы.");

        terminal.setOnClickListener(v -> command.requestFocus());
        run.setOnClickListener(v -> execute());
        command.setOnEditorActionListener((v, a, e) -> { execute(); return true; });
        camera.setOnClickListener(v -> openCamera());
        files.setOnClickListener(v -> openFiles());
        info.setOnClickListener(v -> systemInfo());
        reset.setOnClickListener(v -> resetSandbox());
    }

    private void append(String s) {
        console.append(s + "
");
        console.post(() -> ((ScrollView)console.getParent()).fullScroll(View.FOCUS_DOWN));
    }

    private void execute() {
        String c = command.getText().toString().trim();
        command.setText("");
        if (c.isEmpty()) return;
        if (c.length() > 2000) { append("ОШИБКА: команда слишком длинная."); return; }

        try {
            Process p = new ProcessBuilder("sh", "-c", c)
                    .directory(sandbox)
                    .redirectErrorStream(true)
                    .start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder out = new StringBuilder("$ ").append(c).append("
");
            String line;
            while ((line = r.readLine()) != null) {
                if (out.length() < 12000) out.append(line).append('
');
            }
            int code = p.waitFor();
            append(out.append("[exit ").append(code).append(']').toString());
        } catch (Exception e) {
            append("$ " + c + "
ОШИБКА: " + e.getMessage());
        }
    }

    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
            append("Android запросил доступ к камере.");
            return;
        }
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) startActivityForResult(i, 1001);
        else append("Камера недоступна.");
    }

    private void openFiles() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i, 1002);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 1001 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) openCamera();
        else if (requestCode == 1001) append("Доступ к камере не предоставлен.");
    }

    private void listFiles() {
        StringBuilder s = new StringBuilder("guest files:
");
        appendTree(s, sandbox, "");
        append(s.toString());
    }

    private void appendTree(StringBuilder s, File dir, String prefix) {
        File[] fs = dir.listFiles();
        if (fs == null) return;
        Arrays.sort(fs, Comparator.comparing(File::getName));
        for (File f : fs) {
            s.append(prefix).append(f.isDirectory() ? "[D] " : "[F] ")
             .append(f.getName()).append(f.isFile() ? " (" + f.length() + " B)" : "").append('
');
            if (f.isDirectory()) appendTree(s, f, prefix + "  ");
        }
    }

    private void systemInfo() {
        StringBuilder s = new StringBuilder();
        s.append("Host bridge policy
");
        s.append("Android API: ").append(Build.VERSION.SDK_INT).append('
');
        s.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append('
');
        s.append("Arch: ").append(Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown").append('
');
        s.append("App UID: ").append(android.os.Process.myUid()).append('
');
        s.append("Sandbox: ").append(sandbox.getAbsolutePath()).append('
');
        s.append("AVF platform: ").append(Build.VERSION.SDK_INT >= 33 ? "API family available; hardware/vendor support still required" : "not available").append('
');
        s.append("Network permission: disabled
");
        s.append("Storage permission: disabled
");
        append(s.toString());
    }

    private void resetSandbox() {
        deleteChildren(sandbox);
        append("Guest storage очищено. Данные других приложений не затронуты.");
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
