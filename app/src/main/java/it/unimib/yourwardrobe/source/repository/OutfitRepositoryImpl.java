package it.unimib.yourwardrobe.source.repository;

import java.util.List;

import javax.inject.Inject;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.source.remote.OutfitRemoteDataSource;

public class OutfitRepositoryImpl implements OutfitRepository {

    private final OutfitRemoteDataSource dataSource;

    @Inject
    public OutfitRepositoryImpl(OutfitRemoteDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveOutfit(Outfit outfit, Callback<Boolean> callback) {
        dataSource.saveOutfit(outfit, callback);
    }

    @Override
    public void getOutfits(Callback<List<Outfit>> callback) {
        dataSource.getOutfits(callback);
    }

    @Override
    public void deleteOutfit(Outfit outfit, Callback<Boolean> callback) {
        dataSource.deleteOutfit(outfit, callback);
    }
}
