package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.bptlab.cepta.models.events.weather.WeatherDataProtos.WeatherData;

@Ignore ("Integration tests") public class WeatherLocationCorrelationTests {
  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test
  public void testDirectLocationMatch() throws IOException {
    DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherAtDirectLocationData();
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

  @Test
  public void testInsideBoxMatch() throws IOException {
    DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherInsideBoxLocationData();
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

  @Test
  public void testOutsideBoxMatch() throws IOException {
    DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherOutsideBoxLocationData();
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = AsyncDataStream
        .unorderedWait(weatherStream, new WeatherLocationCorrelationFunction(postgresConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    correlatedWeatherStream.print();
    Iterator<Tuple2<WeatherData, Integer>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
    while(iterator.hasNext()){
      Assert.assertNotEquals(4012656, (long) iterator.next().f1);
    }
  }

}
