package it.unimib.yourwardrobe.core.di;

import android.app.Application;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.source.api.WeatherApiService;
import it.unimib.yourwardrobe.source.remote.GarmentRemoteDataSource;
import it.unimib.yourwardrobe.source.remote.WeatherRemoteDataSource;
import it.unimib.yourwardrobe.source.repository.GarmentRepositoryImpl;
import it.unimib.yourwardrobe.source.repository.WeatherRepositoryImpl;
import it.unimib.yourwardrobe.utils.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceLocator {

    private static ServiceLocator instance;

    private ServiceLocator() {
    }

    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                instance = new ServiceLocator();
            }
        }
        return instance;
    }

    public WeatherApiService getWeatherApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.WEATHER_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(WeatherApiService.class);
    }

    public WeatherRepository getWeatherRepository(Application application) {
        var weatherRemoteDataSource = new WeatherRemoteDataSource(
                application.getString(R.string.weather_api_appid)
        );
        return new WeatherRepositoryImpl(weatherRemoteDataSource);
    }

    public GarmentRepository getGarmentRepository() {
        return new GarmentRepositoryImpl(new GarmentRemoteDataSource());
    }

}
