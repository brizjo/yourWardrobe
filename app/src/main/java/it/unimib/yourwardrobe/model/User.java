package it.unimib.yourwardrobe.model;

public class User {
    private String uid;
    private String email;
    private String displayName;
    // Puoi aggiungere altri campi come photoUrl se ti servono

    public User(String uid, String email, String displayName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
