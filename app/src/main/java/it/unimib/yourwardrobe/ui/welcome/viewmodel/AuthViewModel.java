package it.unimib.yourwardrobe.ui.welcome.viewmodel;

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

    private final AuthRepository authRepository;
    private final MutableLiveData<Result<User>> _authResult = new MutableLiveData<>();
    public final LiveData<Result<User>> authResult = _authResult;

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void signInWithEmail(String email, String password) {
        if (!isEmailValid(email)) {
            this._authResult.postValue(Result.error("Formato email non valido", null));
            return;
        }

        if (!isPasswordValid(password)) {
            this._authResult.postValue(Result.error("La password deve avere almeno 6 caratteri", null));
            return;
        }

        this._authResult.postValue(Result.loading(null));
        this.authRepository.signInWithEmail(email, password, new Callback<User>() {

            @Override
            public void onSuccess(User data) {
                _authResult.postValue(Result.success(data));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                _authResult.postValue(Result.error(errorMessage, null));
            }
        });

    }

    public void signInWithGoogle() {
        _authResult.postValue(Result.loading(null));
        this.authRepository.signInWithGoogle(new Callback<>() {
            @Override
            public void onSuccess(User data) {
                _authResult.postValue(Result.success(data));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                _authResult.postValue(Result.error(errorMessage, null));
            }
        });
    }

    public void signUp(String username, String email, String password, String confirmPassword) {
        //TODO; CONTROLLO CAMPO USERNAME

        if (!isEmailValid(email)) {
            _authResult.postValue(Result.error("Formato email non valido", null));
            return;
        }

        if (!isPasswordValid(password)) {
            _authResult.postValue(Result.error("La password deve avere almeno 6 caratteri", null));
            return;
        }
        if (!password.equals(confirmPassword)) {
            _authResult.postValue(Result.error("Le password non corrispondono", null));
            return;
        }

        this.authRepository.signUp(username, email, password, new Callback<>() {

            @Override
            public void onSuccess(User data) {
                _authResult.postValue(Result.success(data));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                _authResult.postValue(Result.error(errorMessage, null));
            }
        });
    }

    public void signOut() {
        this.authRepository.signOut();
    }

    public void getCurrentUser() {
        var user = this.authRepository.getCurrentUser();
        if (user != null) {
            _authResult.postValue(Result.success(user));
        }
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

}
