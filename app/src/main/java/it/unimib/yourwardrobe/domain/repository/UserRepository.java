package it.unimib.yourwardrobe.domain.repository;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.User;

public interface UserRepository {
    void getUser(Callback<User> callback, String email, String password, boolean isUserRegistered);

    void getGoogleUser(Callback<User> callback);

    User getCurrentUser();

}
