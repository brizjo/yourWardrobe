package it.unimib.yourwardrobe.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Outfit implements Serializable {

    private String id;
    private String name;
    private String style;
    private List<Garment> garments;

    // Richiesto da Firestore per la deserializzazione
    public Outfit() {
        this.garments = new ArrayList<>();
    }

    public Outfit(String name, String style, List<Garment> garments) {
        this.name = name;
        this.style = style;
        this.garments = garments != null ? garments : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public List<Garment> getGarments() { return garments; }
    public void setGarments(List<Garment> garments) { this.garments = garments; }
}