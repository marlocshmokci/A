package ai.ttauoi.mod;

import android.content.Context;
import android.content.SharedPreferences;

public final class ModSettings {
    public static final String VERSION = "v2.0.5";
    public static final String TELEGRAM = "https://t.me/TTAuOI";
    private static final String PREFS = "ttauoi_mod";

    private final SharedPreferences p;

    public ModSettings(Context context) {
        p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String region() { return p.getString("region", ""); }
    public void region(String value) { p.edit().putString("region", value).apply(); }

    public int likes() { return p.getInt("likes", 0); }
    public int views() { return p.getInt("views", 0); }
    public int ageDays() { return p.getInt("ageDays", 3650); }

    public boolean hideLive() { return p.getBoolean("hideLive", false); }
    public boolean hideAds() { return p.getBoolean("hideAds", false); }
    public boolean hideFeedPhotos() { return p.getBoolean("hideFeedPhotos", false); }
    public boolean hideStoryPhotos() { return p.getBoolean("hideStoryPhotos", false); }

    public int theme() { return p.getInt("theme", 0xFFFE2C55); }

    public void setLikes(int v) { p.edit().putInt("likes", Math.max(0, v)).apply(); }
    public void setViews(int v) { p.edit().putInt("views", Math.max(0, v)).apply(); }
    public void setAgeDays(int v) { p.edit().putInt("ageDays", Math.max(0, v)).apply(); }

    public void setHideLive(boolean v) { p.edit().putBoolean("hideLive", v).apply(); }
    public void setHideAds(boolean v) { p.edit().putBoolean("hideAds", v).apply(); }
    public void setHideFeedPhotos(boolean v) { p.edit().putBoolean("hideFeedPhotos", v).apply(); }
    public void setHideStoryPhotos(boolean v) { p.edit().putBoolean("hideStoryPhotos", v).apply(); }

    public void theme(int v) { p.edit().putInt("theme", v).apply(); }
}
