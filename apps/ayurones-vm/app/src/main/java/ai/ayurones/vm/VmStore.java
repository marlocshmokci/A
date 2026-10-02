package ai.ayurones.vm;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class VmStore {
    private static final String PREFS = "ayurones_vm_store";
    private static final String KEY = "machines";

    public static final class VM {
        public String id;
        public String name;
        public String version;
        public String state;
        public long createdAt;
        public boolean developer;

        public VM(String id, String name, String version, String state, long createdAt, boolean developer) {
            this.id = id; this.name = name; this.version = version; this.state = state;
            this.createdAt = createdAt; this.developer = developer;
        }
    }

    private VmStore() {}

    public static List<VM> load(Context c) {
        ArrayList<VM> out = new ArrayList<>();
        String raw = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        try {
            JSONArray a = new JSONArray(raw);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                out.add(new VM(
                    o.optString("id"),
                    o.optString("name", "Ayurones VM"),
                    o.optString("version", "Android 16"),
                    o.optString("state", "stopped"),
                    o.optLong("createdAt", System.currentTimeMillis()),
                    o.optBoolean("developer", false)
                ));
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static void save(Context c, List<VM> list) {
        JSONArray a = new JSONArray();
        for (VM v : list) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", v.id);
                o.put("name", v.name);
                o.put("version", v.version);
                o.put("state", v.state);
                o.put("createdAt", v.createdAt);
                o.put("developer", v.developer);
                a.put(o);
            } catch (Exception ignored) {}
        }
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, a.toString()).apply();
    }

    public static VM create(Context c, String version) {
        List<VM> list = load(c);
        String id = "vm_" + System.currentTimeMillis();
        VM v = new VM(id, "Ayurones " + version.replace("Android ", ""), version, "preparing",
                System.currentTimeMillis(), false);
        list.add(0, v);
        save(c, list);
        return v;
    }

    public static void setState(Context c, String id, String state) {
        List<VM> list = load(c);
        for (VM v : list) if (v.id.equals(id)) v.state = state;
        save(c, list);
    }

    public static void setDeveloper(Context c, String id, boolean unlocked) {
        List<VM> list = load(c);
        for (VM v : list) if (v.id.equals(id)) v.developer = unlocked;
        save(c, list);
    }

    public static void delete(Context c, String id) {
        List<VM> list = load(c);
        list.removeIf(v -> v.id.equals(id));
        save(c, list);
    }
}