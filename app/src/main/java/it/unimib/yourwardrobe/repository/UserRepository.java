package it.unimib.yourwardrobe.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.source.remote.AuthCallback;
import it.unimib.yourwardrobe.source.remote.AuthRemoteDataSource;
import it.unimib.yourwardrobe.ui.welcome.viewmodel.LoginViewModel;

public class UserRepository {

    private final AuthRemoteDataSource authRemoteDataSource;
    private final MutableLiveData<LoginViewModel.AuthenticationResult> authenticationResult;

    public UserRepository() {
        this.authRemoteDataSource = new AuthRemoteDataSource();
        this.authenticationResult = new MutableLiveData<>();
    }

    public MutableLiveData<LoginViewModel.AuthenticationResult> getAuthenticationResult() {
        return authenticationResult;
    }

    public MutableLiveData<LoginViewModel.AuthenticationResult> getUser(String email, String password, boolean isUserRegistered) {
        if (isUserRegistered) {
            signInWithEmail(email, password);
        } else {
            signUp(email, password);
        }
        return authenticationResult;
    }

    public MutableLiveData<LoginViewModel.AuthenticationResult> getGoogleUser(Context context) {
        signInWithGoogle(context);
        return authenticationResult;
    }

    public User getCurrentUser() {
        return authRemoteDataSource.getCurrentUser();
    }

    private void signInWithGoogle(Context context) {
        authRemoteDataSource.signInWithGoogle(context, new AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authenticationResult.postValue(new LoginViewModel.AuthenticationResult(true, user, null));
            }

            @Override
            public void onFailure(String errorMessage) {
                authenticationResult.postValue(new LoginViewModel.AuthenticationResult(false, null, errorMessage));
            }
        });
    }

    private void signInWithEmail(String email, String password) {
        authRemoteDataSource.signInWithEmail(email, password, new AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authenticationResult.postValue(new LoginViewModel.AuthenticationResult(true, user, null));
            }

            @Override
            public void onFailure(String errorMessage) {
                authenticationResult.postValue(new LoginViewModel.AuthenticationResult(false, null, errorMessage));
            }
        });
    }

    private void signUp(String email, String password) {
        authRemoteDataSource.signUp(email, password, new AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authenticationResult.postValue(new LoginViewModel.AuthenticationResult(true, user, null));
            }

            @Override
            public void onFailure(String errorMessage) {
                authenticationResult.postValue(new LoginViewModel.AuthenticationResult(false, null, errorMessage));
            }
        });
    }
}
