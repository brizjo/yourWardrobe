package it.unimib.yourwardrobe.ui.welcome.viewmodel;

import android.util.Log;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.core.functional.Result;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private static final String TAG = AuthViewModel.class.getSimpleName();
    private final AuthRepository authRepository;
    private final MutableLiveData<Result<User>> authResult = new MutableLiveData<>();

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        checkUserStatus();
    }

    public void signInWithEmail(String email, String password) {
        if (!isEmailValid(email)) {
            this.authResult.postValue(Result.error("Formato email non valido", null));
            return;
        }

        if (!isPasswordValid(password)) {
            this.authResult.postValue(Result.error("La password deve avere almeno 6 caratteri", null));
            return;
        }

        this.authResult.postValue(Result.loading(null));
        this.authRepository.signInWithEmail(email, password, new Callback<>() {

            @Override
            public void onSuccess(User user) {
                authResult.postValue(Result.success(user));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.w(TAG, errorMessage, t);
                authResult.setValue(Result.error(errorMessage, null));
            }
        });

    }

    public void signInWithGoogle() {
        authResult.postValue(Result.loading(null));
        authRepository.signInWithGoogle(new Callback<>() {
            @Override
            public void onSuccess(User data) {
                authResult.setValue(Result.success(data));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                authResult.setValue(Result.error(errorMessage, null));
            }
        });
    }

    public void signUp(String username, String email, String password, String confirmPassword) {

        if (!isEmailValid(email)) {
            authResult.postValue(Result.error("Formato email non valido", null));
            return;
        }

        if (!isPasswordValid(password)) {
            authResult.postValue(Result.error("La password deve avere almeno 6 caratteri", null));
            return;
        }

        if (!password.equals(confirmPassword)) {
            authResult.postValue(Result.error("Le password non corrispondono", null));
            return;
        }

        authRepository.signUp(username, email, password, new Callback<>() {

            @Override
            public void onSuccess(User data) {
                authResult.postValue(Result.success(data));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                authResult.postValue(Result.error(errorMessage, null));
            }
        });
    }

    public void signOut() {
        authRepository.signOut();
        authResult.setValue(Result.success(null));
    }

    public void checkUserStatus() {
        var user = authRepository.getCurrentUser();
        authResult.setValue(Result.success(user));
    }

    public LiveData<Result<User>> getAuthResult() {
        return authResult;
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

}
