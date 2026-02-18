package it.unimib.yourwardrobe.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import it.unimib.yourwardrobe.data.remote.ProfileRemoteDataSource;
import it.unimib.yourwardrobe.domain.model.UserPreferences;
import it.unimib.yourwardrobe.domain.repository.ProfileRepository;

@Singleton
public class ProfileRepositoryImpl implements ProfileRepository {

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_STYLES = "favorite_styles";
    private static final String KEY_COLORS = "favorite_colors";

    private final SharedPreferences sharedPreferences;
    private final ProfileRemoteDataSource dataSource;

    private final MutableLiveData<UserPreferences> userPreferencesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> avatarUrlLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @Inject
    public ProfileRepositoryImpl(@ApplicationContext Context context, ProfileRemoteDataSource dataSource) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dataSource = dataSource;
        loadPreferences();
    }

    @Override
    public LiveData<UserPreferences> getUserPreferences() {
        return userPreferencesLiveData;
    }

    @Override
    public void loadPreferences() {
        dataSource.loadPreferences(new it.unimib.yourwardrobe.core.functional.Callback<UserPreferences>() {
            @Override
            public void onSuccess(UserPreferences prefs) {
                userPreferencesLiveData.setValue(prefs);
                savePreferencesLocally(prefs);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorLiveData.setValue("Errore caricamento preferenze: " + error);
                userPreferencesLiveData.setValue(loadPreferencesLocally());
            }
        });
    }

    @Override
    public void savePreferences(List<String> styles, List<String> colors) {
        UserPreferences currentPrefs = userPreferencesLiveData.getValue();
        String avatarUrl = currentPrefs != null ? currentPrefs.getAvatarUrl() : "";

        dataSource.savePreferences(styles, colors, avatarUrl, new it.unimib.yourwardrobe.core.functional.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                UserPreferences newPrefs = new UserPreferences(styles, colors, avatarUrl);
                userPreferencesLiveData.setValue(newPrefs);
                savePreferencesLocally(newPrefs);
                saveSuccessLiveData.setValue(true);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorLiveData.setValue("Errore salvataggio: " + error);
                saveSuccessLiveData.setValue(false);
            }
        });
    }

    @Override
    public void uploadAvatar(Uri imageUri) {
        dataSource.uploadAvatar(imageUri, new it.unimib.yourwardrobe.core.functional.Callback<String>() {
            @Override
            public void onSuccess(String avatarUrl) {
                dataSource.updateAvatarUrl(avatarUrl, new it.unimib.yourwardrobe.core.functional.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        avatarUrlLiveData.setValue(avatarUrl);
                        UserPreferences currentPrefs = userPreferencesLiveData.getValue();
                        if (currentPrefs == null) currentPrefs = new UserPreferences();
                        UserPreferences updatedPrefs = new UserPreferences(
                                currentPrefs.getFavoriteStyles(),
                                currentPrefs.getFavoriteColors(),
                                avatarUrl
                        );
                        userPreferencesLiveData.setValue(updatedPrefs);
                    }

                    @Override
                    public void onFailure(String error, Throwable t) {
                        errorLiveData.setValue("Errore aggiornamento avatar: " + error);
                    }
                });
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorLiveData.setValue("Errore upload avatar: " + error);
            }
        });
    }

    @Override
    public LiveData<Integer> getTotalGarments() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        dataSource.getTotalGarments(new it.unimib.yourwardrobe.core.functional.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                result.setValue(count);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                result.setValue(0);
            }
        });
        return result;
    }

    @Override
    public LiveData<Integer> getTotalOutfits() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        dataSource.getTotalOutfits(new it.unimib.yourwardrobe.core.functional.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                result.setValue(count);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                result.setValue(0);
            }
        });
        return result;
    }

    private void savePreferencesLocally(UserPreferences prefs) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_STYLES, String.join(",", prefs.getFavoriteStyles()));
        editor.putString(KEY_COLORS, String.join(",", prefs.getFavoriteColors()));
        editor.apply();
    }

    private UserPreferences loadPreferencesLocally() {
        String stylesStr = sharedPreferences.getString(KEY_STYLES, "");
        String colorsStr = sharedPreferences.getString(KEY_COLORS, "");

        List<String> styles = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        if (!stylesStr.isEmpty()) {
            for (String s : stylesStr.split(",")) if (!s.trim().isEmpty()) styles.add(s.trim());
        }
        if (!colorsStr.isEmpty()) {
            for (String c : colorsStr.split(",")) if (!c.trim().isEmpty()) colors.add(c.trim());
        }

        return new UserPreferences(styles, colors, "");
    }

    @Override
    public LiveData<String> getAvatarUrl() {
        return avatarUrlLiveData;
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