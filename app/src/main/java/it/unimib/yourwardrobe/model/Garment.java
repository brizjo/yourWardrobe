package it.unimib.yourwardrobe.model;

public class Garment {

    private String name;
    private String category;
    private String imageUrl;

    private String [] colors;
    private String season;

    private String [] styles;

    private String subcategory;

    public Garment(String subcategory, String[] styles, String season, String name, String[] colors, String category, String imageUrl) {
        this.subcategory = subcategory;
        this.styles = styles;
        this.season = season;
        this.name = name;
        this.colors = colors;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public String[] getColors() {
        return colors;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String[] getStyles() {
        return styles;
    }

    public void setStyles(String[] styles) {
        this.styles = styles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
