package ai.ttauoi.mod;
import android.content.*;
public final class ModSettings {
 public static final String VERSION="v2.0.5";
 private final SharedPreferences p;
 public ModSettings(Context c){p=c.getSharedPreferences("ttauoi_mod",Context.MODE_PRIVATE);}
 public String region(){return p.getString("region",java.util.Locale.getDefault().getCountry());}
 public void region(String v){p.edit().putString("region",v).apply();}
 public int likes(){return p.getInt("likes",0);}
 public int views(){return p.getInt("views",0);}
 public int ageDays(){return p.getInt("ageDays",3650);}
 public boolean hideLive(){return p.getBoolean("hideLive",false);}
 public boolean hideAds(){return p.getBoolean("hideAds",false);}
 public boolean hideFeedPhotos(){return p.getBoolean("hideFeedPhotos",false);}
 public boolean hideStoryPhotos(){return p.getBoolean("hideStoryPhotos",false);}
 public int theme(){return p.getInt("theme",0xFFFE2C55);}
 public void set(String key,Object value){SharedPreferences.Editor e=p.edit();if(value instanceof Boolean)e.putBoolean(key,(Boolean)value);else if(value instanceof Integer)e.putInt(key,(Integer)value);else e.putString(key,String.valueOf(value));e.apply();}
}
