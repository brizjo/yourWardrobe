package it.unimib.yourwardrobe.domain.model;

import java.io.Serializable;
import java.util.List;

public class Garment  implements Serializable {
    // Attributi corrispondenti ai campi Firestore
    private String id; // Utile per gestire il documento in locale
    private String name;private String category;
    private String subCategory;
    private String season;
    private String imageUrl;
    private List<String> color;  // Firestore Array -> Java List
    private List<String> fabric; // Firestore Array -> Java List
    private List<String> style;  // Firestore Array -> Java List

    public Garment() {
    }

    public Garment(String id, String name, String category, String subCategory,
                   String season, String imageUrl, List<String> color,
                   List<String> fabric, List<String> style) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.season = season;
        this.imageUrl = imageUrl;
        this.color = color;
        this.fabric = fabric;
        this.style = style;
    }



    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getColor() { return color; }
    public void setColor(List<String> color) { this.color = color; }

    public List<String> getFabric() { return fabric; }
    public void setFabric(List<String> fabric) { this.fabric = fabric; }

    public List<String> getStyle() { return style; }
    public void setStyle(List<String> style) { this.style = style; }

    // Metodo toString opzionale per debug
    @Override
    public String toString() {
        return "Garment{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}