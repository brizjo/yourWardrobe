package it.unimib.yourwardrobe.source.repository;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.source.remote.GarmentRecognitionDataSource;


import android.graphics.Bitmap;
import android.util.Log;

public class GarmentRepositoryImpl implements GarmentRepository {
    private final GarmentRecognitionDataSource dataSource;


    public GarmentRepositoryImpl(GarmentRecognitionDataSource dataSource) {
        this.dataSource = dataSource;

    }

    @Override //todo
    public void addGarment(Bitmap image, Garment garment, Callback<Void> callback) {
        Log.d("GarmentRepositoryImpl", "addGarment called with image: " + image);
        /*FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            String errorMsg = "Utente non autenticato. Impossibile aggiungere il capo.";
            callback.onFailure(errorMsg, new IllegalStateException(errorMsg));
            return;
        }
        String userId = currentUser.getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos); // Qualità 90 per ridurre le dimensioni
        byte[] data = baos.toByteArray();
        String imagePath = "garments/" + userId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(imagePath);

        UploadTask uploadTask = imageRef.putBytes(data);
        */
    }

    @Override
    public void validateGarment(Bitmap garmentBitmap, Callback<Boolean> callback) {

        dataSource.isGarment( garmentBitmap, callback);
    }


}
