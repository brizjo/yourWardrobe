package it.unimib.yourwardrobe.core.di;

import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.source.remote.GarmentRemoteDataSource;
import it.unimib.yourwardrobe.source.repository.GarmentRepositoryImpl;

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

    public GarmentRepository getGarmentRepository() {
        return new GarmentRepositoryImpl(new GarmentRemoteDataSource());
    }

}
