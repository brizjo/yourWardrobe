package it.unimib.yourwardrobe.domain.repository;

import android.graphics.Bitmap;

import java.util.List;

import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.utils.Callback;

public interface GarmentRepository {

    void validateGarment(Bitmap garmentBitmap, Callback<Boolean> callback);

    void addGarment(Bitmap image, Garment garment, Callback<Boolean> callback);

    void updateGarmentImage(Bitmap newImage, Garment garment, Callback<Boolean> callback);

    void deleteGarment(Garment garment, Callback<Boolean> callback);

    void updateGarment(Garment garment, Callback<Boolean> callback);

    void getGarmentsByCategory(String category, Callback<List<Garment>> callback);

    void getGarments(Callback<List<Garment>> callback);

    // NUOVI METODI
    void detectGarmentAttributes(Bitmap bitmap, Callback<GarmentAttributes> callback);

    void saveGarmentWithImage(Bitmap image, Garment garment, Callback<Boolean> callback);

    // CLASSE INNER PER GLI ATTRIBUTI
    class GarmentAttributes {
        private final String category;
        private final String season;
        private final List<String> colors;

        public GarmentAttributes(String category, String season, List<String> colors) {
            this.category = category;
            this.season = season;
            this.colors = colors;
        }

        public String getCategory() {
            return category;
        }

        public String getSeason() {
            return season;
        }

        public List<String> getColors() {
            return colors;
        }
    }
}