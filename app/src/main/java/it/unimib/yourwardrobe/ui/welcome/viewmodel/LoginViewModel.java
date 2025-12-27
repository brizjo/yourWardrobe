package it.unimib.yourwardrobe.ui.welcome.viewmodel;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginViewModel extends ViewModel {

    // LiveData per notificare il fragment del risultato del login
    private final MutableLiveData<AuthenticationResult> authenticationResult = new MutableLiveData<>();

    // Metodo chiamato dal Fragment quando l'utente preme "Login"
    public void login(String email, String password) {
        //  Validazione dell'input
        if (!isEmailValid(email)) {
            authenticationResult.setValue(new AuthenticationResult(false, "Formato email non valido"));
            return;
        }

        if (!isPasswordValid(password)) {
            authenticationResult.setValue(new AuthenticationResult(false, "La password deve avere almeno 6 caratteri"));
            return;
        }

        // Logica di Autenticazione (per ora simulato)
        // Qui chiamata a Repository o Firebase

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        //login è riuscito
                        authenticationResult.setValue(new AuthenticationResult(true, null));
                    } else {
                        //login non riuscito
                        String errorMessage = "Errore di autenticazione";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        authenticationResult.setValue(new AuthenticationResult(false, errorMessage));
                    }
                }
        );
    }

    public void signUp(String email, String password, String confirmPassword) {
        //Validazione dell'input
        if (!isEmailValid(email)) {
            authenticationResult.setValue(new AuthenticationResult(false, "Formato email non valido"));
            return;
        }
        if (!isPasswordValid(password)) {
            authenticationResult.setValue(new AuthenticationResult(false, "La password deve avere almeno 6 caratteri"));
            return;
        }
        if (!password.equals(confirmPassword)) {
            authenticationResult.setValue(new AuthenticationResult(false,"Le password non coincidono"));
            return;
        }
        //qui chiamata a repository o firebase
        // TODO: Implementare la registrazione reale

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //login è riuscito
                authenticationResult.setValue(new AuthenticationResult(true, null));
            }
            else{

                String errorMessage = "Errore di registrazione";
                if (task.getException() != null) {
                    errorMessage = task.getException().getMessage();
                }
                authenticationResult.setValue(new AuthenticationResult(false, errorMessage));
            }



        });
    }
    public LiveData<AuthenticationResult> getAuthenticationResult() {
        return authenticationResult;
    }

    // Helper per validare l'email
    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Helper per validare la password
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // Classe interna per gestire lo stato del risultato
    public static class AuthenticationResult {
        public final boolean success;
        public final String errorMessage;

        public AuthenticationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }
}
