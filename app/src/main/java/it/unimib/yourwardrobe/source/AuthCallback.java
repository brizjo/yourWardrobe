package it.unimib.yourwardrobe.source;

import it.unimib.yourwardrobe.model.User;

/**
 * Interfaccia per gestire le risposte asincrone delle operazioni di autenticazione.
 */
public interface AuthCallback {
    void onSuccess(User user);
    void onFailure(String errorMessage);
}
