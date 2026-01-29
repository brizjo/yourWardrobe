package it.unimib.yourwardrobe.source.api;

import static it.unimib.yourwardrobe.utils.Constants.WEATHER_APPID_PARAMETER;
import static it.unimib.yourwardrobe.utils.Constants.WEATHER_CURRENT_ENDPOINT;
import static it.unimib.yourwardrobe.utils.Constants.WEATHER_LANG_PARAMETER;
import static it.unimib.yourwardrobe.utils.Constants.WEATHER_LAT_PARAMETER;
import static it.unimib.yourwardrobe.utils.Constants.WEATHER_LON_PARAMETER;
import static it.unimib.yourwardrobe.utils.Constants.WEATHER_UNITS_PARAMETER;

import it.unimib.yourwardrobe.source.dto.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET(WEATHER_CURRENT_ENDPOINT)
    Call<WeatherResponse> getCurrentWeather(
            @Query(WEATHER_APPID_PARAMETER) String appid,
            @Query(WEATHER_LAT_PARAMETER) double lat,
            @Query(WEATHER_LON_PARAMETER) double lon,
            @Query(WEATHER_UNITS_PARAMETER) String units,
            @Query(WEATHER_LANG_PARAMETER) String lang
    );
}
