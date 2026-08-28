package com.pedro.fitnessglobal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

/**
 * Cria o canal de notificação do descanso com um padrão de vibração escolhido no app.
 * O Capacitor só permite ligar/desligar a vibração; aqui definimos a duração e os pulsos,
 * que é o que faz a vibração ser sentida mesmo com a tela apagada.
 */
@CapacitorPlugin(name = "RestChannel")
public class RestChannelPlugin extends Plugin {

    @PluginMethod
    public void create(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { call.resolve(); return; }

        String id = call.getString("id", "rest-custom");
        String name = call.getString("name", "Descanso");
        boolean withSound = Boolean.TRUE.equals(call.getBoolean("sound", false));

        long[] pattern = { 0, 600, 150, 600 };
        try {
            JSArray arr = call.getArray("pattern");
            if (arr != null) {
                List<Integer> vals = arr.toList();
                if (!vals.isEmpty()) {
                    pattern = new long[vals.size() + 1];
                    pattern[0] = 0;                          // sem atraso inicial
                    for (int i = 0; i < vals.size(); i++) {
                        pattern[i + 1] = Math.max(0, vals.get(i).longValue());
                    }
                }
            }
        } catch (Exception ignored) { }

        NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) { call.resolve(); return; }

        NotificationChannel ch = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Aviso de fim do tempo de descanso");
        ch.enableVibration(true);
        ch.setVibrationPattern(pattern);
        ch.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        ch.enableLights(true);
        if (!withSound) {
            ch.setSound(null, null);
        }
        nm.createNotificationChannel(ch);

        JSObject ret = new JSObject();
        ret.put("id", id);
        call.resolve(ret);
    }

    /**
     * Diz se o celular está no Não Perturbe. Ler o filtro NÃO precisa de permissão
     * (só mudar precisaria), então dá para avisar que o som do descanso não vai tocar.
     * Filtro: 1 = tudo passa · 2 = só prioridade · 3 = nada · 4 = só alarmes.
     */
    @PluginMethod
    public void dnd(PluginCall call) {
        JSObject ret = new JSObject();
        try {
            NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                ret.put("known", false);
                call.resolve(ret);
                return;
            }
            int f = nm.getCurrentInterruptionFilter();
            ret.put("known", true);
            ret.put("filter", f);
            ret.put("on", f == NotificationManager.INTERRUPTION_FILTER_PRIORITY
                       || f == NotificationManager.INTERRUPTION_FILTER_NONE
                       || f == NotificationManager.INTERRUPTION_FILTER_ALARMS);
            ret.put("silent", f == NotificationManager.INTERRUPTION_FILTER_NONE);
        } catch (Exception e) {
            ret.put("known", false);
        }
        call.resolve(ret);
    }

    private static final int LIVE_ID = 7002;
    private static final String LIVE_CH = "rest-live";

    private int smallIcon() {
        int r = getContext().getResources().getIdentifier("ic_stat_icon", "drawable", getContext().getPackageName());
        return r != 0 ? r : getContext().getApplicationInfo().icon;
    }

    /**
     * Notificacao fixa com contagem regressiva na barra de status, para ver quanto falta
     * do descanso sem abrir o app. Canal proprio, silencioso e de baixa prioridade: quem
     * apita no fim continua sendo a notificacao do descanso (id 7001).
     */
    @PluginMethod
    public void ongoing(PluginCall call) {
        long endAt = call.getLong("endAt", 0L);
        String title = call.getString("title", "Descanso");
        String text = call.getString("text", "");
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { call.resolve(); return; }
            NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) { call.resolve(); return; }

            NotificationChannel c = new NotificationChannel(LIVE_CH, "Descanso em andamento", NotificationManager.IMPORTANCE_LOW);
            c.setDescription("Mostra quanto falta do descanso na barra de status");
            c.setSound(null, null);
            c.enableVibration(false);
            c.setShowBadge(false);
            nm.createNotificationChannel(c);

            Notification.Builder b = new Notification.Builder(getContext(), LIVE_CH)
                    .setSmallIcon(smallIcon())
                    .setContentTitle(title)
                    .setContentText(text)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(true)
                    .setWhen(endAt)
                    .setUsesChronometer(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) b.setChronometerCountDown(true);

            Intent open = getContext().getPackageManager().getLaunchIntentForPackage(getContext().getPackageName());
            if (open != null) {
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                b.setContentIntent(PendingIntent.getActivity(getContext(), 0, open,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
            }
            nm.notify(LIVE_ID, b.build());
        } catch (Exception ignored) { }
        call.resolve();
    }

    @PluginMethod
    public void ongoingCancel(PluginCall call) {
        try {
            NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(LIVE_ID);
        } catch (Exception ignored) { }
        call.resolve();
    }

    /** Abre outro app instalado (usado para o Samsung Health). */
    @PluginMethod
    public void openApp(PluginCall call) {
        String pkg = call.getString("pkg", "");
        JSObject r = new JSObject();
        try {
            Intent i = getContext().getPackageManager().getLaunchIntentForPackage(pkg);
            if (i == null) { r.put("ok", false); call.resolve(r); return; }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
            r.put("ok", true);
        } catch (Exception e) { r.put("ok", false); }
        call.resolve(r);
    }
}
