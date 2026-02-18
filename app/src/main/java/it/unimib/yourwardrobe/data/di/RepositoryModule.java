package it.unimib.yourwardrobe.data.di;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import it.unimib.yourwardrobe.data.repository.AuthRepositoryImpl;
import it.unimib.yourwardrobe.data.repository.GarmentRepositoryImpl;
import it.unimib.yourwardrobe.data.repository.OutfitRepositoryImpl;
import it.unimib.yourwardrobe.data.repository.ProfileRepositoryImpl;
import it.unimib.yourwardrobe.data.repository.WeatherRepositoryImpl;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.domain.repository.ProfileRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;

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

    @Binds
    public abstract ProfileRepository bindProfileRepository(ProfileRepositoryImpl impl);


}
