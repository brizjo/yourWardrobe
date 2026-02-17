package it.unimib.yourwardrobe.domain.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.unimib.yourwardrobe.domain.model.UserPreferences;

public interface ProfileRepository {

    // Preferenze
    LiveData<UserPreferences> getUserPreferences();
    void loadPreferences();
    void savePreferences(List<String> styles, List<String> colors);

    // Avatar
    LiveData<String> getAvatarUrl();
    void uploadAvatar(Uri imageUri);

    // Statistiche
    LiveData<Integer> getTotalGarments();
    LiveData<Integer> getTotalOutfits();
    // Observers
    LiveData<Boolean> getSaveSuccess();
    LiveData<String> getError();
}