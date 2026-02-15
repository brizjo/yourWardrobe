package it.unimib.yourwardrobe.utils;

public enum ImageValidationState {
    VALID,
    INVALID_CONFIRMATION_NEEDED, // Stato per quando l'immagine non è valida
    ERROR,
    UNCHECKED // Stato iniziale
}
