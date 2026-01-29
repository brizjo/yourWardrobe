package it.unimib.yourwardrobe.source.remote;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.User;

public class AuthRemoteDataSource {

    private final FirebaseAuth firebaseAuth;

    public AuthRemoteDataSource() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public User getCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            return new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName());
        }
        return null;
    }

    public void signInWithGoogle(Context context, AuthCallback callback) {
        // 1. Configurazione opzioni Google
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // 2. Avvio CredentialManager
        CredentialManager credentialManager = CredentialManager.create(context);

        credentialManager.getCredentialAsync(
                context,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(context),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential &&
                                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            GoogleIdTokenCredential googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(credential.getData());
                            String idToken = googleIdTokenCredential.getIdToken();

                            // 3. Autenticazione Firebase
                            authenticateFirebaseWithGoogle(idToken, callback);

                        } else {
                            callback.onFailure("Tipo di credenziale non supportato");
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        callback.onFailure("Errore Credential Manager: " + e.getMessage());
                    }
                }
        );
    }

    private void authenticateFirebaseWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName());
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("Errore recupero utente");
                        }
                    } else {
                        String errorMessage = "Errore login Firebase";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        callback.onFailure(errorMessage);
                    }
                });
    }

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName());
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("Errore recupero utente");
                        }
                    } else {
                        String errorMessage = "Errore di autenticazione";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        callback.onFailure(errorMessage);
                    }
                });
    }

    public void signUp(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName());
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("Errore recupero utente");
                        }
                    } else {
                        String errorMessage = "Errore di registrazione";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        callback.onFailure(errorMessage);
                    }
                });
    }
}
