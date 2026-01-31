package it.unimib.yourwardrobe.source.repository;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.source.remote.GarmentRemoteDataSource;


import android.graphics.Bitmap;
import android.util.Log;

public class GarmentRepositoryImpl implements GarmentRepository {
    private final GarmentRemoteDataSource dataSource;


    public GarmentRepositoryImpl(GarmentRemoteDataSource dataSource) {
        this.dataSource = dataSource;

    }



    @Override
    public void validateGarment(Bitmap garmentBitmap, Callback<Boolean> callback) {

        dataSource.isGarment( garmentBitmap, callback);
    }


    @Override
    public void addGarment(Bitmap image, Garment garment, Callback<Boolean> callback) {
        dataSource.uploadImage(image, new Callback<String>() {
            @Override
            public void onSuccess(String imageUrl){
                garment.setImageUrl(imageUrl);
                dataSource.saveGarmentDocument(garment, new Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d("GarmentRepositoryImpl", "Documento salvato con successo.");
                        callback.onSuccess(true);
                    }

                    @Override
                    public void onFailure(String errorMessage, Throwable t) {
                        callback.onFailure("Errore salvataggio dati: " + errorMessage, t);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                callback.onFailure("Errore caricamento immagine: " + errorMessage, t);
            }


        });

    }


}
