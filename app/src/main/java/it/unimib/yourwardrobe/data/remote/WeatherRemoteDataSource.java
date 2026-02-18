package it.unimib.yourwardrobe.data.remote;

import static it.unimib.yourwardrobe.utils.Constants.API_APPID_ERROR;

import androidx.annotation.NonNull;

import java.util.Calendar;

import javax.inject.Inject;

import it.unimib.yourwardrobe.BuildConfig;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.data.api.WeatherApiService;
import it.unimib.yourwardrobe.data.dto.ForecastResponse;
import it.unimib.yourwardrobe.data.dto.WeatherResponse;
import retrofit2.Call;
import retrofit2.Response;

public class WeatherRemoteDataSource {

    private static final String API_KEY = BuildConfig.OPENWEATHERMAP_KEY;
    private final WeatherApiService weatherApiService;

    @Inject
    public WeatherRemoteDataSource(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    public void getCurrentWeather(double lat, double lon, Callback<WeatherResponse> callback) {
        Call<WeatherResponse> call = weatherApiService.getCurrentWeather(
                API_KEY, lat, lon, "metric", "it");

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call,
                                   @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Error getting weather data",
                            new Exception(API_APPID_ERROR));
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                callback.onFailure("Error getting weather data", t);
            }
        });
    }

    /**
     * Recupera il meteo previsto per il giorno indicato da dateMillis.
     * Tra tutti gli slot 3h restituisce quello più vicino alle 12:00 del giorno scelto.
     */
    public void getForecastWeather(double lat, double lon, long dateMillis,
                                   Callback<ForecastResponse.ForecastItem> callback) {
        Call<ForecastResponse> call = weatherApiService.getForecastWeather(
                API_KEY, lat, lon, "metric", "it");

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call,
                                   @NonNull Response<ForecastResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure("Error getting forecast data",
                            new Exception(API_APPID_ERROR));
                    return;
                }

                ForecastResponse forecastResponse = response.body();

                // Calcola il timestamp di mezzogiorno del giorno selezionato
                Calendar targetCal = Calendar.getInstance();
                targetCal.setTimeInMillis(dateMillis);
                targetCal.set(Calendar.HOUR_OF_DAY, 12);
                targetCal.set(Calendar.MINUTE, 0);
                targetCal.set(Calendar.SECOND, 0);
                long targetNoon = targetCal.getTimeInMillis() / 1000; // in secondi

                // Trova lo slot più vicino al mezzogiorno del giorno scelto
                ForecastResponse.ForecastItem best = null;
                long minDiff = Long.MAX_VALUE;

                for (ForecastResponse.ForecastItem item : forecastResponse.list) {
                    // Verifica che sia lo stesso giorno
                    Calendar itemCal = Calendar.getInstance();
                    itemCal.setTimeInMillis(item.dt * 1000L);

                    Calendar selCal = Calendar.getInstance();
                    selCal.setTimeInMillis(dateMillis);

                    boolean sameDay =
                            itemCal.get(Calendar.YEAR) == selCal.get(Calendar.YEAR) &&
                                    itemCal.get(Calendar.DAY_OF_YEAR) == selCal.get(Calendar.DAY_OF_YEAR);

                    if (sameDay) {
                        long diff = Math.abs(item.dt - targetNoon);
                        if (diff < minDiff) {
                            minDiff = diff;
                            best = item;
                        }
                    }
                }

                if (best != null) {
                    callback.onSuccess(best);
                } else {
                    callback.onFailure("Nessun dato meteo disponibile per il giorno selezionato", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                callback.onFailure("Error getting forecast data", t);
            }
        });
    }
}