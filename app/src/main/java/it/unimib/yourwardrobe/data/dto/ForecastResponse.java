package it.unimib.yourwardrobe.data.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastResponse {

    @SerializedName("list")
    @Expose
    public List<ForecastItem> list;

    @SerializedName("city")
    @Expose
    public City city;

    public static class ForecastItem {
        @SerializedName("dt")
        @Expose
        public long dt; // unix timestamp

        @SerializedName("main")
        @Expose
        public WeatherResponse.Main main;

        @SerializedName("weather")
        @Expose
        public List<WeatherResponse.Weather> weather;

        @SerializedName("sys")
        @Expose
        public Sys sys;
    }

    public static class Sys {
        @SerializedName("pod")
        @Expose
        public String pod; // "d" = day, "n" = night
    }

    public static class City {
        @SerializedName("sunrise")
        @Expose
        public long sunrise;

        @SerializedName("sunset")
        @Expose
        public long sunset;
    }
}