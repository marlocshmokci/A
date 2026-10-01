package ai.ayurones.messenger;

import android.content.Context;
import android.media.MediaPlayer;

public final class AudioPack {
    private AudioPack() {}

    static final String[] IDS = {
        "send_chime","soft_pop","glass_ping","digital_wave","warm_bell",
        "focus_tone","airy_note","pulse_note","success_chord","classic_message"
    };

    static final String[] NAMES = {
        "Chime","Soft Pop","Glass Ping","Digital Wave","Warm Bell",
        "Focus Tone","Airy Note","Pulse Note","Success Chord","Classic Message"
    };

    static MediaPlayer player;

    static void play(Context c, String id) {
        if (!Store.sendSound(c)) return;
        int res = resource(id);
        if (res == 0) return;
        try {
            stop();
            player = MediaPlayer.create(c, res);
            if (player != null) {
                player.setOnCompletionListener(mp -> stop());
                player.start();
            }
        } catch (Exception ignored) {}
    }

    static void stop() {
        try {
            if (player != null) {
                player.stop();
                player.release();
            }
        } catch (Exception ignored) {}
        player = null;
    }

    static int resource(String id) {
        switch (id) {
            case "send_chime": return R.raw.send_chime;
            case "soft_pop": return R.raw.soft_pop;
            case "glass_ping": return R.raw.glass_ping;
            case "digital_wave": return R.raw.digital_wave;
            case "warm_bell": return R.raw.warm_bell;
            case "focus_tone": return R.raw.focus_tone;
            case "airy_note": return R.raw.airy_note;
            case "pulse_note": return R.raw.pulse_note;
            case "success_chord": return R.raw.success_chord;
            case "classic_message": return R.raw.classic_message;
            default: return R.raw.send_chime;
        }
    }

    static String name(String id) {
        for (int i=0;i<IDS.length;i++) if (IDS[i].equals(id)) return NAMES[i];
        return "Chime";
    }
}
