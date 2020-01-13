package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.testng.Assert;
import java.util.TreeSet;
import org.testng.annotations.Test;

public class WeatherLocationCorrelationTests {
  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test(groups = {"integration-tests-db"},
      dataProvider = "weather-at-direct-location",
      dataProviderClass = WeatherDataProvider.class)
  public void testDirectLocationMatch(DataStream<WeatherData> weatherStream) throws IOException {
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = AsyncDataStream
        .unorderedWait(weatherStream, new WeatherLocationCorrelationFunction(postgresConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    correlatedWeatherStream.print();
    Iterator<Tuple2<WeatherData, Integer>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
    ArrayList<Integer> locationIds = new ArrayList<>();
    while(iterator.hasNext()){
      locationIds.add(iterator.next().f1);
    }
    Assert.assertTrue(locationIds.contains(4012656));
  }

  @Test(groups = {"integration-tests-db"},
      dataProvider = "weather-inside-box-location",
      dataProviderClass = WeatherDataProvider.class)
  public void testInsideBoxMatch(DataStream<WeatherData> weatherStream) throws IOException {
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = AsyncDataStream
        .unorderedWait(weatherStream, new WeatherLocationCorrelationFunction(postgresConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    correlatedWeatherStream.print();
    Iterator<Tuple2<WeatherData, Integer>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
    ArrayList<Integer> locationIds = new ArrayList<>();
    while(iterator.hasNext()){
      locationIds.add(iterator.next().f1);
    }
    Assert.assertTrue(locationIds.contains(4012656));
  }

  @Test(groups = {"integration-tests-db"},
      dataProvider = "weather-outside-box-location",
      dataProviderClass = WeatherDataProvider.class)
  public void testOutsideBoxMatch(DataStream<WeatherData> weatherStream) throws IOException {
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = AsyncDataStream
        .unorderedWait(weatherStream, new WeatherLocationCorrelationFunction(postgresConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    correlatedWeatherStream.print();
    Iterator<Tuple2<WeatherData, Integer>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
    while(iterator.hasNext()){
      Assert.assertNotEquals(4012656, iterator.next().f1);
    }
  }

}
