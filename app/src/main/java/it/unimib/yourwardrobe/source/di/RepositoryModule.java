package it.unimib.yourwardrobe.source.di;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.source.repository.WeatherRepositoryImpl;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    public abstract WeatherRepository bindWeatherRepository(WeatherRepositoryImpl impl);
}
