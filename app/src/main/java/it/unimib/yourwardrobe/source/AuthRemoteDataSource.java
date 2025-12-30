package it.unimib.yourwardrobe.source;

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
import com.google.firebase.auth.FirebaseUser;//utente di firebase
import com.google.firebase.auth.GoogleAuthProvider;


//dependencies per firestore
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.type.Date;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.model.User;

public class AuthRemoteDataSource {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore database;
    public AuthRemoteDataSource() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.database = FirebaseFirestore.getInstance();
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

    private boolean checkUsernameAvailable(String username, AuthCallback callback) {

        AtomicBoolean returnValue = new AtomicBoolean(true);
        database.collection("usernames").document(username).get().
                addOnCompleteListener(usernameAvailable ->{
                    if(usernameAvailable.isSuccessful()){

                        returnValue.set(usernameAvailable.getResult().exists());

                    }

                    else{
                        callback.onFailure("Errore connessione con il database");
                    }

                });
        return returnValue.get();

    }

    public void signUp(String username, String email, String password, AuthCallback callback) {

        if(checkUsernameAvailable(username, callback))
            callback.onFailure("username già in uso");

        else
            performAuthCreation(username, email, password, callback);
    }

    private void performAuthCreation(String username, String email, String password, AuthCallback callback){

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // 3. Scrivi su Firestore
                            saveUserToFirestore(firebaseUser.getUid(), username, email, callback);
                        } else {
                            callback.onFailure("Errore recupero utente dopo registrazione.");
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Errore registrazione Auth";
                        callback.onFailure(error);
                    }
                });
    }

    private void saveUserToFirestore(String uid, String username, String email, AuthCallback callback) {
        WriteBatch batch = database.batch();

        // Riferimento documento collezione 'user'
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        //userData.put("creationDate", new com.android.identity.android.legacy.Timestamp(new Date()));
        userData.put("profilePictureUrl", ""); // Vuoto per ora

        // Riferimento documento collezione 'usernames'
        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", uid);

        batch.set(database.collection("user").document(uid), userData);
        batch.set(database.collection("usernames").document(username), usernameData);

        batch.commit().addOnCompleteListener(batchTask -> {
            if (batchTask.isSuccessful()) {
                // TUTTO OK: Auth + DB
                User user = new User(uid, email, username); // Aggiorna costruttore User se necessario
                callback.onSuccess(user);
            } else {
                // GRAVE: Utente creato in Auth ma DB fallito.
                // Best practice: Cancellare l'utente Auth per evitare inconsistenze (account zombie)
                if (firebaseAuth.getCurrentUser() != null) {
                    firebaseAuth.getCurrentUser().delete();
                }
                callback.onFailure("Errore salvataggio dati utente. Riprova.");
            }
        });

    }

}
