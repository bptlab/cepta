package org.bptlab.cepta.providers;

import java.util.ArrayList;
import com.google.protobuf.Timestamp;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.events.weather.WeatherDataProtos.WeatherData;

public class WeatherDataProvider {

  public static WeatherData getDefaultWeatherEvent() {
    long millis = System.currentTimeMillis();
    Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
         .setNanos((int) ((millis % 1000) * 1000000)).build();
    WeatherData.Builder builder = WeatherData.newBuilder();
    builder.setEventclass("");
    builder.setLatitude(49.577);
    builder.setLongitude(3.0067);
    builder.setStarttimestamp(timestamp);
    builder.setEndtimestamp(timestamp);
    builder.setDetectiontimestamp(timestamp);
    builder.setTitle("");
    builder.setDescription("");
    builder.setTemperature(1d);
    builder.setRain(1d);
    builder.setWindspeed(1d);
    builder.setCloudpercentage(1d);
    builder.setCityname("");
    builder.setIdentifier("");
    builder.setPressure(1d);
    builder.setOzone(1d);
    builder.setHumidity(1d);
    builder.setWindbearing(1);
    builder.setPrecippropability(1d);
    builder.setPreciptype("");
    builder.setDewpoint(1d);
    builder.setNeareststormbearing(1);
    builder.setNeareststormdistance(1);
    builder.setVisibility(1d);
    return builder.build();
  }

  public static DataStream<WeatherData> weatherAtDirectLocationData(){
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);
    WeatherData weather = WeatherDataProvider.getDefaultWeatherEvent();
    DataStream<WeatherData> weatherStream = env.fromElements(weather);
    return weatherStream;
  }

  public  static DataStream<WeatherData> weatherInsideBoxLocationData(){
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    ArrayList<WeatherData> testData = new ArrayList<>();
    testData.add(weatherWithLatLng(49.576, 3.0066));
    testData.add(weatherWithLatLng(49.578, 3.0068));
    testData.add(weatherWithLatLng(49.577, 3.0066));
    testData.add(weatherWithLatLng(49.576, 3.0067));
    testData.add(weatherWithLatLng(49.5765, 3.00665));
    testData.add(weatherWithLatLng(49.5775, 3.00676));
    DataStream<WeatherData> weatherStream = env.fromCollection(testData);
    return weatherStream;
  }

  public static DataStream<WeatherData> weatherOutsideBoxLocationData(){
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
    DataStream<WeatherData> weatherStream = env.fromCollection(testData);
    return weatherStream;
  }

  private static WeatherData weatherWithLatLng(double lat, double lng){
    return WeatherDataProvider.getDefaultWeatherEvent().toBuilder()
      .setLatitude(lat).setLongitude(lng).build();
  }

}
