package com.example.outfitowl;

public class user {

    String profilePic, displayName, email, username, password;

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public user(String profilePic, String username, String email, String displayName, String password) {
        this.profilePic = profilePic;
        this.displayName = displayName;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public user() {
    }
}
