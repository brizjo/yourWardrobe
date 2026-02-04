package it.unimib.yourwardrobe.domain.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Garment  implements Serializable {
    // Attributi corrispondenti ai campi Firestore
    private String id; // Utile per gestire il documento in locale
    private String name;private String category;
    private String subCategory;

    @ServerTimestamp // Annotazione di Firestore
    private Date createdAt;


    private String imageUrl;
    private List<String> color;  // Firestore Array -> Java List
    private List<String> fabric; // Firestore Array -> Java List
    private List<String> style;  // Firestore Array -> Java List

    public Garment() {
    }

    public Garment(Garment other) {
        this.id = other.id;
        this.name = other.name;
        this.imageUrl = other.imageUrl;
        this.category = other.category;
        this.createdAt = other.createdAt;
        // nuove liste per evitare di modificare la stessa istanza in memoria
        this.color = (other.color != null) ? new ArrayList<>(other.color) : null;
        this.style = (other.style != null) ? new ArrayList<>(other.style) : null;
        this.fabric = (other.fabric != null) ? new ArrayList<>(other.fabric) : null;

    }

    public Garment(String id, String name, String category, String subCategory,
                   String season, String imageUrl, List<String> color,
                   List<String> fabric, List<String> style) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.imageUrl = imageUrl;
        this.color = color;
        this.fabric = fabric;
        this.style = style;
    }


    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Garment garment = (Garment) o;
        return Objects.equals(id, garment.id) &&
                Objects.equals(name, garment.name) &&
                Objects.equals(imageUrl, garment.imageUrl) &&
                Objects.equals(category, garment.category) &&
                Objects.equals(color, garment.color) &&
                Objects.equals(style, garment.style) &&
                Objects.equals(fabric, garment.fabric);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, name, imageUrl, category, color, style, fabric);
    }
}