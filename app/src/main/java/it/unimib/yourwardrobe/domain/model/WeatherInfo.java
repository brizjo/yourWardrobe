package it.unimib.yourwardrobe.domain.model;

public class WeatherInfo {
    private final String temperature;
    private final String condition;
    private final String iconUrl;
    private final int backgroundResId;

    public WeatherInfo(String temperature, String condition, String iconUrl, int backgroundResId) {
        this.temperature = temperature;
        this.condition = condition;
        this.iconUrl = iconUrl;
        this.backgroundResId = backgroundResId;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getBackgroundResId() {
        return backgroundResId;
    }
}
