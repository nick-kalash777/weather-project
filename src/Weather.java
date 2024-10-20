import java.net.URI;
import java.net.http.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

//test

public class Weather {
    private static final String accessKey = "2de8313b-f076-472c-ac2b-35306450c8c0";
    private static final String yandexURL = "https://api.weather.yandex.ru/v2/forecast?";
    private static final double latitude = 59.9311;
    private static final double longitude = 30.3609;
    private static final int forecastDays = 5;


    public static void main(String[] args) {
        //try with resources so that HTTP client closes after
        try (HttpClient client = HttpClient.newHttpClient()) {
            //send a request to Yandex Weather API and get the response
            HttpResponse<String> response = client.send(buildWeatherRequest(), HttpResponse.BodyHandlers.ofString());

            String responseString = response.body();

            //format response to JSONObject
            JSONObject weatherJO = parseResponse(responseString);

            //display needed info
            System.out.println(responseString);
            System.out.println("Температура сейчас: " + getCurrentTemperature(weatherJO) + " градусов.");
            System.out.println("Средняя температура на следующие " + forecastDays + " дней: " + getAverageTemperatures(weatherJO) + " градусов.");

        } catch (Exception e) {
            System.out.println("Что-то пошло не так.");
            e.printStackTrace();
        }
    }

    private static HttpRequest buildWeatherRequest () {
        return HttpRequest
                .newBuilder()
                .uri(URI.create(yandexURL + "lat=" + latitude + "&" + "lon=" + longitude + "&" + "limit=" + forecastDays + "&hours=false&extra=false"))
                .header("X-Yandex-Weather-Key", accessKey)
                .GET()
                .build();
    }

    private static JSONObject parseResponse(String responseString) throws ParseException {
        Object obj = new JSONParser().parse(responseString);
        return (JSONObject) obj;
    }

    private static long getCurrentTemperature(JSONObject weather) {
        JSONObject factJO = (JSONObject) weather.get("fact");
        return (long) factJO.get("temp");
    }

    private static double getAverageTemperatures(JSONObject weather) {
        JSONArray forecasts = (JSONArray) weather.get("forecasts");
        double sumOfTemperatures = 0.0;
        for (Object forecastObj : forecasts) {
            sumOfTemperatures += getDailyTemperature(forecastObj);
        }

        return sumOfTemperatures/forecastDays;
    }

    private static double getDailyTemperature(Object forecastObj) {
        JSONObject forecast = (JSONObject) forecastObj;
        JSONObject forecastParts = (JSONObject) forecast.get("parts");
        JSONObject morningSummary = (JSONObject) forecastParts.get("morning");
        long morningTemp = (long) morningSummary.get("temp_avg");
        JSONObject daySummary = (JSONObject) forecastParts.get("day");
        long dayTemp = (long) daySummary.get("temp_avg");
        JSONObject eveningSummary = (JSONObject) forecastParts.get("night");
        long eveningTemp = (long) eveningSummary.get("temp_avg");
        JSONObject nightSummary = (JSONObject) forecastParts.get("night");
        long nightTemp = (long) nightSummary.get("temp_avg");
        return (double) (morningTemp + dayTemp + eveningTemp + nightTemp) /4;
    }

}
