package it.unimib.yourwardrobe.data.remote;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.UserProfileChangeRequest;

import javax.inject.Inject;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.utils.Callback;

public class AuthRemoteDataSource {
    private final FirebaseAuth auth;

    @Inject
    public AuthRemoteDataSource(FirebaseAuth auth) {
        this.auth = auth;
    }

    public User getCurrentUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            var photoUrl = getUserPhotoUrl(firebaseUser);
            return new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(), photoUrl);
        }

        return null;
    }

    public void signInWithGoogle(Context context, Callback<User> callback) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId(context.getString(R.string.default_web_client_id)).setAutoSelectEnabled(true).build();

        GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build();

        CredentialManager credentialManager = CredentialManager.create(context);

        credentialManager.getCredentialAsync(context, request, new CancellationSignal(), ContextCompat.getMainExecutor(context), new CredentialManagerCallback<>() {
            @Override
            public void onResult(GetCredentialResponse result) {
                Credential credential = result.getCredential();
                if (credential instanceof CustomCredential && credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                    try {
                        var googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                        var idToken = googleIdTokenCredential.getIdToken();

                        authenticateFirebaseWithGoogle(idToken, callback);
                    } catch (Exception e) {
                        callback.onFailure("Error while parsing Google token", e);
                    }
                } else {
                    callback.onFailure("Credential type is not supported", null);
                }
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                callback.onFailure("Error Credential Manager", e);
            }
        });
    }

    private void authenticateFirebaseWithGoogle(String idToken, Callback<User> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                var firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    User user = mapFirebaseUserToUser(firebaseUser);
                    callback.onSuccess(user);
                }
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Errore login Firebase";
                callback.onFailure(errorMessage, null);
            }
        });
    }

    public void signInWithEmail(String email, String password, Callback<User> callback) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                var fUser = task.getResult().getUser();
                assert fUser != null;
                callback.onSuccess(mapFirebaseUserToUser(fUser));
            } else {
                callback.onFailure("AuthRemoteDataSource:signInWithEmail", task.getException());
            }
        });
    }

    public void signUp(String username, String email, String password, Callback<User> callback) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                var fUser = task.getResult().getUser();
                if (fUser != null) {
                    updateUserProfile(fUser, username, callback);
                }
            } else {
                callback.onFailure("AuthRemoteDatasource:signUp", task.getException());
            }
        });
    }

    public void signOut() {
        auth.signOut();
    }

    private void updateUserProfile(FirebaseUser firebaseUser, String username, Callback<User> callback) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();

        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(mapFirebaseUserToUser(firebaseUser));
            } else {
                callback.onFailure("AuthRemoteDataSource:updateUserProfile", task.getException());
            }
        });
    }

    private String getUserPhotoUrl(FirebaseUser firebaseUser) {
        return firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";
    }

    private User mapFirebaseUserToUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;

        return new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(), getUserPhotoUrl(firebaseUser));
    }

}
