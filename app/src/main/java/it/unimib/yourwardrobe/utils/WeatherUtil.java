package it.unimib.yourwardrobe.utils;

import it.unimib.yourwardrobe.R;

public class WeatherUtil {

    private WeatherUtil() {

    }

    public static int getDrawableResourceForWeatherId(int weatherId, long sunsetUTC, long sunriseUTC) {
        var currentTimeUTC = System.currentTimeMillis() / 1000;
        var day = currentTimeUTC >= sunriseUTC && currentTimeUTC <= sunsetUTC;

        if (weatherId >= 200 && weatherId < 300)
            return R.drawable.thunderstorm;
        if (weatherId >= 300 && weatherId < 600)
            return R.drawable.rain;
        if (weatherId >= 600 && weatherId < 700)
            return R.drawable.snow;
        if (weatherId == 800) {
            return day ? R.drawable.clear_sky_day : R.drawable.clear_sky_night;
        }
        if (weatherId == 801) {
            return day ? R.drawable.clear_sky_day : R.drawable.clear_sky_night;
        }
        if (weatherId == 802) {
            return day ? R.drawable.scattered_clouds_day : R.drawable.scattered_clouds_night;
        }
        if (weatherId == 803) {
            return day ? R.drawable.scattered_clouds_day : R.drawable.scattered_clouds_night;
        }

        return R.drawable.mist;
    }

    public static String getWeatherIconUrl(String icon) {
        return String.format("https://openweathermap.org/img/wn/%s@2x.png", icon);
    }

    public static String getFormattedTemperature(double temp) {
        return String.format("%.1f°C", temp);
    }


}
