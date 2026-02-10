package it.unimib.yourwardrobe.source.remote;

import static it.unimib.yourwardrobe.utils.Constants.API_APPID_ERROR;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import it.unimib.yourwardrobe.BuildConfig;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.source.api.WeatherApiService;
import it.unimib.yourwardrobe.source.dto.WeatherResponse;
import retrofit2.Call;
import retrofit2.Response;

public class WeatherRemoteDataSource {

    private static final String API_KEY = BuildConfig.weather_api_appid;
    private final WeatherApiService weatherApiService;

    @Inject
    public WeatherRemoteDataSource(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    public void getCurrentWeather(double lat, double lon, Callback<WeatherResponse> callback) {
        Call<WeatherResponse> call = weatherApiService.getCurrentWeather(API_KEY,
                lat,
                lon,
                "metric",
                "it");

        call.enqueue(new retrofit2.Callback<>() {

            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call,
                                   @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Error getting weather data", new Exception(API_APPID_ERROR));
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                callback.onFailure("Error getting weather data", t);
            }
        });

    }
}
