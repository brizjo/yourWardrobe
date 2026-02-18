package it.unimib.yourwardrobe.ui.shared;

import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;
import it.unimib.yourwardrobe.utils.Callback;
import it.unimib.yourwardrobe.utils.Resource;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private static final String TAG = AuthViewModel.class.getSimpleName();
    private static final String PREF_OUTFIT_KEY = "daily_outfit";
    private static final String PREF_OUTFIT_DATE_KEY = "outfit_date";

    private final AuthRepository authRepository;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<Resource<User>> authResult = new MutableLiveData<>();

    @Inject
    public AuthViewModel(AuthRepository authRepository, SharedPreferences sharedPreferences) {
        this.authRepository = authRepository;
        this.sharedPreferences = sharedPreferences;
        checkUserStatus();
    }

    public void signInWithEmail(String email, String password) {
        if (!isEmailValid(email)) {
            this.authResult.postValue(Resource.error("Formato email non valido", null));
            return;
        }

        if (!isPasswordValid(password)) {
            this.authResult.postValue(Resource.error("La password deve avere almeno 6 caratteri", null));
            return;
        }

        this.authResult.postValue(Resource.loading(null));
        this.authRepository.signInWithEmail(email, password, new Callback<>() {

            @Override
            public void onSuccess(User user) {
                authResult.postValue(Resource.success(user));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.w(TAG, errorMessage, t);
                authResult.setValue(Resource.error(errorMessage, null));
            }
        });

    }

    public void signInWithGoogle() {
        authResult.postValue(Resource.loading(null));
        authRepository.signInWithGoogle(new Callback<>() {
            @Override
            public void onSuccess(User data) {
                authResult.setValue(Resource.success(data));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                authResult.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    public void signUp(String username, String email, String password, String confirmPassword) {

        if (!isEmailValid(email)) {
            authResult.postValue(Resource.error("Formato email non valido", null));
            return;
        }

        if (!isPasswordValid(password)) {
            authResult.postValue(Resource.error("La password deve avere almeno 6 caratteri", null));
            return;
        }

        if (!password.equals(confirmPassword)) {
            authResult.postValue(Resource.error("Le password non corrispondono", null));
            return;
        }

        authRepository.signUp(username, email, password, new Callback<>() {

            @Override
            public void onSuccess(User data) {
                authResult.postValue(Resource.success(data));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                authResult.postValue(Resource.error(errorMessage, null));
            }
        });
    }

    public void signOut() {
        // Cancella outfit salvato prima del logout
        clearSavedOutfit();

        authRepository.signOut();
        authResult.setValue(Resource.success(null));
    }

    public void checkUserStatus() {
        var user = authRepository.getCurrentUser();
        authResult.setValue(Resource.success(user));
    }

    public LiveData<Resource<User>> getAuthResult() {
        return authResult;
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    /**
     * Cancella outfit salvato dalle SharedPreferences
     */
    private void clearSavedOutfit() {
        sharedPreferences.edit()
                .remove(PREF_OUTFIT_KEY)
                .remove(PREF_OUTFIT_DATE_KEY)
                .apply();
        Log.d(TAG, "Outfit salvato cancellato al logout");
    }
}