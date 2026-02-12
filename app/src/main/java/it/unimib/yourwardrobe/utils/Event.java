package it.unimib.yourwardrobe.utils;

public class Event<T> {

    private T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Restituisce il contenuto e impedisce che venga usato di nuovo.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Restituisce il contenuto, anche se è già stato gestito.
     */
    public T peekContent() {
        return content;
    }
}
