package it.unimib.yourwardrobe.core.functional;

public interface Callback<T> {
    void onSuccess(T data);

    void onFailure(String errorMessage, Throwable t);
}

