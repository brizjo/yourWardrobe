package it.unimib.yourwardrobe.source.repository;

import android.content.Context;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.repository.UserRepository;
import it.unimib.yourwardrobe.source.remote.AuthCallback;
import it.unimib.yourwardrobe.source.remote.AuthRemoteDataSource;

public class UserRepositoryImpl implements UserRepository {

    private final Context context;
    private final AuthRemoteDataSource dataSource;

    public UserRepositoryImpl(Context context, AuthRemoteDataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @Override
    public void getUser(Callback<User> callback, String email, String password, boolean isUserRegistered) {
        if (isUserRegistered) {
            signInWithEmail(callback, email, password);
        } else {
            signUp(callback, email, password);
        }
    }

    @Override
    public void getGoogleUser(Callback<User> callback) {
        dataSource.signInWithGoogle(context, new AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage, new Exception(errorMessage));
            }
        });
    }

    @Override
    public User getCurrentUser() {
        return dataSource.getCurrentUser();
    }

    private void signInWithEmail(Callback<User> callback, String email, String password) {
        dataSource.signInWithEmail(email, password, new AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage, new Exception(errorMessage));
            }
        });
    }

    private void signUp(Callback<User> callback, String email, String password) {
        dataSource.signUp(email, password, new AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage, new Exception(errorMessage));
            }
        });
    }
}
