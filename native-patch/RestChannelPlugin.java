package com.pedro.fitnessglobal;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

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


    /* ---------------------------------------------------------------------------
       Bolha flutuante com o tempo que falta.
       O Android nao deixa nenhum app escrever texto na barra de status (so o icone),
       entao a unica forma de ver o numero enquanto se usa OUTRO app e uma janela
       sobreposta (permissao "Exibir sobre outros apps"). E o que isto faz.
       --------------------------------------------------------------------------- */
    private TextView overlayTv;
    private WindowManager.LayoutParams overlayLp;
    private Handler overlayH;
    private Runnable overlayTick;
    private long overlayEnd;

    private boolean canDraw() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext());
    }

    @PluginMethod
    public void overlayCan(PluginCall call) {
        JSObject r = new JSObject();
        r.put("ok", canDraw());
        call.resolve(r);
    }

    /** Abre a tela do Android onde se autoriza "Exibir sobre outros apps". */
    @PluginMethod
    public void overlayAsk(PluginCall call) {
        try {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } catch (Exception ignored) { }
        call.resolve();
    }

    private int dp(float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getContext().getResources().getDisplayMetrics());
    }

    private void buildOverlay() {
        TextView tv = new TextView(getContext());
        tv.setTextColor(Color.parseColor("#7CF7C4"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tv.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E6101418"));
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor("#3A4A55"));
        tv.setBackground(bg);
        tv.setText("--:--");

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = dp(12);
        lp.y = dp(64);

        /* arrastar para tirar da frente de qualquer coisa; toque curto abre o app */
        tv.setOnTouchListener(new View.OnTouchListener() {
            float dx, dy;
            int x0, y0;
            boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = e.getRawX(); dy = e.getRawY();
                        x0 = overlayLp.x; y0 = overlayLp.y; moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int nx = x0 + (int) (e.getRawX() - dx);
                        int ny = y0 + (int) (e.getRawY() - dy);
                        if (Math.abs(nx - x0) > dp(6) || Math.abs(ny - y0) > dp(6)) moved = true;
                        overlayLp.x = nx; overlayLp.y = ny;
                        try { if (wm != null) wm.updateViewLayout(overlayTv, overlayLp); } catch (Exception ignored) { }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) {
                            try {
                                Intent open = getContext().getPackageManager()
                                        .getLaunchIntentForPackage(getContext().getPackageName());
                                if (open != null) {
                                    open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getContext().startActivity(open);
                                }
                            } catch (Exception ignored) { }
                        }
                        return true;
                }
                return false;
            }
        });

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return;
        wm.addView(tv, lp);
        overlayTv = tv;
        overlayLp = lp;
    }

    private void overlayRefresh() {
        if (overlayTv == null) return;
        long left = overlayEnd - System.currentTimeMillis();
        if (left <= 0) { removeOverlay(); return; }
        long s = (left + 999) / 1000;
        overlayTv.setText("\u23F1 " + (s / 60) + ":" + String.format("%02d", s % 60));
    }

    private void removeOverlay() {
        if (overlayH != null && overlayTick != null) overlayH.removeCallbacks(overlayTick);
        if (overlayTv != null) {
            try {
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                if (wm != null) wm.removeView(overlayTv);
            } catch (Exception ignored) { }
            overlayTv = null;
        }
    }

    @PluginMethod
    public void overlayStart(PluginCall call) {
        final long endAt = call.getLong("endAt", 0L);
        JSObject r = new JSObject();
        if (!canDraw()) { r.put("ok", false); r.put("perm", false); call.resolve(r); return; }
        overlayEnd = endAt;
        final Activity act = getActivity();
        Runnable job = new Runnable() {
            @Override
            public void run() {
                try {
                    if (overlayTv == null) buildOverlay();
                    if (overlayH == null) overlayH = new Handler(Looper.getMainLooper());
                    if (overlayTick == null) {
                        overlayTick = new Runnable() {
                            @Override
                            public void run() {
                                overlayRefresh();
                                if (overlayTv != null) overlayH.postDelayed(this, 500);
                            }
                        };
                    }
                    overlayH.removeCallbacks(overlayTick);
                    overlayH.post(overlayTick);
                } catch (Exception ignored) { }
            }
        };
        if (act != null) act.runOnUiThread(job); else new Handler(Looper.getMainLooper()).post(job);
        r.put("ok", true);
        r.put("perm", true);
        call.resolve(r);
    }

    @PluginMethod
    public void overlayStop(PluginCall call) {
        final Activity act = getActivity();
        Runnable job = new Runnable() { @Override public void run() { removeOverlay(); } };
        if (act != null) act.runOnUiThread(job); else new Handler(Looper.getMainLooper()).post(job);
        call.resolve();
    }

    @Override
    protected void handleOnDestroy() {
        removeOverlay();
        super.handleOnDestroy();
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
