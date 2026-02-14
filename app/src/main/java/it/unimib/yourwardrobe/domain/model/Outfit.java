package it.unimib.yourwardrobe.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Outfit implements Serializable {

    private String id;
    private String name;
    private String season;
    private List<Garment> garments;
    private Date createdAt;

    public Outfit() {
        this.garments = new ArrayList<>();
    }

    public Outfit(String name, String season, List<Garment> garments) {
        this.name = name;
        this.season = season;
        this.garments = garments != null ? garments : new ArrayList<>();
        this.createdAt = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public List<Garment> getGarments() { return garments; }
    public void setGarments(List<Garment> garments) { this.garments = garments; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}