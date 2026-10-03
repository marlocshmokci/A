package ai.ttauoi.mod;

import android.content.Context;
import android.content.SharedPreferences;

public final class ModSettings {
    public static final String VERSION = "v2.0.5";
    private static final String PREFS = "ttauoi_mod";

    private final SharedPreferences p;

    public ModSettings(Context context) {
        p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String region() {
        return p.getString("region", java.util.Locale.getDefault().getCountry());
    }

    public void region(String value) {
        p.edit().putString("region", value).apply();
    }

    public int likes() { return p.getInt("likes", 0); }
    public int views() { return p.getInt("views", 0); }
    public int ageDays() { return p.getInt("ageDays", 3650); }

    public boolean hideLive() { return p.getBoolean("hideLive", false); }
    public boolean hideAds() { return p.getBoolean("hideAds", false); }
    public boolean hideFeedPhotos() { return p.getBoolean("hideFeedPhotos", false); }
    public boolean hideStoryPhotos() { return p.getBoolean("hideStoryPhotos", false); }

    public int theme() { return p.getInt("theme", 0xFFFE2C55); }

    public void setLikes(int value) { p.edit().putInt("likes", value).apply(); }
    public void setViews(int value) { p.edit().putInt("views", value).apply(); }
    public void setAgeDays(int value) { p.edit().putInt("ageDays", value).apply(); }

    public void setHideLive(boolean value) { p.edit().putBoolean("hideLive", value).apply(); }
    public void setHideAds(boolean value) { p.edit().putBoolean("hideAds", value).apply(); }
    public void setHideFeedPhotos(boolean value) { p.edit().putBoolean("hideFeedPhotos", value).apply(); }
    public void setHideStoryPhotos(boolean value) { p.edit().putBoolean("hideStoryPhotos", value).apply(); }

    public void theme(int value) { p.edit().putInt("theme", value).apply(); }
}
