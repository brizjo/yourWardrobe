package it.unimib.yourwardrobe.domain.repository;

import android.graphics.Bitmap;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
public interface GarmentRepository {

    public void addGarment(Bitmap image, Garment garment, Callback<Boolean> callback);

    public void validateGarment(Bitmap  garmentIMage, Callback<Boolean> Callback); //metodo per controllare se la foto di garment è effettivamente un vestito


}
