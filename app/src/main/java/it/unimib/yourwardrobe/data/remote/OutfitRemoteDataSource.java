package it.unimib.yourwardrobe.data.remote;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import javax.inject.Inject;

import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.utils.Callback;

public class OutfitRemoteDataSource {

    private static final String TAG = "OutfitRemoteDS";
    private final FirebaseFirestore firestore;
    private final AuthRemoteDataSource auth;

    @Inject
    public OutfitRemoteDataSource(AuthRemoteDataSource auth, FirebaseFirestore firestore) {
        this.auth = auth;
        this.firestore = firestore;
        Log.d(TAG, "DataSource creato - Firestore: " + (firestore != null) + ", Auth: " + (auth != null));
    }

    public void saveOutfit(Outfit outfit, Callback<Boolean> callback) {
        Log.d(TAG, "=== SAVE OUTFIT ===");

        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "User NULL");
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            Log.e(TAG, "UID NULL");
            callback.onFailure("UID non disponibile", new IllegalStateException("UID null"));
            return;
        }

        Log.d(TAG, "UID: " + uid);

        try {
            DocumentReference newDoc = firestore.collection("user")
                    .document(uid)
                    .collection("outfits")
                    .document();

            String docId = newDoc.getId();
            outfit.setId(docId);

            Log.d(TAG, "Path: user/" + uid + "/outfits/" + docId);
            Log.d(TAG, "Nome: " + outfit.getName() + ", Stagione: " + outfit.getSeason() + ", Capi: " + outfit.getGarments().size());

            newDoc.set(outfit)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "FIRESTORE SUCCESS - ID: " + docId);
                        callback.onSuccess(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "FIRESTORE ERROR: " + e.getMessage());
                        e.printStackTrace();
                        callback.onFailure("Errore Firestore: " + e.getMessage(), e);
                    });

        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Exception: " + e.getMessage(), e);
        }
    }

    public void getOutfits(Callback<List<Outfit>> callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        firestore.collection("user").document(uid).collection("outfits")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage(), error);
                        return;
                    }
                    if (value != null) {
                        List<Outfit> outfits = value.toObjects(Outfit.class);
                        callback.onSuccess(outfits);
                    }
                });
    }

    public void deleteOutfit(Outfit outfit, Callback<Boolean> callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("UID null"));
            return;
        }
        if (outfit.getId() == null) {
            callback.onFailure("ID outfit mancante", new IllegalStateException("ID null"));
            return;
        }

        firestore.collection("user")
                .document(uid)
                .collection("outfits")
                .document(outfit.getId())
                .delete()
                .addOnSuccessListener(x -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onFailure("Errore eliminazione", e));
    }


    public void updateOutfit(Outfit outfit, Callback<Boolean> callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || outfit.getId() == null) return;

        firestore.collection("user").document(uid)
                .collection("outfits").document(outfit.getId())
                .set(outfit) // Sovrascrive il documento con la nuova lista di componenti
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage(), e));
    }
}