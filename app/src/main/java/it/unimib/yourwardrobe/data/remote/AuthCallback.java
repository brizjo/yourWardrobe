package it.unimib.yourwardrobe.data.remote;

import it.unimib.yourwardrobe.domain.model.User;

/**
 * Interfaccia per gestire le risposte asincrone delle operazioni di autenticazione.
 */
public interface AuthCallback {
    void onSuccess(User user);

    void onFailure(String errorMessage);
}
