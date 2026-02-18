package it.unimib.yourwardrobe.utils;

public interface Callback<T> {
    void onSuccess(T data);

    void onFailure(String errorMessage, Throwable t);
}

