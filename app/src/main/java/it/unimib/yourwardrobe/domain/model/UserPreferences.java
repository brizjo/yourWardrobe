package it.unimib.yourwardrobe.domain.model;

import java.util.ArrayList;
import java.util.List;

public class UserPreferences {
    private List<String> favoriteStyles;
    private List<String> favoriteColors;
    private String avatarUrl;

    public UserPreferences() {
        this.favoriteStyles = new ArrayList<>();
        this.favoriteColors = new ArrayList<>();
        this.avatarUrl = "";
    }

    public UserPreferences(List<String> favoriteStyles, List<String> favoriteColors, String avatarUrl) {
        this.favoriteStyles = favoriteStyles != null ? favoriteStyles : new ArrayList<>();
        this.favoriteColors = favoriteColors != null ? favoriteColors : new ArrayList<>();
        this.avatarUrl = avatarUrl != null ? avatarUrl : "";
    }

    public List<String> getFavoriteStyles() {
        return favoriteStyles;
    }

    public void setFavoriteStyles(List<String> favoriteStyles) {
        this.favoriteStyles = favoriteStyles;
    }

    public List<String> getFavoriteColors() {
        return favoriteColors;
    }

    public void setFavoriteColors(List<String> favoriteColors) {
        this.favoriteColors = favoriteColors;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}