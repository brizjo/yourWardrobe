package it.unimib.yourwardrobe.domain.repository;

import java.util.List;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Outfit;

public interface OutfitRepository {
    void saveOutfit(Outfit outfit, Callback<Boolean> callback);
    void getOutfits(Callback<List<Outfit>> callback);
    void deleteOutfit(Outfit outfit, Callback<Boolean> callback);
    void updateOutfit(Outfit outfit, Callback<Boolean> callback);
}
