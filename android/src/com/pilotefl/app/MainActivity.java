package com.pilotefl.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final int FILE_REQUEST = 42, NOTIF_REQUEST = 43;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;

    /** Pont appelé par la page : partage le texte d'export (e-mail, Drive, messagerie…). */
    public class Bridge {
        /** Numéro de version installé, comparé à version.json pour proposer les mises à jour. */
        @JavascriptInterface
        public int versionCode() {
            try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode; } catch (Exception e) { return 0; }
        }

        @JavascriptInterface
        public String versionName() {
            try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (Exception e) { return ""; }
        }

        /** Ouvre dans le navigateur le téléchargement d'une nouvelle version (adresses GitHub du projet uniquement). */
        @JavascriptInterface
        public void openUrl(final String url) {
            if (url == null || !(url.startsWith("https://github.com/tristanderoquefeuil-tech/pilote-fl/")
                    || url.startsWith("https://tristanderoquefeuil-tech.github.io/pilote-fl/"))) return;
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { }
                }
            });
        }

        @JavascriptInterface
        public void share(final String text) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde Pilote F&L");
                    send.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(send, "Envoyer la sauvegarde"));
                }
            });
        }

        /**
         * Télécharge une page de l'API publique des prix (Commission européenne) en arrière-plan,
         * puis renvoie le résultat à la page via window.__pflFetchDone(id, ok, texte).
         * Seule l'adresse de cette API est autorisée.
         */
        @JavascriptInterface
        public void fetchAsync(final String id, final String url) {
            new Thread(new Runnable() {
                @Override public void run() {
                    boolean ok = false;
                    String body;
                    if (url == null || !url.startsWith("https://api.tech.ec.europa.eu/agrifood/")) {
                        body = "adresse non autorisée";
                    } else {
                        HttpURLConnection c = null;
                        try {
                            c = (HttpURLConnection) new URL(url).openConnection();
                            c.setConnectTimeout(15000);
                            c.setReadTimeout(30000);
                            c.setRequestProperty("Accept", "application/json");
                            int code = c.getResponseCode();
                            InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream();
                            StringBuilder sb = new StringBuilder();
                            if (in != null) {
                                BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                                String line;
                                while ((line = r.readLine()) != null) sb.append(line).append('\n');
                                r.close();
                            }
                            body = sb.toString();
                            ok = code < 400;
                        } catch (Exception e) {
                            body = String.valueOf(e.getMessage());
                        } finally {
                            if (c != null) c.disconnect();
                        }
                    }
                    final String js = "window.__pflFetchDone && window.__pflFetchDone(" + JSONObject.quote(id) + "," + ok + "," + JSONObject.quote(body) + ")";
                    runOnUiThread(new Runnable() {
                        @Override public void run() { web.evaluateJavascript(js, null); }
                    });
                }
            }).start();
        }

        /** État des notifications du téléphone : « on », « off » ou « blocked » (refusées dans Android). */
        @JavascriptInterface
        public String notifState() { return notifStateNow(); }

        /**
         * Active ou coupe les notifications. Sur Android 13 et plus, la permission est demandée au premier
         * usage ; la réponse est renvoyée à la page via window.__pflNotifState.
         */
        @JavascriptInterface
        public String setNotif(boolean on) {
            final android.content.SharedPreferences sp = Veille.prefs(MainActivity.this);
            if (!on) { sp.edit().putBoolean("notif", false).apply(); return "off"; }
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (sp.getBoolean("notif_asked", false) && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) return "blocked";
                sp.edit().putBoolean("notif_asked", true).apply();
                runOnUiThread(new Runnable() {
                    @Override public void run() { requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, NOTIF_REQUEST); }
                });
                return "asking";
            }
            return enableNotif();
        }

        /** Ouvre les réglages Android des notifications de l'appli. */
        @JavascriptInterface
        public void openNotifSettings() {
            runOnUiThread(new Runnable() {
                @Override public void run() { openNotifSettingsNow(); }
            });
        }

        /** Reçoit les alertes de prix de la page (JSON) pour le widget et les notifications appli fermée. */
        @JavascriptInterface
        public void syncAlerts(String json) {
            try { new org.json.JSONArray(json); } catch (Exception e) { return; }
            Veille.prefs(MainActivity.this).edit().putString("alerts", json).apply();
            final android.content.Context app = getApplicationContext();
            new Thread(new Runnable() {
                @Override public void run() { CoursWidget.renderAll(app); Veille.checkAlerts(app); }
            }).start();
        }

        /** Enregistre une affiche (PNG en base64) dans la galerie, puis ouvre le menu de partage. */
        @JavascriptInterface
        public String saveImage(String dataUrl, String name) {
            try {
                if (Build.VERSION.SDK_INT < 29 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() { requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1); }
                    });
                    return "perm";
                }
                byte[] bytes = Base64.decode(dataUrl.substring(dataUrl.indexOf(',') + 1), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                String url = MediaStore.Images.Media.insertImage(getContentResolver(), bmp, name, "Affiche Pilote F&L");
                if (url == null) return "error";
                final Uri uri = Uri.parse(url);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Intent send = new Intent(Intent.ACTION_SEND);
                        send.setType("image/*");
                        send.putExtra(Intent.EXTRA_STREAM, uri);
                        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(send, "Partager l'affiche"));
                    }
                });
                return "ok";
            } catch (Exception e) {
                return "error";
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#F4F6F3"));
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        // Les liens vers le web (articles d'actualité, site du projet) s'ouvrent dans le navigateur du téléphone,
        // l'appli reste sur sa page
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("https://") || url.startsWith("http://"))) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { }
                    return true;
                }
                return false;
            }
        });
        // Ouvre le sélecteur de fichiers du téléphone pour les boutons « Choisir un fichier »
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Choisir un fichier"), FILE_REQUEST);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        setContentView(web);

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            String page = pageOf(getIntent());
            web.loadUrl("file:///android_asset/index.html" + (page != null ? "#" + page : ""));
        }
        // Widget et notifications : vérification périodique en arrière-plan
        Veille.channels(this);
        CheckJob.schedule(this);
    }

    /** Page demandée par une notification ou le widget (nom de vue simple uniquement). */
    private static String pageOf(Intent i) {
        String p = i == null ? null : i.getStringExtra("page");
        return p != null && p.matches("[a-z]{2,20}") ? p : null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String page = pageOf(intent);
        if (page != null && web != null) web.evaluateJavascript("location.hash=" + JSONObject.quote(page), null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retour des réglages Android : la page affiche l'état réel des notifications
        if (web != null) web.evaluateJavascript("window.__pflNotifState && window.__pflNotifState(" + JSONObject.quote(notifStateNow()) + ")", null);
    }

    private String notifStateNow() {
        if (!Veille.prefs(this).getBoolean("notif", false)) return "off";
        return Veille.notifOn(this) ? "on" : "blocked";
    }

    /** Active les notifications : les alertes déjà déclenchées et les actus déjà parues ne sont pas renotifiées. */
    private String enableNotif() {
        android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && !nm.areNotificationsEnabled()) return "blocked";
        Veille.prefs(this).edit().putBoolean("notif", true).putBoolean("baseline_alerts", false).remove("news_last").apply();
        Veille.channels(this);
        CheckJob.schedule(this);
        final android.content.Context app = getApplicationContext();
        new Thread(new Runnable() {
            @Override public void run() { Veille.runAll(app); }
        }).start();
        return "on";
    }

    private void openNotifSettingsNow() {
        Intent i;
        if (Build.VERSION.SDK_INT >= 26) {
            i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        }
        try { startActivity(i); } catch (Exception e) { }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != NOTIF_REQUEST) return;
        String st;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) st = enableNotif();
        else {
            Veille.prefs(this).edit().putBoolean("notif", false).apply();
            st = Build.VERSION.SDK_INT >= 33 && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ? "blocked" : "off";
        }
        web.evaluateJavascript("window.__pflNotifState && window.__pflNotifState(" + JSONObject.quote(st) + ")", null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_REQUEST && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    /**
     * Bouton Retour : la page referme d'abord ce qui est ouvert (roue, fiche, panneau) ;
     * sinon on revient à l'écran précédent, et depuis le premier écran on quitte l'appli.
     */
    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.__pflBack && window.__pflBack()) ? '1' : '0'", new ValueCallback<String>() {
            @Override public void onReceiveValue(String handled) {
                if ("\"1\"".equals(handled)) return;
                if (web.canGoBack()) web.goBack();
                else finish();
            }
        });
    }
}
