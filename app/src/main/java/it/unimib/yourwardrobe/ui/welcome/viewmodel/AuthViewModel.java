package it.unimib.yourwardrobe.ui.welcome.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

//import com.bumptech.glide.load.engine.Resource;
import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;
import it.unimib.yourwardrobe.utils.Event;
import it.unimib.yourwardrobe.utils.Resource;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    public final MutableLiveData<Event<Resource<User>>> authResult = new MutableLiveData<>();



    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void signInWithEmail(String email, String password) {
        if (!isEmailValid(email)) {
            authResult.postValue(new Event<>(Resource.error("Formato email non valido", null)));
            return;
        }

        if (!isPasswordValid(password)) {
            authResult.postValue(new Event<>(Resource.error("La password deve avere almeno 6 caratteri", null)));
            return;
        }

        authResult.postValue(new Event<>(Resource.loading(null)));
        this.authRepository.signInWithEmail(email, password, new Callback<User>() {

            @Override
            public void onSuccess(User data) {
                authResult.postValue(new Event<>(Resource.success(data)));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                authResult.postValue(new Event<>(Resource.error(errorMessage, null)));
            }
        });

    }

    public void signInWithGoogle() {
        authResult.postValue(new Event<>(Resource.loading(null)));
        this.authRepository.signInWithGoogle(new Callback<>() {
            @Override
            public void onSuccess(User data) {
                authResult.postValue(new Event<>(Resource.success(data)));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                authResult.postValue(new Event<>(Resource.error(errorMessage, null)));
            }
        });
    }

    public void signUp(String username, String email, String password, String confirmPassword) {
        //TODO; CONTROLLO CAMPO USERNAME

        if (!isEmailValid(email)) {
            authResult.postValue(new Event<>(Resource.error("Formato email non valido", null)));
            return;
        }

        if (!isPasswordValid(password)) {
            authResult.postValue(new Event<>(Resource.error("La password deve avere almeno 6 caratteri", null)));
            return;
        }
        if (!password.equals(confirmPassword)) {
            authResult.postValue(new Event<>(Resource.error("Le password non corrispondono", null)));
            return;
        }

        authResult.postValue(new Event<>(Resource.loading(null)));
        this.authRepository.signUp(username, email, password, new Callback<>() {

            @Override
            public void onSuccess(User data) {
                authResult.postValue(new Event<>(Resource.success(data)));
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                authResult.postValue(new Event<>(Resource.error(errorMessage, null)));
            }
        });
    }

    public void signOut() {
        this.authRepository.signOut();
    }

    public void getCurrentUser() {
        var user = this.authRepository.getCurrentUser();
        if (user != null) {
            authResult.postValue(new Event<>(Resource.success(user)));
        }
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

}
