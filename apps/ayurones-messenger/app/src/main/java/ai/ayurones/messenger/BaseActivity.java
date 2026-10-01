package ai.ayurones.messenger;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public abstract class BaseActivity extends Activity {
    LinearLayout base;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
    }
    LinearLayout root() {
        base = new LinearLayout(this);
        base.setOrientation(LinearLayout.VERTICAL);
        base.setBackgroundColor(Color.BLACK);
        base.setPadding(0,0,0,0);
        setContentView(base);
        return base;
    }
    LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(android.view.Gravity.CENTER_VERTICAL);
        return r;
    }
    TextViewCompat hidden;
    static final class TextViewCompat {}
}
