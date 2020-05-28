package org.bptlab.cepta;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.containers.PostgresContainer;
import org.bptlab.cepta.models.events.info.LocationDataOuterClass;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationMongoFunction;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.bptlab.cepta.providers.MongoDbProvider.insertToDb;
import static org.bptlab.cepta.providers.MongoDbProvider.setupMongoContainer;

public class WeatherLocationCorrelationMongoTests {
  private static MongoConfig mongoConfig;
  private static LocationDataOuterClass.LocationData locationPrefab;
  private static StreamExecutionEnvironment env;

  @BeforeClass
  public static void initialize() throws Exception {
    mongoConfig = setupMongoContainer();
    locationPrefab = LocationDataOuterClass.LocationData.newBuilder()
            .setStationId(4012656L)
            .setStationName("fancy name")
            .setCountryCode("country code")
            .setLatitude(49.577)
            .setLongitude(3.0067)
            .build();

    insertToDb(mongoConfig,locationPrefab);
  }

  @Before
  public void setUp(){
    env = StreamExecutionEnvironment.getExecutionEnvironment();

  }

  @Test
  public void testDirectLocationMatch() throws IOException {
    DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherAtDirectLocationData();
    DataStream<Tuple2<WeatherData, Long>> correlatedWeatherStream = AsyncDataStream
        .unorderedWait(weatherStream, new WeatherLocationCorrelationMongoFunction("locationData",mongoConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    correlatedWeatherStream.print();
    Iterator<Tuple2<WeatherData, Long>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
    ArrayList<Long> locationIds = new ArrayList<>();
    while(iterator.hasNext()){
      locationIds.add(iterator.next().f1);
    }
    Assert.assertTrue(locationIds.contains(4012656L));
  }


  @Test
  public void testInsideBoxMatch() throws IOException {
    DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherInsideBoxLocationData();
    DataStream<Tuple2<WeatherData, Long>> correlatedWeatherStream = AsyncDataStream
        .unorderedWait(weatherStream, new WeatherLocationCorrelationMongoFunction("locationData",mongoConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    // correlatedWeatherStream.print();
    Iterator<Tuple2<WeatherData, Long>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
    ArrayList<Long> locationIds = new ArrayList<>();
    while(iterator.hasNext()){
      locationIds.add(iterator.next().f1);
    }
    Assert.assertTrue(locationIds.contains(4012656L));
  }

  @Test
  public void testOutsideBoxMatch() throws IOException {
    System.out.println("Start testOutsideBoxMatch");

    DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherOutsideBoxLocationData();
    DataStream<Tuple2<WeatherData, Long>> correlatedWeatherStream = AsyncDataStream
        .unorderedWait(weatherStream, new WeatherLocationCorrelationMongoFunction("locationData",mongoConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    // correlatedWeatherStream.print();
    Iterator<Tuple2<WeatherData, Long>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
    while(iterator.hasNext()){
      Assert.assertNotEquals(4012656, (long) iterator.next().f1);
    }
  }

}
