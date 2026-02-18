package it.unimib.yourwardrobe.data.repository;

import android.content.Context;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.data.remote.AuthRemoteDataSource;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;

public class AuthRepositoryImpl implements AuthRepository {

    private final Context context;
    private final AuthRemoteDataSource authRemoteDataSource;

    @Inject
    public AuthRepositoryImpl(
            @ApplicationContext Context context,
            AuthRemoteDataSource authRemoteDataSource) {
        this.context = context;
        this.authRemoteDataSource = authRemoteDataSource;
    }

    @Override
    public User getCurrentUser() {
        return this.authRemoteDataSource.getCurrentUser();
    }

    @Override
    public void signInWithEmail(String email, String password, Callback<User> callback) {
        this.authRemoteDataSource.signInWithEmail(email, password, callback);
    }

    @Override
    public void signUp(String username, String email, String password, Callback<User> callback) {
        this.authRemoteDataSource.signUp(username, email, password, callback);
    }

    @Override
    public void signInWithGoogle(Callback<User> callback) {
        this.authRemoteDataSource.signInWithGoogle(context, callback);

    }

    @Override
    public void signOut() {
        this.authRemoteDataSource.signOut();
    }
}
