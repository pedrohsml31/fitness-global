package com.pedro.fitnessglobal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

/**
 * Toca o som do fim do descanso. Quem chama e o AlarmManager, na hora exata, entao
 * funciona com a tela apagada e com o app fechado — o JavaScript esta congelado.
 *
 * Por que nao e o som da propria notificacao: o Android CALA som de notificacao no
 * silencioso e no vibrar, com ou sem fone. A regra do Pedro e outra:
 *   - com fone: toca SEMPRE, mesmo no silencioso/vibrar. Vai pelo canal de MIDIA, que o
 *     modo silencioso nao afeta e que sai no fone;
 *   - sem fone: so toca se ele ligou "tocar no alto-falante" no app, e ai RESPEITA o
 *     silencioso (alto-falante tocando com o celular no silencioso seria desobedecer).
 *
 * A decisao e tomada AGORA, na hora de tocar, e nao quando o descanso comecou: se ele
 * colocou ou tirou o fone no meio do descanso, vale o estado de agora.
 */
public class RestSoundReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.pedro.fitnessglobal.REST_SOUND";

    static boolean headsetOn(AudioManager am) {
        if (am == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                int t = d.getType();
                if (t == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || t == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || t == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || t == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                        || t == AudioDeviceInfo.TYPE_USB_HEADSET) {
                    return true;
                }
            }
            return false;
        }
        //noinspection deprecation
        return am.isWiredHeadsetOn() || am.isBluetoothA2dpOn();
    }

    /** Se vai tocar e por qual caminho: "fone", "alto-falante" ou o motivo de nao tocar. */
    static String decide(Context ctx, boolean spk) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return "sem-audio";
        if (headsetOn(am)) return "fone";
        if (!spk) return "sem-fone";
        if (am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) return "silencioso";
        return "alto-falante";
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String sound = intent.getStringExtra("sound");
        boolean spk = intent.getBooleanExtra("spk", false);
        String via = decide(ctx, spk);
        if (!"fone".equals(via) && !"alto-falante".equals(via)) return;
        play(ctx, sound, "fone".equals(via), goAsync());
    }

    /** Toca o arquivo de res/raw. pending pode ser null (teste com o app aberto). */
    static void play(Context ctx, String sound, boolean viaFone, final PendingResult pending) {
        final AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        int resId = ctx.getResources().getIdentifier(sound == null ? "" : sound, "raw", ctx.getPackageName());
        if (resId == 0) resId = ctx.getResources().getIdentifier("rest_medio", "raw", ctx.getPackageName());
        if (resId == 0) { if (pending != null) pending.finish(); return; }

        /* fone: MIDIA (o silencioso nao mexe nela) · alto-falante: NOTIFICACAO (segue o volume
           de notificacao que ele ja ajustou, e o silencioso ja foi conferido acima) */
        final AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(viaFone ? AudioAttributes.USAGE_MEDIA : AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        /* se tiver musica tocando no fone, ela abaixa um instante em vez de brigar com o bipe */
        AudioFocusRequest foco = null;
        if (am != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            foco = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(attrs).build();
            am.requestAudioFocus(foco);
        }
        final AudioFocusRequest focoFinal = foco;

        try {
            final MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(attrs);
            mp.setDataSource(ctx, Uri.parse("android.resource://" + ctx.getPackageName() + "/" + resId));
            mp.setOnCompletionListener(p -> {
                p.release();
                if (am != null && focoFinal != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) am.abandonAudioFocusRequest(focoFinal);
                if (pending != null) pending.finish();
            });
            mp.setOnErrorListener((p, w, x) -> {
                p.release();
                if (pending != null) pending.finish();
                return true;
            });
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            if (am != null && focoFinal != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) am.abandonAudioFocusRequest(focoFinal);
            if (pending != null) pending.finish();
        }
    }
}
