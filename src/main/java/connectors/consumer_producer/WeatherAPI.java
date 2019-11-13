package connectors.consumer_producer;

import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

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

    public static String makeRandomAPICall() throws APIException, FileNotFoundException {

        String city = "";
        // declaring object of "OWM" class
        OWM owm = new OWM("1f7e1c12a81f5e324869d671958bd985");

        // getting current weather data for the "London" city
        CurrentWeather cwd = owm.currentWeatherByCityName("");

        return ("City: " + cwd.getCityName() +
                " Temperature: " + cwd.getMainData().getTempMax() + "/" + cwd.getMainData().getTempMin() + "\'K" +
                " Wind" +cwd.getWindData().getSpeed());
    }

}
