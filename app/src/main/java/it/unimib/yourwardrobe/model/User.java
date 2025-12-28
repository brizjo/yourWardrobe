package it.unimib.yourwardrobe.model;

public class User {
    private String uid;
    private String email;
    private String username;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class

}

    public User(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}


