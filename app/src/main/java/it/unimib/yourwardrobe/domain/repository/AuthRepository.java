package it.unimib.yourwardrobe.domain.repository;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.User;

public interface AuthRepository {

    User getCurrentUser();

    void signInWithEmail(String email, String password, Callback<User> callback);

    void signUp(String username, String email, String password, Callback<User> callback);

    void signInWithGoogle(Callback<User> callback);

    void signOut();
}
