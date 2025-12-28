package it.unimib.yourwardrobe.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;

import it.unimib.yourwardrobe.source.AuthRemoteDataSource;
import it.unimib.yourwardrobe.ui.welcome.viewmodel.LoginViewModel;

public class UserRepository {

    private final AuthRemoteDataSource authRemoteDataSource;

    public UserRepository() {
        this.authRemoteDataSource = new AuthRemoteDataSource();
    }

    public void signInWithGoogle(Context context, final MutableLiveData<LoginViewModel.AuthenticationResult> resultLiveData) {
        authRemoteDataSource.signInWithGoogle(context, new AuthRemoteDataSource.AuthCallback() {
            @Override
            public void onSuccess() {
                resultLiveData.postValue(new LoginViewModel.AuthenticationResult(true, null));
            }

            @Override
            public void onFailure(String errorMessage) {
                resultLiveData.postValue(new LoginViewModel.AuthenticationResult(false, errorMessage));
            }
        });
    }

    public void signInWithEmail(String email, String password, final MutableLiveData<LoginViewModel.AuthenticationResult> resultLiveData) {
        authRemoteDataSource.signInWithEmail(email, password, new AuthRemoteDataSource.AuthCallback() {
            @Override
            public void onSuccess() {
                resultLiveData.postValue(new LoginViewModel.AuthenticationResult(true, null));
            }

            @Override
            public void onFailure(String errorMessage) {
                resultLiveData.postValue(new LoginViewModel.AuthenticationResult(false, errorMessage));
            }
        });
    }

    public void signUp(String email, String password, final MutableLiveData<LoginViewModel.AuthenticationResult> resultLiveData) {
        authRemoteDataSource.signUp(email, password, new AuthRemoteDataSource.AuthCallback() {
            @Override
            public void onSuccess() {
                resultLiveData.postValue(new LoginViewModel.AuthenticationResult(true, null));
            }

            @Override
            public void onFailure(String errorMessage) {
                resultLiveData.postValue(new LoginViewModel.AuthenticationResult(false, errorMessage));
            }
        });
    }
}
