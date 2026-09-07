package com.pedro.fitnessglobal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Atualizacao do app sem baixar APK na mao.
 *
 * Sao dois caminhos:
 *
 * 1) OTA do conteudo web (o caso normal). O app copia o que esta dentro do APK
 *    (assets/public) para uma pasta propria, grava por cima os arquivos novos baixados
 *    do site e manda a Bridge servir dessa pasta. A ORIGEM do WebView continua a mesma
 *    (https://localhost), entao o localStorage NAO e apagado — que e o problema de
 *    apontar o server.url para outro dominio.
 *    Se a versao nova nao chamar confirm() (tela branca, erro de sintaxe), o proximo
 *    boot desfaz sozinho e volta para o que veio no APK.
 *    Quando um APK novo e instalado, a propria Capacitor limpa o caminho salvo
 *    (isNewBinary), entao o APK sempre ganha da OTA.
 *
 * 2) APK, so quando a versao nova mexe em codigo nativo. O app baixa sozinho e abre o
 *    instalador do Android — resta um toque em "Instalar". O Android nao permite que um
 *    app comum instale outro sem essa confirmacao.
 */
@CapacitorPlugin(name = "Updater")
public class UpdaterPlugin extends Plugin {

    private static final String PREF = "fitglobal.updater";
    private static final String K_PENDING = "pending";   // versao aplicada e ainda nao confirmada
    private static final String K_VERSION = "webVersion";
    private static final String K_NATIVE = "nativeBuild";

    /* mesmas chaves que a Capacitor usa para lembrar de onde servir os arquivos */
    private static final String CAP_PREFS = "CapWebViewSettings";
    private static final String CAP_SERVER_PATH = "serverBasePath";

    private static final String OTA = "ota";
    private static final String OTA_NEW = "ota_new";
    private static final String APK_DIR = "updates";
    private static final String APK_FILE = "app.apk";

    private SharedPreferences prefs() {
        return getContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private File otaDir() { return new File(getContext().getFilesDir(), OTA); }
    private File otaNewDir() { return new File(getContext().getFilesDir(), OTA_NEW); }
    private File apkFile() { return new File(new File(getContext().getFilesDir(), APK_DIR), APK_FILE); }

    private int nativeBuild() {
        try {
            PackageInfo pi = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            return (int) (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? pi.getLongVersionCode() : pi.versionCode);
        } catch (Exception e) { return 0; }
    }

    @Override
    public void load() {
        SharedPreferences p = prefs();
        int nb = nativeBuild();
        /* APK novo instalado: a Capacitor ja voltou a servir dos assets, entao a pasta
           da OTA antiga so ocupa espaco e mentiria sobre a versao ativa */
        if (p.getInt(K_NATIVE, 0) != nb) {
            deleteDir(otaDir());
            deleteDir(otaNewDir());
            p.edit().putInt(K_NATIVE, nb).remove(K_VERSION).putInt(K_PENDING, 0).apply();
            return;
        }
        /* a atualizacao anterior nunca confirmou que abriu: desfaz */
        if (p.getInt(K_PENDING, 0) != 0) {
            backToApk();
            p.edit().putInt(K_PENDING, 0).remove(K_VERSION).apply();
        }
    }

    @PluginMethod
    public void info(PluginCall call) {
        SharedPreferences p = prefs();
        String base = bridge != null ? bridge.getServerBasePath() : "";
        JSObject r = new JSObject();
        r.put("nativeBuild", nativeBuild());
        r.put("webVersion", p.getInt(K_VERSION, 0));
        r.put("ota", base != null && !base.isEmpty() && base.contains(OTA));
        r.put("path", base == null ? "" : base);
        call.resolve(r);
    }

    /** Diz que a versao recem-aplicada abriu de verdade — cancela o desfazer automatico. */
    @PluginMethod
    public void confirm(PluginCall call) {
        prefs().edit().putInt(K_PENDING, 0).apply();
        call.resolve();
    }

    /** Prepara a pasta nova com uma copia do que veio no APK. */
    @PluginMethod
    public void stage(PluginCall call) {
        JSObject r = new JSObject();
        try {
            File dst = otaNewDir();
            deleteDir(dst);
            if (!dst.mkdirs()) throw new Exception("nao criou " + dst);
            copyAssetDir("public", dst);
            r.put("ok", true);
            r.put("path", dst.getAbsolutePath());
        } catch (Exception e) {
            r.put("ok", false);
            r.put("erro", String.valueOf(e.getMessage()));
        }
        call.resolve(r);
    }

    /** Grava um arquivo de texto dentro da pasta preparada. */
    @PluginMethod
    public void write(PluginCall call) {
        String name = call.getString("name", "");
        String data = call.getString("data", "");
        JSObject r = new JSObject();
        try {
            if (name.isEmpty() || name.contains("..")) throw new Exception("nome invalido");
            File f = new File(otaNewDir(), name);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) throw new Exception("nao criou pasta");
            FileOutputStream out = new FileOutputStream(f);
            out.write(data.getBytes(StandardCharsets.UTF_8));
            out.close();
            r.put("ok", true);
            r.put("bytes", f.length());
        } catch (Exception e) {
            r.put("ok", false);
            r.put("erro", String.valueOf(e.getMessage()));
        }
        call.resolve(r);
    }

    /** Troca a pasta ativa e recarrega o app na versao nova. */
    @PluginMethod
    public void activate(PluginCall call) {
        int version = call.getInt("version", 0);
        JSObject r = new JSObject();
        try {
            File novo = otaNewDir(), ativo = otaDir();
            if (!novo.exists()) throw new Exception("nada preparado");
            deleteDir(ativo);
            if (!novo.renameTo(ativo)) throw new Exception("nao trocou a pasta");

            prefs().edit().putInt(K_PENDING, version).putInt(K_VERSION, version).apply();
            getContext()
                .getSharedPreferences(CAP_PREFS, Activity.MODE_PRIVATE)
                .edit()
                .putString(CAP_SERVER_PATH, ativo.getAbsolutePath())
                .apply();

            r.put("ok", true);
            call.resolve(r);
            /* recarrega DEPOIS de responder, senao o resolve se perde no reload */
            bridge.getActivity().runOnUiThread(() -> bridge.setServerBasePath(ativo.getAbsolutePath()));
        } catch (Exception e) {
            r.put("ok", false);
            r.put("erro", String.valueOf(e.getMessage()));
            call.resolve(r);
        }
    }

    /** Volta para o conteudo que veio dentro do APK. */
    @PluginMethod
    public void revert(PluginCall call) {
        backToApk();
        prefs().edit().remove(K_VERSION).putInt(K_PENDING, 0).apply();
        JSObject r = new JSObject();
        r.put("ok", true);
        call.resolve(r);
        if (bridge != null && bridge.getActivity() != null) {
            bridge.getActivity().runOnUiThread(() -> bridge.setServerAssetPath("public"));
        }
    }

    private void backToApk() {
        getContext()
            .getSharedPreferences(CAP_PREFS, Activity.MODE_PRIVATE)
            .edit()
            .putString(CAP_SERVER_PATH, "")
            .apply();
        deleteDir(otaDir());
        deleteDir(otaNewDir());
    }

    /* ---------------- APK ---------------- */

    /** O Android exige que VOCE autorize este app a instalar aplicativos. */
    @PluginMethod
    public void canInstall(PluginCall call) {
        JSObject r = new JSObject();
        boolean ok = true;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ok = getContext().getPackageManager().canRequestPackageInstalls();
            }
        } catch (Exception e) { ok = false; }
        r.put("ok", ok);
        call.resolve(r);
    }

    @PluginMethod
    public void askInstall(PluginCall call) {
        try {
            Intent i = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + getContext().getPackageName()))
                : new Intent(Settings.ACTION_SECURITY_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } catch (Exception ignored) { }
        call.resolve();
    }

    /** Baixa o APK para dentro do app, avisando o progresso. */
    @PluginMethod
    public void downloadApk(final PluginCall call) {
        final String url = call.getString("url", "");
        new Thread(() -> {
            JSObject r = new JSObject();
            HttpURLConnection c = null;
            try {
                File dir = new File(getContext().getFilesDir(), APK_DIR);
                if (!dir.exists() && !dir.mkdirs()) throw new Exception("nao criou " + dir);
                File out = apkFile();
                if (out.exists() && !out.delete()) throw new Exception("nao apagou o anterior");

                c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(20000);
                c.setReadTimeout(60000);
                c.setInstanceFollowRedirects(true);
                c.connect();
                int code = c.getResponseCode();
                if (code < 200 || code >= 300) throw new Exception("HTTP " + code);
                int total = c.getContentLength();

                InputStream in = c.getInputStream();
                OutputStream os = new FileOutputStream(out);
                byte[] buf = new byte[16384];
                long lidos = 0;
                int ultimoPct = -1, n;
                while ((n = in.read(buf)) > 0) {
                    os.write(buf, 0, n);
                    lidos += n;
                    if (total > 0) {
                        int pct = (int) (lidos * 100 / total);
                        if (pct != ultimoPct) {
                            ultimoPct = pct;
                            JSObject ev = new JSObject();
                            ev.put("pct", pct);
                            ev.put("bytes", lidos);
                            ev.put("total", total);
                            notifyListeners("apkProgress", ev);
                        }
                    }
                }
                os.close();
                in.close();
                if (out.length() < 100000) throw new Exception("arquivo veio pequeno demais");
                r.put("ok", true);
                r.put("bytes", out.length());
            } catch (Exception e) {
                r.put("ok", false);
                r.put("erro", String.valueOf(e.getMessage()));
            } finally {
                if (c != null) c.disconnect();
            }
            call.resolve(r);
        }).start();
    }

    /** Abre o instalador do Android com o APK baixado. */
    @PluginMethod
    public void installApk(PluginCall call) {
        JSObject r = new JSObject();
        try {
            File f = apkFile();
            if (!f.exists()) throw new Exception("nada baixado");
            Uri uri = FileProvider.getUriForFile(getContext(),
                getContext().getPackageName() + ".fileprovider", f);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
            r.put("ok", true);
        } catch (Exception e) {
            r.put("ok", false);
            r.put("erro", String.valueOf(e.getMessage()));
        }
        call.resolve(r);
    }

    /* ---------------- utilidades ---------------- */

    private void copyAssetDir(String assetPath, File dst) throws Exception {
        String[] filhos = getContext().getAssets().list(assetPath);
        if (filhos == null || filhos.length == 0) {          // e arquivo, nao pasta
            copyAssetFile(assetPath, dst);
            return;
        }
        if (!dst.exists() && !dst.mkdirs()) throw new Exception("nao criou " + dst);
        for (String f : filhos) {
            copyAssetDir(assetPath + "/" + f, new File(dst, f));
        }
    }

    private void copyAssetFile(String assetPath, File dst) throws Exception {
        File parent = dst.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) throw new Exception("nao criou pasta");
        InputStream in = getContext().getAssets().open(assetPath);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[16384];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        out.close();
        in.close();
    }

    private void deleteDir(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null) for (File x : fs) deleteDir(x);
        }
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
