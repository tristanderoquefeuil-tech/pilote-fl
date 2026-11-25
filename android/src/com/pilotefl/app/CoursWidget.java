package com.pilotefl.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Widget d'écran d'accueil : résumé des cours d'achat de la semaine (plus fortes baisses et hausses)
 * et nombre d'alertes de prix déclenchées. Un toucher ouvre l'appli sur la page Cours.
 */
public class CoursWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        renderAll(ctx);
        final Context app = ctx.getApplicationContext();
        final PendingResult pending = goAsync();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Veille.refreshCours(app);
                    renderAll(app);
                } catch (Exception e) {
                    /* hors connexion : le dernier résumé reste affiché */
                } finally {
                    pending.finish();
                }
            }
        }).start();
        CheckJob.schedule(ctx);
    }

    @Override
    public void onEnabled(Context ctx) { CheckJob.schedule(ctx); }

    /** Redessine tous les widgets posés avec les dernières données enregistrées (sans réseau). */
    static void renderAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        ComponentName cn = new ComponentName(ctx, CoursWidget.class);
        if (mgr.getAppWidgetIds(cn).length == 0) return;
        mgr.updateAppWidget(cn, build(ctx));
    }

    private static RemoteViews build(Context ctx) {
        SharedPreferences sp = Veille.prefs(ctx);
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_cours);
        String sem = sp.getString("w_sem", "");
        v.setTextViewText(R.id.w_title, sem.isEmpty() ? "Cours d'achat" : "Cours d'achat · semaine " + sem);
        int hits = Veille.hits(ctx).size();
        v.setViewVisibility(R.id.w_alerts, hits > 0 ? View.VISIBLE : View.GONE);
        v.setTextViewText(R.id.w_alerts, hits + (hits > 1 ? " alertes" : " alerte"));
        long maj = sp.getLong("w_maj", 0);
        String dn = sp.getString("w_dn", ""), up = sp.getString("w_up", "");
        if (maj == 0) {
            v.setTextViewText(R.id.w_dn, "Résumé des cours en cours de chargement…");
            v.setViewVisibility(R.id.w_up, View.GONE);
            v.setTextViewText(R.id.w_maj, "Touchez pour ouvrir Pilote F&L");
        } else if (sp.getInt("w_n", 0) == 0) {
            v.setTextViewText(R.id.w_dn, "Pas de cours récents cette semaine.");
            v.setViewVisibility(R.id.w_up, View.GONE);
            v.setTextViewText(R.id.w_maj, "Mis à jour " + quand(maj));
        } else {
            v.setTextViewText(R.id.w_dn, "▼ Baisses : " + (dn.isEmpty() ? "aucune" : dn));
            v.setViewVisibility(R.id.w_up, View.VISIBLE);
            v.setTextViewText(R.id.w_up, "▲ Hausses : " + (up.isEmpty() ? "aucune" : up));
            v.setTextViewText(R.id.w_maj, "Commission européenne · mis à jour " + quand(maj));
        }
        Intent open = new Intent(ctx, MainActivity.class)
            .putExtra("page", "marche")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(ctx, 1, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        return v;
    }

    private static String quand(long t) {
        return new SimpleDateFormat("dd/MM 'à' HH'h'mm", Locale.FRANCE).format(new Date(t));
    }
}
