package org.bptlab.cepta;

import java.util.ArrayList;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.testng.annotations.DataProvider;

public class WeatherDataProvider {

  @DataProvider(name = "weather-at-direct-location")
  public  static Object[][] weatherAtDirectLocation(){
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    WeatherData weather1 = new WeatherData("",49.577, 3.0067, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);

    //System.out.println(weather1);
    DataStream<WeatherData> weatherStream1 = env.fromElements(weather1);

    return new Object[][] { {weatherStream1} };
  }

  @DataProvider(name = "weather-inside-box-location")
  public  static Object[][] weatherInsideBoxLocation(){
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    WeatherData weather1 = new WeatherData("",49.576, 3.0066, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);
    WeatherData weather2 = new WeatherData("",49.578, 3.0068, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);
    WeatherData weather3 = new WeatherData("",49.577, 3.0066, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);
    WeatherData weather4 = new WeatherData("",49.576, 3.0067, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);
    WeatherData weather5 = new WeatherData("",49.5765, 3.00665, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);
    WeatherData weather6 = new WeatherData("",49.5775, 3.00676, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);

    //System.out.println(weather1);
    DataStream<WeatherData> weatherStream1 = env.fromElements(weather1,weather2,weather3,weather4,weather5,weather6);

    return new Object[][] { {weatherStream1} };
  }

  @DataProvider(name = "weather-outside-box-location")
  public static Object[][] weatherOutsideBoxLocation(){
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    ArrayList<WeatherData> testData = new ArrayList<>();
    double baseLat = 49.577;
    double baseLng = 3.0067;
    double boxRadius = 0.02;
    // exacltyLongitudePlus
    testData.add(weatherWithLatLng(baseLat, baseLng + boxRadius));
    // exacltyLatitudePlus
    testData.add(weatherWithLatLng(baseLat + boxRadius, baseLng));
    // exacltyLongitudeMinus
    testData.add(weatherWithLatLng(baseLat, baseLng - boxRadius));
    // exacltyLatitudeMinus
    testData.add(weatherWithLatLng(baseLat - boxRadius, baseLng));
    // exactlyBothPlus
    testData.add(weatherWithLatLng(baseLat + boxRadius, baseLng + boxRadius));
    // exactlyBothMinus
    testData.add(weatherWithLatLng(baseLat - boxRadius, baseLng - boxRadius));
    // farAwayLatitude
    testData.add(weatherWithLatLng(baseLat + boxRadius * 10, baseLng + boxRadius));
    // farAwayLongitude
    testData.add(weatherWithLatLng(baseLat + boxRadius, baseLng + boxRadius * 10));
    // farAwayBoth
    testData.add(weatherWithLatLng(baseLat + boxRadius * 10, baseLng + boxRadius * 10));
    DataStream<WeatherData> weatherStream1 = env.fromCollection(testData);

    return new Object[][] { {weatherStream1} };
  }

  private static WeatherData weatherWithLatLng(double lat, double lng){
    return new WeatherData("",lat, lng, 1l, 1l, 1l, "", "", 1d, 1d, 1d, 1d, "", "", 1d, 1d , 1d, 1, 1d, "", 1d, 1, 1, 1d);
  }

}
