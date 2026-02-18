package it.unimib.yourwardrobe.domain.repository;

import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.utils.Callback;

public interface WeatherRepository {
    void getCurrentWeather(double lat, double lon, Callback<WeatherInfo> callback);

    void getForecastWeather(double lat, double lon, long dateMillis, Callback<WeatherInfo> callback);
}