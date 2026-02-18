package it.unimib.yourwardrobe.data.repository;

import java.util.List;

import javax.inject.Inject;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.data.remote.OutfitRemoteDataSource;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

public class OutfitRepositoryImpl implements OutfitRepository {

    private final OutfitRemoteDataSource dataSource;

    @Inject
    public OutfitRepositoryImpl(OutfitRemoteDataSource dataSource) {
        this.dataSource = dataSource;
        android.util.Log.d("OutfitRepository", "Repository creato");
    }

    @Override
    public void saveOutfit(Outfit outfit, Callback<Boolean> callback) {
        android.util.Log.d("OutfitRepository", "Repository: saveOutfit chiamato");
        android.util.Log.d("OutfitRepository", "Nome: " + outfit.getName() + ", Capi: " + outfit.getGarments().size());

        dataSource.saveOutfit(outfit, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                android.util.Log.d("OutfitRepository", "Repository: salvataggio OK");
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                android.util.Log.e("OutfitRepository", "Repository: errore - " + error);
                callback.onFailure(error, t);
            }
        });
    }

    @Override
    public void getOutfits(Callback<List<Outfit>> callback) {
        dataSource.getOutfits(callback);
    }

    @Override
    public void deleteOutfit(Outfit outfit, Callback<Boolean> callback) {
        dataSource.deleteOutfit(outfit, callback);
    }

    @Override
    public void updateOutfit(Outfit outfit, Callback<Boolean> callback) {
        dataSource.updateOutfit(outfit, callback);
    }


}