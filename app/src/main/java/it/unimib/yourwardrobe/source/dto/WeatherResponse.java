package it.unimib.yourwardrobe.source.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("coord")
    @Expose
    public Coord coord;
    @SerializedName("weather")
    @Expose
    public List<Weather> weather;
    @SerializedName("base")
    @Expose
    public String base;
    @SerializedName("main")
    @Expose
    public Main main;
    @SerializedName("visibility")
    @Expose
    public Integer visibility;
    @SerializedName("wind")
    @Expose
    public Wind wind;
    @SerializedName("clouds")
    @Expose
    public Clouds clouds;
    @SerializedName("dt")
    @Expose
    public Integer dt;
    @SerializedName("sys")
    @Expose
    public Sys sys;
    @SerializedName("timezone")
    @Expose
    public Integer timezone;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("cod")
    @Expose
    public Integer cod;

    public static class Main {
        @SerializedName("temp")
        @Expose
        public Double temp;
        @SerializedName("feels_like")
        @Expose
        public Double feelsLike;
        @SerializedName("temp_min")
        @Expose
        public Double tempMin;
        @SerializedName("temp_max")
        @Expose
        public Double tempMax;
        @SerializedName("pressure")
        @Expose
        public Integer pressure;
        @SerializedName("humidity")
        @Expose
        public Integer humidity;
        @SerializedName("sea_level")
        @Expose
        public Integer seaLevel;
        @SerializedName("grnd_level")
        @Expose
        public Integer grndLevel;
    }

    public static class Weather {
        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("main")
        @Expose
        public String main;
        @SerializedName("description")
        @Expose
        public String description;
        @SerializedName("icon")
        @Expose
        public String icon;

    }

    public static class Coord {
        @SerializedName("lon")
        @Expose
        public Double lon;
        @SerializedName("lat")
        @Expose
        public Double lat;
    }

    public static class Wind {
        @SerializedName("speed")
        @Expose
        public Double speed;
        @SerializedName("deg")
        @Expose
        public Integer deg;
        @SerializedName("gust")
        @Expose
        public Double gust;
    }

    public static class Sys {
        @SerializedName("country")
        @Expose
        public String country;
        @SerializedName("sunrise")
        @Expose
        public Integer sunrise;
        @SerializedName("sunset")
        @Expose
        public Integer sunset;

    }

    public static class Clouds {
        @SerializedName("all")
        @Expose
        public Integer all;
    }

}

