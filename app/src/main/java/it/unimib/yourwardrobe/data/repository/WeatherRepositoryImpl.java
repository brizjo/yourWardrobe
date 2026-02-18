package it.unimib.yourwardrobe.data.repository;

import android.util.Log;

import javax.inject.Inject;

import it.unimib.yourwardrobe.data.dto.ForecastResponse;
import it.unimib.yourwardrobe.data.dto.WeatherResponse;
import it.unimib.yourwardrobe.data.remote.WeatherRemoteDataSource;
import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.utils.Callback;
import it.unimib.yourwardrobe.utils.WeatherUtil;

public class WeatherRepositoryImpl implements WeatherRepository {
    private static final String TAG = WeatherRepositoryImpl.class.getSimpleName();
    private final WeatherRemoteDataSource remoteDataSource;

    @Inject
    public WeatherRepositoryImpl(WeatherRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void getCurrentWeather(double lat, double lon, Callback<WeatherInfo> callback) {
        remoteDataSource.getCurrentWeather(lat, lon, new Callback<>() {
            @Override
            public void onSuccess(WeatherResponse data) {
                var weather = data.weather.get(0);
                callback.onSuccess(new WeatherInfo(
                        WeatherUtil.getFormattedTemperature(data.main.temp),
                        weather.main,
                        WeatherUtil.getWeatherIconUrl(weather.icon),
                        WeatherUtil.getDrawableResourceForWeatherId(
                                weather.id,
                                data.sys.sunset,
                                data.sys.sunrise)
                ));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.e(TAG, errorMessage, t);
                callback.onFailure(errorMessage, t);
            }
        });
    }

    @Override
    public void getForecastWeather(double lat, double lon, long dateMillis, Callback<WeatherInfo> callback) {
        remoteDataSource.getForecastWeather(lat, lon, dateMillis, new Callback<>() {
            @Override
            public void onSuccess(ForecastResponse.ForecastItem item) {
                var weather = item.weather.get(0);
                boolean isDay = item.sys != null && "d".equals(item.sys.pod);
                callback.onSuccess(new WeatherInfo(
                        WeatherUtil.getFormattedTemperature(item.main.temp),
                        weather.main,
                        WeatherUtil.getWeatherIconUrl(weather.icon),
                        WeatherUtil.getDrawableResourceForWeatherId(
                                weather.id,
                                isDay ? 1 : 0,
                                isDay ? 0 : 1)
                ));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.e(TAG, errorMessage, t);
                callback.onFailure(errorMessage, t);
            }
        });
    }
}