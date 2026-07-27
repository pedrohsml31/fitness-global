package com.pedro.fitnessglobal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
}
