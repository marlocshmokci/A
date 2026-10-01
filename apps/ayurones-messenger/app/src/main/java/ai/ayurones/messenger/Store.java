package ai.ayurones.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Store {
    private static final String PREF = "ayurones";
    private Store() {}

    static SharedPreferences p(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    static int points(Context c) { return p(c).getInt("points", 250); }
    static void addPoints(Context c, int amount) {
        p(c).edit().putInt("points", Math.max(0, points(c) + amount)).apply();
    }

    static Set<String> contacts(Context c) {
        Set<String> s = p(c).getStringSet("contacts", null);
        return s == null ? new HashSet<>() : new HashSet<>(s);
    }

    static void addContact(Context c, String name) {
        Set<String> s = contacts(c);
        s.add(name);
        p(c).edit().putStringSet("contacts", s).apply();
    }

    static List<String> messages(Context c, String chat) {
        List<String> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(p(c).getString("chat_" + chat, "[]"));
            for (int i=0;i<a.length();i++) out.add(a.getString(i));
        } catch (Exception ignored) {}
        return out;
    }

    static void addMessage(Context c, String chat, String text) {
        try {
            JSONArray a = new JSONArray(p(c).getString("chat_" + chat, "[]"));
            a.put(text);
            p(c).edit()
                .putString("chat_" + chat, a.toString())
                .putString("preview_" + chat, text)
                .apply();
        } catch (Exception ignored) {}
    }

    static String preview(Context c, String chat) {
        String s = p(c).getString("preview_" + chat, "");
        return s.isEmpty() ? "Новый чат" : s;
    }

    static void dailyBonus(Context c) {
        Calendar now = Calendar.getInstance();
        String today = now.get(Calendar.YEAR) + "-" + now.get(Calendar.DAY_OF_YEAR);
        String last = p(c).getString("daily", "");
        if (!today.equals(last)) {
            addPoints(c, 20);
            p(c).edit().putString("daily", today).apply();
        }
    }

    static Uri avatar(Context c) {
        String v = p(c).getString("avatar", "");
        return v.isEmpty() ? null : Uri.parse(v);
    }

    static void setAvatar(Context c, Uri uri) {
        p(c).edit().putString("avatar", uri.toString()).apply();
    }

    static boolean owned(Context c, String id) {
        return p(c).getBoolean("owned_" + id, false);
    }

    static void buy(Context c, String id) {
        p(c).edit().putBoolean("owned_" + id, true).apply();
    }

    static String equipped(Context c) {
        return p(c).getString("decoration", "");
    }

    static void equip(Context c, String id) {
        p(c).edit().putString("decoration", id).apply();
    }

    static boolean unlimited(Context c) {
        return p(c).getBoolean("unlimited", false);
    }

    static void setUnlimited(Context c, boolean v) {
        p(c).edit().putBoolean("unlimited", v).apply();
    }

    static final class Decoration {
        final String id, title, icon;
        final int cost;
        final String detail;
        Decoration(String id, String title, String icon, int cost, String detail) {
            this.id=id; this.title=title; this.icon=icon; this.cost=cost; this.detail=detail;
        }
    }
}
