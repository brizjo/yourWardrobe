package it.unimib.yourwardrobe.source.repository;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.source.remote.GarmentRecognitionDataSource;

import android.graphics.Bitmap;

public class GarmentRepositoryImpl implements GarmentRepository {
    private final GarmentRecognitionDataSource dataSource;

    public GarmentRepositoryImpl(GarmentRecognitionDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override //todo
    public void saveGarment(Garment garment, Callback<Boolean> Callback) {


    }

    @Override
    public void validateGarment(Bitmap garmentBitmap, Callback<Boolean> callback) {

        dataSource.isGarment( garmentBitmap, callback);
    }


}
