package it.unimib.yourwardrobe.data.remote;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.UserPreferences;

@Singleton
public class ProfileRemoteDataSource {

    private static final String COLLECTION_USERS = "user";
    private static final String FIELD_PREFERENCES = "preferences";

    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;

    @Inject
    public ProfileRemoteDataSource(FirebaseFirestore firestore, FirebaseStorage storage, FirebaseAuth auth) {
        this.firestore = firestore;
        this.storage = storage;
        this.auth = auth;
    }

    public void loadPreferences(Callback<UserPreferences> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains(FIELD_PREFERENCES)) {
                        Map<String, Object> prefsMap = (Map<String, Object>) doc.get(FIELD_PREFERENCES);
                        if (prefsMap != null) {
                            List<String> styles = (List<String>) prefsMap.get("favoriteStyles");
                            List<String> colors = (List<String>) prefsMap.get("favoriteColors");
                            String avatarUrl = (String) prefsMap.get("avatarUrl");
                            callback.onSuccess(new UserPreferences(styles, colors, avatarUrl));
                        } else {
                            callback.onFailure("Preferenze nulle", null);
                        }
                    } else {
                        callback.onFailure("Documento non trovato", null);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage(), e));
    }

    public void savePreferences(List<String> styles, List<String> colors, String avatarUrl, Callback<Boolean> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }

        Map<String, Object> prefsMap = new java.util.HashMap<>();
        prefsMap.put("favoriteStyles", styles);
        prefsMap.put("favoriteColors", colors);
        prefsMap.put("avatarUrl", avatarUrl);

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .update(FIELD_PREFERENCES, prefsMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> {
                    Map<String, Object> userData = new java.util.HashMap<>();
                    userData.put(FIELD_PREFERENCES, prefsMap);
                    firestore.collection(COLLECTION_USERS)
                            .document(user.getUid())
                            .set(userData)
                            .addOnSuccessListener(aVoid2 -> callback.onSuccess(true))
                            .addOnFailureListener(e2 -> callback.onFailure(e2.getMessage(), e2));
                });
    }

    public void uploadAvatar(Uri imageUri, Callback<String> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }

        StorageReference avatarRef = storage.getReference()
                .child("avatars/" + user.getUid() + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        avatarRef.getDownloadUrl().addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                )
                .addOnFailureListener(e -> callback.onFailure(e.getMessage(), e));
    }

    public void updateAvatarUrl(String avatarUrl, Callback<Boolean> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }

        Map<String, Object> update = new java.util.HashMap<>();
        update.put(FIELD_PREFERENCES + ".avatarUrl", avatarUrl);

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .update(update)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage(), e));
    }

    public void getTotalGarments(Callback<Integer> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSuccess(0);
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .collection("garments")
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(querySnapshot.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage(), e));
    }

    public void getTotalOutfits(Callback<Integer> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSuccess(0);
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .collection("outfits")
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(querySnapshot.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage(), e));
    }
}