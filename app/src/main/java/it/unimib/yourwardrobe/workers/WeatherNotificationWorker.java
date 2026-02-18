package it.unimib.yourwardrobe.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import it.unimib.yourwardrobe.BuildConfig;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.data.api.WeatherApiService;
import it.unimib.yourwardrobe.data.dto.WeatherResponse;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherNotificationWorker extends Worker {

    private static final String CHANNEL_ID = "outfit_weather_channel";
    private static final String TAG = "WeatherWorker";

    public WeatherNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Coordinate di default (Milano) - puoi renderle dinamiche
            double lat = getInputData().getDouble("lat", 45.4654);
            double lon = getInputData().getDouble("lon", 9.1859);

            WeatherApiService service = new Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WeatherApiService.class);

            Response<WeatherResponse> response = service.getCurrentWeather(
                    BuildConfig.OPENWEATHERMAP_KEY, lat, lon, "metric", "it"
            ).execute();

            if (response.isSuccessful() && response.body() != null) {
                double temp = response.body().main.temp;
                String message = getMessageForTemperature(temp);
                sendNotification(message, temp);
            }

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Errore nel worker: " + e.getMessage());
            return Result.failure();
        }
    }

    private String getMessageForTemperature(double temp) {
        if (temp < 10) {
            return "🧥 Oggi fa freddo (" + (int) temp + "°C), prova uno dei tuoi outfit invernali!";
        } else if (temp < 15) {
            return "🍂 Temperatura fresca (" + (int) temp + "°C), perfetto per un outfit autunnale!";
        } else if (temp < 20) {
            return "🌸 Clima mite (" + (int) temp + "°C), un outfit primaverile è l'ideale!";
        } else {
            return "☀️ Oggi fa caldo (" + (int) temp + "°C), è il momento degli outfit estivi!";
        }
    }

    private void sendNotification(String message, double temp) {
        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Crea il canale (richiesto da Android 8+)
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Outfit del giorno",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Suggerimenti outfit basati sul meteo");
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("👗 YourWardrobe")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        manager.notify(1001, builder.build());
    }
}