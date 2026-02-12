package it.unimib.yourwardrobe.domain.model;

public class Outfit {
    public String sweaterUrl;
    public String jacketUrl;
    public String pantsUrl;
    public String glassesUrl;
    public String bootsUrl;
    public String earringsUrl;

    public Outfit(String sweaterUrl, String jacketUrl, String pantsUrl, String glassesUrl, String bootsUrl, String earringsUrl) {
        this.sweaterUrl = sweaterUrl;
        this.jacketUrl = jacketUrl;
        this.pantsUrl = pantsUrl;
        this.glassesUrl = glassesUrl;
        this.bootsUrl = bootsUrl;
        this.earringsUrl = earringsUrl;
    }

    public String getSweaterUrl() {
        return sweaterUrl;
    }

    public String getJacketUrl() {
        return jacketUrl;
    }

    public String getPantsUrl() {
        return pantsUrl;
    }

    public String getGlassesUrl() {
        return glassesUrl;
    }

    public String getBootsUrl() {
        return bootsUrl;
    }

    public String getEarringsUrl() {
        return earringsUrl;
    }
}
