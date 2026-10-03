package ai.ayurones.vm;

import android.content.Context;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class GuestImage {
    public static final String ASSET = "guest/ayurones-guest.ayr";
    private GuestImage() {}

    public static long size(Context c) {
        try {
            return c.getAssets().openFd(ASSET).getLength();
        } catch (Exception ignored) {
            try {
                InputStream in = c.getAssets().open("guest/README.runtime");
                long total = 0;
                byte[] b = new byte[65536];
                int n;
                while ((n = in.read(b)) > 0) total += n;
                in.close();
                return total;
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public static String header(Context c) {
        try {
            InputStream in = c.getAssets().open(ASSET);
            byte[] b = new byte[256];
            int n = in.read(b);
            in.close();
            if (n <= 0) return "empty";
            return new String(b, 0, n, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            return "Guest image unavailable";
        }
    }

    public static String format(long bytes) {
        if (bytes >= 1073741824L) return String.format(Locale.US, "%.2f GB", bytes / 1073741824.0);
        return String.format(Locale.US, "%.1f MB", bytes / 1048576.0);
    }
}