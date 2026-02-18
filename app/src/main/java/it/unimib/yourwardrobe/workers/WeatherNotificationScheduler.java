package it.unimib.yourwardrobe.workers;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import androidx.work.OneTimeWorkRequest;

public class WeatherNotificationScheduler {

    private static final String WORK_NAME = "weather_notification_work";

    public static void schedule(Context context, double lat, double lon) {
        // Calcola i minuti mancanti alle 8:00 di domani
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 8);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        if (now.after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = target.getTimeInMillis() - now.getTimeInMillis();

        Data inputData = new Data.Builder()
                .putDouble("lat", lat)
                .putDouble("lon", lon)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WeatherNotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }

    public static void testNow(Context context, double lat, double lon) {

        Data inputData = new Data.Builder()
                .putDouble("lat", lat)
                .putDouble("lon", lon)
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(WeatherNotificationWorker.class)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(context).enqueue(request);
    }




    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}