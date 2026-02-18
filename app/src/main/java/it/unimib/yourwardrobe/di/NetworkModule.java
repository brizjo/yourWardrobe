package it.unimib.yourwardrobe.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import it.unimib.yourwardrobe.data.api.WeatherApiService;
import it.unimib.yourwardrobe.utils.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public WeatherApiService provideWeatherApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.WEATHER_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(WeatherApiService.class);
    }

}
