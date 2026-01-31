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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.User;

public class AuthRemoteDataSource {


    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore database;

    public AuthRemoteDataSource() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.database = FirebaseFirestore.getInstance();
    }

    public  User getCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            return new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName());
        }
        return null;
    }

    public void signInWithGoogle(Context context, AuthCallback callback) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

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
                            try {
                                GoogleIdTokenCredential googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(credential.getData());
                                String idToken = googleIdTokenCredential.getIdToken();

                                authenticateFirebaseWithGoogle(idToken, callback);
                            } catch (Exception e) {
                                callback.onFailure("Errore durante il parsing del token Google");
                            }
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
                        AuthResult authResult = task.getResult();
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null && authResult != null) {
                            if (authResult.getAdditionalUserInfo() != null && authResult.getAdditionalUserInfo().isNewUser()) {
                                String username = firebaseUser.getDisplayName();
                                if (username == null || username.isEmpty()) {
                                    username = firebaseUser.getEmail().split("@")[0];
                                }
                                saveUserToFirestore(firebaseUser.getUid(), username, firebaseUser.getEmail(), callback);
                            } else {
                                fetchUserFromFirestore(firebaseUser.getUid(), firebaseUser.getEmail(), callback);
                            }
                        } else {
                            callback.onFailure("Errore recupero utente");
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Errore login Firebase";
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
                            fetchUserFromFirestore(firebaseUser.getUid(), email, callback);
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Errore di autenticazione";
                        callback.onFailure(errorMessage);
                    }
                });
    }

    public void signUp(String username, String email, String password, AuthCallback callback) {
        database.collection("usernames").document(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && task.getResult().exists()) {
                            callback.onFailure("username già in uso");
                        } else {
                            performAuthCreation(username, email, password, callback);
                        }
                    } else {
                        callback.onFailure("Errore connessione con il database");
                    }
                });
    }

    private void performAuthCreation(String username, String email, String password, AuthCallback callback){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        saveUserToFirestore(firebaseUser.getUid(), username, email, callback);
                                    });
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Errore registrazione Auth";
                        callback.onFailure(error);
                    }
                });
    }

    public void fetchUserFromFirestore(String uid, String email, AuthCallback callback) {
        database.collection("user").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String username = document.getString("username");
                        
                        // Sincronizziamo lo username scelto con il profilo Firebase Auth per usi futuri
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null && firebaseUser.getDisplayName() == null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates);
                        }

                        User user = new User(uid, email, username);
                        callback.onSuccess(user);
                    } else {
                        callback.onSuccess(getCurrentUser());
                    }
                });
    }

    private void saveUserToFirestore(String uid, String username, String email, AuthCallback callback) {
        WriteBatch batch = database.batch();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("profilePictureUrl", "");

        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", uid);

        batch.set(database.collection("user").document(uid), userData);
        batch.set(database.collection("usernames").document(username), usernameData);

        batch.commit().addOnCompleteListener(batchTask -> {
            if (batchTask.isSuccessful()) {
                User user = new User(uid, email, username);
                callback.onSuccess(user);
            } else {
                if (firebaseAuth.getCurrentUser() != null) {
                    firebaseAuth.getCurrentUser().delete();
                }
                callback.onFailure("Errore salvataggio dati utente.");
            }
        });
    }
}
