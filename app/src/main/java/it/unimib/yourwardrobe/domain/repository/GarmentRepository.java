package it.unimib.yourwardrobe.domain.repository;

import android.graphics.Bitmap;

import java.util.List;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;

public interface GarmentRepository {

    void addGarment(Bitmap image, Garment garment, Callback<Boolean> callback);
    void validateGarment(Bitmap garmentImage, Callback<Boolean> callback);
    void deleteGarment(Garment garment, Callback<Boolean> callback);
    void getGarments(Callback<List<Garment>> callback);
    void updateGarment(Garment garment, Callback<Boolean> callback);
    void updateGarmentImage(Bitmap newImage, Garment garment, Callback<Boolean> callback);
    void getGarmentsByCategory(String category, Callback<List<Garment>> callback);
}