package it.unimib.yourwardrobe.source.repository;

import android.util.Log;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.source.dto.WeatherResponse;
import it.unimib.yourwardrobe.source.remote.WeatherRemoteDataSource;
import it.unimib.yourwardrobe.utils.WeatherUtil;

public class WeatherRepositoryImpl implements WeatherRepository {
    private static final String TAG = WeatherRepositoryImpl.class.getSimpleName();
    private final WeatherRemoteDataSource remoteDataSource;

    public WeatherRepositoryImpl(WeatherRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void getCurrentWeather(
            double lat, double lon,
            Callback<WeatherInfo> callback
    ) {
        remoteDataSource.getCurrentWeather(lat, lon, new Callback<>() {
            public void onSuccess(WeatherResponse data) {
                var weather = data.weather.get(0);
                var weatherInfo = new WeatherInfo(
                        WeatherUtil.getFormattedTemperature(data.main.temp),
                        weather.main,
                        WeatherUtil.getWeatherIconUrl(weather.icon),
                        WeatherUtil.getDrawableResourceForWeatherId(
                                weather.id,
                                data.sys.sunset,
                                data.sys.sunrise)

                );
                callback.onSuccess(weatherInfo);
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.e(TAG, errorMessage, t);
                callback.onFailure(errorMessage, t);
            }
        });
    }
}
