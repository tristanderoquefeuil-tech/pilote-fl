package com.pilotefl.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Veille en arrière-plan, sans serveur : lit les cours et les actus publiés sur le site GitHub,
 * prépare le résumé du widget et envoie les notifications (alerte de prix déclenchée, nouvelles actus).
 */
final class Veille {
    static final String SITE = "https://tristanderoquefeuil-tech.github.io/pilote-fl/";
    static final String CH_ALERTES = "alertes", CH_ACTUS = "actus";
    private static final int NOTIF_ACTUS = 900, NOTIF_ALERTE = 1000;

    private Veille() { }

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("pilote", Context.MODE_PRIVATE); }

    static String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setConnectTimeout(15000);
            c.setReadTimeout(30000);
            c.setUseCaches(false);
            c.setRequestProperty("Accept", "application/json");
            if (c.getResponseCode() >= 400) throw new Exception("HTTP " + c.getResponseCode());
            InputStream in = c.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
            r.close();
            return sb.toString();
        } finally {
            c.disconnect();
        }
    }

    static String pct(double v) { return (v >= 0 ? "+" : "−") + Math.round(Math.abs(v)) + " %"; }

    static String eur(double v) { return String.format(Locale.FRANCE, "%.2f €", v); }

    /**
     * Télécharge les cours d'achat et prépare le résumé de la semaine, comme le bloc « En bref » de la page Cours :
     * produits cotés depuis moins de 24 jours, variation sur une semaine, plus fortes baisses et hausses.
     */
    static void refreshCours(Context ctx) throws Exception {
        JSONObject j = new JSONObject(get(SITE + "data/cours.json"));
        JSONArray ps = j.getJSONArray("produits");
        JSONObject last = new JSONObject();
        List<Object[]> moves = new ArrayList<>();
        String sem = "";
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long now = System.currentTimeMillis();
        for (int i = 0; i < ps.length(); i++) {
            JSONObject p = ps.getJSONObject(i);
            JSONArray pts = p.optJSONArray("points");
            if (pts == null || pts.length() == 0) continue;
            String nom = p.optString("produit");
            JSONObject lp = pts.getJSONObject(pts.length() - 1);
            last.put(nom, new JSONObject().put("p", lp.getDouble("p")).put("s", lp.optString("s")));
            if (pts.length() < 2) continue;
            boolean recent = true;
            try {
                Date d = f.parse(lp.optString("d"));
                recent = d == null || now - d.getTime() < 24L * 86400000L;
            } catch (Exception e) { /* date absente : considérée récente */ }
            if (!recent) continue;
            double prev = pts.getJSONObject(pts.length() - 2).getDouble("p");
            if (prev <= 0) continue;
            moves.add(new Object[] { nom, (lp.getDouble("p") / prev - 1) * 100 });
            if (lp.optString("s").compareTo(sem) > 0) sem = lp.optString("s");
        }
        List<Object[]> dn = new ArrayList<>(), up = new ArrayList<>();
        for (Object[] m : moves) {
            double ch = (Double) m[1];
            if (ch <= -1) dn.add(m); else if (ch >= 1) up.add(m);
        }
        Collections.sort(dn, new Comparator<Object[]>() { public int compare(Object[] a, Object[] b) { return Double.compare((Double) a[1], (Double) b[1]); } });
        Collections.sort(up, new Comparator<Object[]>() { public int compare(Object[] a, Object[] b) { return Double.compare((Double) b[1], (Double) a[1]); } });
        prefs(ctx).edit()
            .putString("last", last.toString())
            .putString("w_sem", sem.contains("-S") ? sem.substring(sem.indexOf("-S") + 2) : "")
            .putInt("w_n", moves.size())
            .putString("w_dn", liste(dn))
            .putString("w_up", liste(up))
            .putLong("w_maj", now)
            .apply();
    }

    private static String liste(List<Object[]> l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size() && i < 3; i++) {
            if (i > 0) sb.append(", ");
            sb.append(l.get(i)[0]).append(' ').append(pct((Double) l.get(i)[1]));
        }
        return sb.toString();
    }

    /** Alertes déclenchées : [id, produit, texte] pour chaque alerte dont le seuil est franchi. */
    static List<String[]> hits(Context ctx) {
        List<String[]> out = new ArrayList<>();
        try {
            SharedPreferences sp = prefs(ctx);
            JSONArray al = new JSONArray(sp.getString("alerts", "[]"));
            JSONObject last = new JSONObject(sp.getString("last", "{}"));
            for (int i = 0; i < al.length(); i++) {
                JSONObject a = al.getJSONObject(i);
                String nom = a.optString("produit");
                double seuil = a.optDouble("prix", Double.NaN);
                double prix = a.isNull("user") ? Double.NaN : a.optDouble("user", Double.NaN);
                if (Double.isNaN(prix)) {
                    JSONObject l = last.optJSONObject(nom);
                    prix = l == null ? Double.NaN : l.optDouble("p", Double.NaN);
                }
                if (Double.isNaN(prix) || Double.isNaN(seuil)) continue;
                boolean sous = "sous".equals(a.optString("sens"));
                if (sous ? prix < seuil : prix > seuil) {
                    out.add(new String[] { a.optString("id"), nom,
                        nom + " : cours " + (sous ? "passé sous " : "passé au-dessus de ") + eur(seuil) + " (" + eur(prix) + " HT/kg)" });
                }
            }
        } catch (Exception e) { /* alertes illisibles : aucune */ }
        return out;
    }

    // ---------- Notifications ----------

    static boolean notifOn(Context c) {
        if (!prefs(c).getBoolean("notif", false)) return false;
        if (Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false;
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        return nm != null && nm.areNotificationsEnabled();
    }

    static void channels(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel a = new NotificationChannel(CH_ALERTES, "Alertes de prix", NotificationManager.IMPORTANCE_DEFAULT);
        a.setDescription("Quand le cours d'un produit franchit le seuil d'une de vos alertes");
        NotificationChannel n = new NotificationChannel(CH_ACTUS, "Actualités", NotificationManager.IMPORTANCE_LOW);
        n.setDescription("Nouvelles actus des fruits et légumes et de la grande distribution");
        nm.createNotificationChannel(a);
        nm.createNotificationChannel(n);
    }

    @SuppressWarnings("deprecation")
    private static void post(Context c, int id, String channel, String title, String text, String page, List<String> lines) {
        channels(c);
        Intent open = new Intent(c, MainActivity.class)
            .putExtra("page", page)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, id, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, channel) : new Notification.Builder(c);
        b.setSmallIcon(R.drawable.ic_notif).setColor(0xFF1F7A4D).setContentTitle(title).setContentText(text)
            .setContentIntent(pi).setAutoCancel(true).setShowWhen(true);
        if (lines != null && lines.size() > 1) {
            Notification.InboxStyle st = new Notification.InboxStyle();
            for (int i = 0; i < lines.size() && i < 5; i++) st.addLine(lines.get(i));
            if (lines.size() > 5) st.setSummaryText("et " + (lines.size() - 5) + " de plus");
            b.setStyle(st);
        } else {
            b.setStyle(new Notification.BigTextStyle().bigText(text));
        }
        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, b.build());
    }

    /**
     * Notifie chaque alerte nouvellement déclenchée (une seule fois tant qu'elle reste déclenchée).
     * Au premier passage après l'activation, les alertes déjà déclenchées sont seulement mémorisées.
     */
    static void checkAlerts(Context c) {
        SharedPreferences sp = prefs(c);
        List<String[]> h = hits(c);
        try {
            JSONObject before = new JSONObject(sp.getString("notified", "{}")), now = new JSONObject();
            boolean silent = !sp.getBoolean("baseline_alerts", false);
            for (String[] x : h) {
                now.put(x[0], true);
                if (!silent && !before.has(x[0]) && notifOn(c)) {
                    post(c, NOTIF_ALERTE + (x[0].hashCode() & 0xfff), CH_ALERTES, "Alerte de prix : " + x[1], x[2], "marche", null);
                }
            }
            sp.edit().putString("notified", now.toString()).putBoolean("baseline_alerts", true).apply();
        } catch (Exception e) { /* rien */ }
    }

    /** Notifie les actus publiées depuis la dernière vérification (une notification groupée). */
    static void checkNews(Context c) throws Exception {
        SharedPreferences sp = prefs(c);
        JSONArray arts = new JSONObject(get(SITE + "data/news.json")).optJSONArray("articles");
        if (arts == null) return;
        String seen = sp.getString("news_last", ""), max = seen;
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < arts.length(); i++) {
            JSONObject a = arts.getJSONObject(i);
            String d = a.optString("date");
            if (d.compareTo(max) > 0) max = d;
            if (!seen.isEmpty() && d.compareTo(seen) > 0) titles.add(a.optString("titre"));
        }
        sp.edit().putString("news_last", max).apply();
        if (titles.isEmpty() || !notifOn(c)) return;
        int n = titles.size();
        post(c, NOTIF_ACTUS, CH_ACTUS, n == 1 ? "Nouvelle actu fruits et légumes" : n + " nouvelles actus fruits et légumes", titles.get(0), "news", titles);
    }

    /** Vérification complète : cours, widget, puis notifications si elles sont activées. */
    static void runAll(Context c) {
        try { refreshCours(c); } catch (Exception e) { /* hors connexion : on garde le dernier résumé */ }
        CoursWidget.renderAll(c);
        checkAlerts(c);
        if (prefs(c).getBoolean("notif", false)) {
            try { checkNews(c); } catch (Exception e) { /* réessai au prochain passage */ }
        }
    }
}
