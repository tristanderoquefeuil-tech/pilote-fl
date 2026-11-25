package com.pilotefl.app;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

/**
 * Vérification périodique (environ toutes les 4 heures, réseau requis, conservée après redémarrage) :
 * met à jour le widget et envoie les notifications d'alerte de prix et de nouvelles actus.
 */
public class CheckJob extends JobService {
    private static final int JOB_ID = 4223;
    private static final long PERIODE = 4L * 60 * 60 * 1000, SOUPLESSE = 60L * 60 * 1000;

    static void schedule(Context ctx) {
        JobScheduler js = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js == null || js.getPendingJob(JOB_ID) != null) return;
        try {
            js.schedule(new JobInfo.Builder(JOB_ID, new ComponentName(ctx, CheckJob.class))
                .setPeriodic(PERIODE, SOUPLESSE)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build());
        } catch (Exception e) { /* planification refusée : le widget se met à jour seul */ }
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        final Context app = getApplicationContext();
        new Thread(new Runnable() {
            @Override public void run() {
                try { Veille.runAll(app); } finally { jobFinished(params, false); }
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) { return true; }
}
