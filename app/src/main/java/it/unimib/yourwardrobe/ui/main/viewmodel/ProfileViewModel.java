package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.domain.repository.ProfileRepository;
import it.unimib.yourwardrobe.domain.model.UserPreferences;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final ProfileRepository repository;

    @Inject
    public ProfileViewModel(ProfileRepository repository) {
        this.repository = repository;
    }

    // ========== PREFERENZE ==========

    public LiveData<UserPreferences> getUserPreferences() {
        return repository.getUserPreferences();
    }

    public void savePreferences(List<String> styles, List<String> colors) {
        repository.savePreferences(styles, colors);
    }

    public void loadPreferences() {
        repository.loadPreferences();
    }

    // ========== AVATAR ==========

    public LiveData<String> getAvatarUrl() {
        return repository.getAvatarUrl();
    }

    public void uploadAvatar(Uri imageUri) {
        repository.uploadAvatar(imageUri);
    }

    // ========== STATISTICHE ==========

    public LiveData<Integer> getTotalGarments() {
        return repository.getTotalGarments();
    }

    public LiveData<Integer> getTotalOutfits() {
        return repository.getTotalOutfits();
    }

    // ========== OBSERVERS ==========

    public LiveData<Boolean> getSaveSuccess() {
        return repository.getSaveSuccess();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }
}