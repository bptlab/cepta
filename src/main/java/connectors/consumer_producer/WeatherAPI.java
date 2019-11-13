package connectors.consumer_producer;

import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class WeatherAPI {

    // make an Open Weather API Call for a specific City
    public static String makeCityApiCall(String city) throws APIException {
        // declaring object of "OWM" class
        OWM owm = new OWM("1f7e1c12a81f5e324869d671958bd985");

        // getting current weather data for the "London" city
        CurrentWeather cwd = owm.currentWeatherByCityName(city);

        return ("City: " + cwd.getCityName() +
                " Temperature: " + cwd.getMainData().getTempMax() + "/" + cwd.getMainData().getTempMin() + "\'K" +
                " Wind" +cwd.getWindData().getSpeed());
    }

    public static String makeRandomAPICall( List<String> cityList) {

        int size = cityList.size() - 1;
        int random = (int )(Math.random()* size + 0);

        String city = cityList.get(random);
        String cleanCity = city.replace("\"" , "");
        System.out.println(cleanCity);
        // declaring object of "OWM" class
        OWM owm = new OWM("1f7e1c12a81f5e324869d671958bd985");

        // getting current weather data for the "London" city
       try {
           CurrentWeather cwd = owm.currentWeatherByCityName(cleanCity);
           return (" City: " + cwd.getCityName() +
                   "\n" + " Temperature: " + cwd.getMainData().getTempMax() + "/" + cwd.getMainData().getTempMin() + "\'K" +
                   "\n" + " Windspeed: " +cwd.getWindData().getSpeed());
       } catch (Exception e) {
           System.out.println("Api error by city:" + cleanCity);
           return "Error";
       }



    }

}
