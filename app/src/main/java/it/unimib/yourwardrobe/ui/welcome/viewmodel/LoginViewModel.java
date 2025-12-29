package it.unimib.yourwardrobe.ui.welcome.viewmodel;

import android.content.Context;
import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unimib.yourwardrobe.model.User;
import it.unimib.yourwardrobe.repository.UserRepository;

public class LoginViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<AuthenticationResult> authenticationResult;

    public LoginViewModel() {
        this.userRepository = new UserRepository();
        // Osserviamo lo stesso LiveData del repository
        this.authenticationResult = userRepository.getAuthenticationResult();
    }

    public void login(String email, String password) {
        if (!isEmailValid(email)) {
            authenticationResult.setValue(new AuthenticationResult(false, null, "Formato email non valido"));
            return;
        }

        if (!isPasswordValid(password)) {
            authenticationResult.setValue(new AuthenticationResult(false, null, "La password deve avere almeno 6 caratteri"));
            return;
        }

        // Usa il nuovo metodo getUser con flag isUserRegistered = true (Login)
        userRepository.getUser(email, password, true);
    }

    public void loginGoogle(Context context){
        // Usa il nuovo metodo getGoogleUser
        userRepository.getGoogleUser(context);
    }

    public void signUp(String email, String password, String confirmPassword) {
        if (!isEmailValid(email)) {
            authenticationResult.setValue(new AuthenticationResult(false, null, "Formato email non valido"));
            return;
        }
        if (!isPasswordValid(password)) {
            authenticationResult.setValue(new AuthenticationResult(false, null, "La password deve avere almeno 6 caratteri"));
            return;
        }
        if (!password.equals(confirmPassword)) {
            authenticationResult.setValue(new AuthenticationResult(false, null, "Le password non coincidono"));
            return;
        }

        // Usa il nuovo metodo getUser con flag isUserRegistered = false (SignUp)
        userRepository.getUser(email, password, false);
    }

    public LiveData<AuthenticationResult> getAuthenticationResult() {
        return authenticationResult;
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public static class AuthenticationResult {
        public final boolean success;
        public final User user;
        public final String errorMessage;

        public AuthenticationResult(boolean success, User user, String errorMessage) {
            this.success = success;
            this.user = user;
            this.errorMessage = errorMessage;
        }
    }
}
