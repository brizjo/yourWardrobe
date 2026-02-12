package it.unimib.yourwardrobe.source.remote;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import javax.inject.Inject;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Outfit;

public class OutfitRemoteDataSource {

    private static final String TAG = "OutfitRemoteDataSource";

    private final FirebaseFirestore firestore;
    private final AuthRemoteDataSource auth;

    @Inject
    public OutfitRemoteDataSource(AuthRemoteDataSource auth, FirebaseFirestore firestore) {
        this.auth = auth;
        this.firestore = firestore;
    }

    public void saveOutfit(Outfit outfit, Callback<Boolean> callback) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onFailure("Problemi con autenticazione", new IllegalStateException("Utente non autenticato"));
            return;
        }

        DocumentReference newDoc = firestore.collection("user")
                .document(uid)
                .collection("outfits")
                .document();

        outfit.setId(newDoc.getId());

        newDoc.set(outfit)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Outfit salvato con successo su Firestore.");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Errore durante il salvataggio dell'outfit: " + e.getMessage());
                    callback.onFailure("Errore durante il salvataggio dell'outfit", e);
                });
    }

    public void getOutfits(Callback<List<Outfit>> callback) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onFailure("Problemi con autenticazione", new IllegalStateException("Utente non autenticato"));
            return;
        }

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
            callback.onFailure("Problemi con autenticazione", new IllegalStateException("Utente non autenticato"));
            return;
        }
        if (outfit.getId() == null) {
            callback.onFailure("Outfit non trovato: dati mancanti", new IllegalStateException("ID non valido"));
            return;
        }

        firestore.collection("user")
                .document(uid)
                .collection("outfits")
                .document(outfit.getId())
                .delete()
                .addOnSuccessListener(x -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onFailure("Errore durante la cancellazione dell'outfit", e));
    }
}
