package ai.ayurones.messenger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public final class Ui {
    private Ui() {}
    static int dp(Context c, int n) { return Math.round(n * c.getResources().getDisplayMetrics().density); }

    static TextView text(Context c, String value, float size) {
        TextView v = new TextView(c);
        v.setText(value);
        v.setTextColor(Color.WHITE);
        v.setTextSize(size);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10));
        return v;
    }

    static GradientDrawable bg(int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        if (stroke != 0) d.setStroke(1, stroke);
        d.setCornerRadius(radius);
        return d;
    }

    static Button button(Context c, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setBackground(bg(Color.rgb(25,25,25), Color.rgb(70,70,70), dp(c,16)));
        return b;
    }

    static TextView divider(Context c) {
        TextView v = new TextView(c);
        v.setBackgroundColor(Color.rgb(35,35,35));
        v.setHeight(dp(c, 1));
        return v;
    }

    static void styleRoot(View v, Context c) {
        v.setBackgroundColor(Color.BLACK);
    }
}
