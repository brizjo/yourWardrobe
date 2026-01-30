package it.unimib.yourwardrobe.source.repository;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.source.remote.GarmentRecognitionDataSource;


import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import java.io.ByteArrayOutputStream;

public class GarmentRepositoryImpl implements GarmentRepository {
    private final GarmentRecognitionDataSource dataSource;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public GarmentRepositoryImpl(GarmentRecognitionDataSource dataSource, FirebaseAuth firebaseAuth,
                                 FirebaseFirestore firestore,
                                 FirebaseStorage storage) {
        this.dataSource = dataSource;
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
        this.storage = storage;
    }

    @Override //todo
    public void addGarment(Bitmap image, Garment garment, Callback<Void> callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
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
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                garment.setImageUrl(imageUrl);
                firestore.collection("users").document(userId)
                        .collection("garments").add(garment)
                        .addOnSuccessListener(documentReference -> {
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            // D. Errore durante il salvataggio su Firestore
                            callback.onFailure("Errore nel salvataggio dei dati.", e);
                        });
            }).addOnFailureListener(e -> {
                // B. Errore nel recuperare l'URL di download
                callback.onFailure("Errore nel recupero dell'URL dell'immagine.", e);
            });
        }).addOnFailureListener(e -> {
            // E. Errore durante il caricamento dell'immagine
            callback.onFailure("Errore durante il caricamento dell'immagine.", e);
        });
    }

    @Override
    public void validateGarment(Bitmap garmentBitmap, Callback<Boolean> callback) {

        dataSource.isGarment( garmentBitmap, callback);
    }


}
