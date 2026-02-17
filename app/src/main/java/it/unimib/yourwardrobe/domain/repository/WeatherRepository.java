package it.unimib.yourwardrobe.domain.repository;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.WeatherInfo;

public interface WeatherRepository {
    void getCurrentWeather(double lat, double lon, Callback<WeatherInfo> callback);
    void getForecastWeather(double lat, double lon, long dateMillis, Callback<WeatherInfo> callback);
}