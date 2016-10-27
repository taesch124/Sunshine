package com.danieltaeschler.udacity.sunshine.sunshine;

/**
 * Created by Daniel Taeschler on 6/10/2016.
 */
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class WeatherDataParser {
    private static final String TAG = WeatherDataParser.class.getSimpleName();

    private static final String JSON_CITY_DETAILS = "city";
    private static final String JSON_CITY_NAME = "name";
    private static final String JSON_COUNTRY_NAME = "country";
    private static final String JSON_DAY = "list";
    private static final String JSON_TEMPERATURE_INFO = "temp";
    private static final String JSON_MAX_TEMPERATURE = "max";
    private static final String JSON_MIN_TEMPERATURE = "min";
    private static final String JSON_WEATHER = "weather";
    private static final String JSON_WEATHER_DETAILS = "main";

    private static String getReadableDateString(long time) {
        //API returns unix timestamp (in milliseconds). Format to date
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
        return shortenedDateFormat.format(time);
    }

    public static String formatHighLows(double high, double low) {
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    public static String[] getWeatherDataFromJson(String forecastJsonString, int numDays) throws JSONException {
        String cityInfo;

        JSONObject forecastJson = new JSONObject(forecastJsonString);

        //Get the json object of city details
        JSONObject cityDetails = forecastJson.getJSONObject(JSON_CITY_DETAILS);
        String cityName = cityDetails.getString(JSON_CITY_NAME);
        String countryName = cityDetails.getString(JSON_COUNTRY_NAME);
        cityInfo = cityName + ", " + countryName;

        //Get the array of weather forecast for the days setting input
        JSONArray weatherJsonArray = forecastJson.getJSONArray(JSON_DAY);

        Time dayTime = new Time();
        dayTime.setToNow();

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        dayTime = new Time();

        String[] resultStrings = new String[numDays];
        for (int i = 0; i < weatherJsonArray.length(); i++) {
            String day;
            String description;
            String highAndLow;

            //Get the json object for the looped day
            JSONObject dayForecast = weatherJsonArray.getJSONObject(i);

            //dat time is long, but needs to be returned as a readable value
            long dateTime;
            //Cheating to convert the UTC time
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            //Getting description in weather object which is 1 element long
            JSONObject weatherObject = dayForecast.getJSONArray(JSON_WEATHER).getJSONObject(0);
            description = weatherObject.getString(JSON_WEATHER_DETAILS);

            //Getting temperature from temp object
            JSONObject temperatureObject = dayForecast.getJSONObject(JSON_TEMPERATURE_INFO);
            double high = temperatureObject.getDouble(JSON_MAX_TEMPERATURE);
            double low = temperatureObject.getDouble(JSON_MIN_TEMPERATURE);

            //Format the temperatures received into more readable string
            highAndLow = formatHighLows(high, low);

            resultStrings[i] = cityInfo + ": " + day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrings) {
            Log.v(TAG, "Forecast entry: " + s);
        }

        return resultStrings;
    }

}
