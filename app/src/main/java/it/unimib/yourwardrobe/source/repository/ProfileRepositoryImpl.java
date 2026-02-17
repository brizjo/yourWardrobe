package it.unimib.yourwardrobe.source.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import it.unimib.yourwardrobe.domain.model.UserPreferences;
import it.unimib.yourwardrobe.domain.repository.ProfileRepository;

@Singleton
public class ProfileRepositoryImpl implements ProfileRepository {

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_STYLES = "favorite_styles";
    private static final String KEY_COLORS = "favorite_colors";
    private static final String COLLECTION_USERS = "user";
    private static final String FIELD_PREFERENCES = "preferences";

    private final SharedPreferences sharedPreferences;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;

    private final MutableLiveData<UserPreferences> userPreferencesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> avatarUrlLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @Inject
    public ProfileRepositoryImpl(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();

        loadPreferences();
    }

    @Override
    public LiveData<UserPreferences> getUserPreferences() {
        return userPreferencesLiveData;
    }

    @Override
    public void loadPreferences() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            userPreferencesLiveData.setValue(new UserPreferences());
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

                            UserPreferences prefs = new UserPreferences(styles, colors, avatarUrl);
                            userPreferencesLiveData.setValue(prefs);
                            savePreferencesLocally(prefs);
                        }
                    } else {
                        userPreferencesLiveData.setValue(loadPreferencesLocally());
                    }
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Errore caricamento preferenze: " + e.getMessage());
                    userPreferencesLiveData.setValue(loadPreferencesLocally());
                });
    }

    @Override
    public void savePreferences(List<String> styles, List<String> colors) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorLiveData.setValue("Utente non autenticato");
            return;
        }

        UserPreferences currentPrefs = userPreferencesLiveData.getValue();
        String avatarUrl = currentPrefs != null ? currentPrefs.getAvatarUrl() : "";

        UserPreferences newPrefs = new UserPreferences(styles, colors, avatarUrl);

        Map<String, Object> prefsMap = new HashMap<>();
        prefsMap.put("favoriteStyles", styles);
        prefsMap.put("favoriteColors", colors);
        prefsMap.put("avatarUrl", avatarUrl);

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .update(FIELD_PREFERENCES, prefsMap)
                .addOnSuccessListener(aVoid -> {
                    userPreferencesLiveData.setValue(newPrefs);
                    savePreferencesLocally(newPrefs);
                    saveSuccessLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put(FIELD_PREFERENCES, prefsMap);

                    firestore.collection(COLLECTION_USERS)
                            .document(user.getUid())
                            .set(userData)
                            .addOnSuccessListener(aVoid2 -> {
                                userPreferencesLiveData.setValue(newPrefs);
                                savePreferencesLocally(newPrefs);
                                saveSuccessLiveData.setValue(true);
                            })
                            .addOnFailureListener(e2 -> {
                                errorLiveData.setValue("Errore salvataggio: " + e2.getMessage());
                                saveSuccessLiveData.setValue(false);
                            });
                });
    }

    private void savePreferencesLocally(UserPreferences prefs) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String stylesStr = String.join(",", prefs.getFavoriteStyles());
        String colorsStr = String.join(",", prefs.getFavoriteColors());

        editor.putString(KEY_STYLES, stylesStr);
        editor.putString(KEY_COLORS, colorsStr);
        editor.apply();
    }

    private UserPreferences loadPreferencesLocally() {
        String stylesStr = sharedPreferences.getString(KEY_STYLES, "");
        String colorsStr = sharedPreferences.getString(KEY_COLORS, "");

        List<String> styles = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        if (!stylesStr.isEmpty()) {
            String[] stylesArray = stylesStr.split(",");
            for (String style : stylesArray) {
                if (!style.trim().isEmpty()) {
                    styles.add(style.trim());
                }
            }
        }

        if (!colorsStr.isEmpty()) {
            String[] colorsArray = colorsStr.split(",");
            for (String color : colorsArray) {
                if (!color.trim().isEmpty()) {
                    colors.add(color.trim());
                }
            }
        }

        return new UserPreferences(styles, colors, "");
    }

    @Override
    public LiveData<String> getAvatarUrl() {
        return avatarUrlLiveData;
    }

    @Override
    public void uploadAvatar(Uri imageUri) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorLiveData.setValue("Utente non autenticato");
            return;
        }

        StorageReference avatarRef = storage.getReference()
                .child("avatars/" + user.getUid() + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String avatarUrl = uri.toString();
                            updateAvatarUrl(avatarUrl);
                        })
                )
                .addOnFailureListener(e ->
                        errorLiveData.setValue("Errore upload avatar: " + e.getMessage())
                );
    }

    private void updateAvatarUrl(String avatarUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        UserPreferences currentPrefs = userPreferencesLiveData.getValue();
        if (currentPrefs == null) {
            currentPrefs = new UserPreferences();
        }

        UserPreferences updatedPrefs = new UserPreferences(
                currentPrefs.getFavoriteStyles(),
                currentPrefs.getFavoriteColors(),
                avatarUrl
        );

        Map<String, Object> update = new HashMap<>();
        update.put(FIELD_PREFERENCES + ".avatarUrl", avatarUrl);

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    avatarUrlLiveData.setValue(avatarUrl);
                    userPreferencesLiveData.setValue(updatedPrefs);
                })
                .addOnFailureListener(e ->
                        errorLiveData.setValue("Errore aggiornamento avatar: " + e.getMessage())
                );
    }

    @Override
    public LiveData<Integer> getTotalGarments() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            result.setValue(0);
            return result;
        }

        // Query sulla SUBCOLLECTION dentro il documento utente
        firestore.collection("user")
                .document(user.getUid())
                .collection("garments")  // ← Subcollection!
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    android.util.Log.d("ProfileRepo", "Garments trovati: " + querySnapshot.size());
                    result.setValue(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ProfileRepo", "Errore garments: " + e.getMessage());
                    result.setValue(0);
                });

        return result;
    }

    @Override
    public LiveData<Integer> getTotalOutfits() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            result.setValue(0);
            return result;
        }

        // Query sulla SUBCOLLECTION dentro il documento utente
        firestore.collection("user")
                .document(user.getUid())
                .collection("outfits")  // ← Subcollection!
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    android.util.Log.d("ProfileRepo", "Outfits trovati: " + querySnapshot.size());
                    result.setValue(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ProfileRepo", "Errore outfits: " + e.getMessage());
                    result.setValue(0);
                });

        return result;
    }


    @Override
    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccessLiveData;
    }

    @Override
    public LiveData<String> getError() {
        return errorLiveData;
    }
}