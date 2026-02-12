package it.unimib.yourwardrobe.source.di;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.source.repository.AuthRepositoryImpl;
import it.unimib.yourwardrobe.source.repository.OutfitRepositoryImpl;
import it.unimib.yourwardrobe.source.repository.GarmentRepositoryImpl;
import it.unimib.yourwardrobe.source.repository.WeatherRepositoryImpl;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    public abstract WeatherRepository bindWeatherRepository(WeatherRepositoryImpl impl);

    @Binds
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl impl);

    @Binds
    public abstract GarmentRepository bindGarmentRepository(GarmentRepositoryImpl impl);

    @Binds
    public abstract OutfitRepository bindOutfitRepository(OutfitRepositoryImpl impl);
}
