package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import javax.xml.crypto.Data;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.WeatherLiveTrainJoinFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.junit.Before;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WeatherLiveTrainJoinTests {

  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test(dataProvider = "one-matching-live-train-weather-data-provider", dataProviderClass = LiveTrainDataProvider.class)
  public void testOneMatching(Object[] input) throws Exception {
    DataStream<LiveTrainData> liveTrainStream = (DataStream<LiveTrainData>) input[0];
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = (DataStream<Tuple2<WeatherData, Integer>>) input[1];

    DataStream<TrainDelayNotification> trainDelayNotificationDataStream =
        WeatherLiveTrainJoinFunction.delayFromWeather(correlatedWeatherStream, liveTrainStream);

    Iterator<TrainDelayNotification> iterator = DataStreamUtils.collect(trainDelayNotificationDataStream);
    int count = 0;
    while(iterator.hasNext()){
      iterator.next();
      count++;
    }
    Assert.assertEquals(count, 1);
  }

  @Test(dataProvider = "several-matching-live-train-weather-data-provider", dataProviderClass = LiveTrainDataProvider.class)
  public void testMoreMatching(Object[] input) throws Exception {
    DataStream<LiveTrainData> liveTrainStream = (DataStream<LiveTrainData>) input[0];
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = (DataStream<Tuple2<WeatherData, Integer>>) input[1];

    DataStream<TrainDelayNotification> trainDelayNotificationDataStream =
        WeatherLiveTrainJoinFunction.delayFromWeather(correlatedWeatherStream, liveTrainStream);

    Iterator<TrainDelayNotification> iterator = DataStreamUtils.collect(trainDelayNotificationDataStream);
    int count = 0;
    while(iterator.hasNext()){
      iterator.next();
      count++;
    }
    Assert.assertEquals(count, 4);
  }

  @Test(dataProvider = "not-matching-live-train-weather-data-provider", dataProviderClass = LiveTrainDataProvider.class)
  public void testNotMatching(Object[] input) throws Exception {
    DataStream<LiveTrainData> liveTrainStream = (DataStream<LiveTrainData>) input[0];
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = (DataStream<Tuple2<WeatherData, Integer>>) input[1];

    DataStream<TrainDelayNotification> trainDelayNotificationDataStream =
        WeatherLiveTrainJoinFunction.delayFromWeather(correlatedWeatherStream, liveTrainStream);

    Iterator<TrainDelayNotification> iterator = DataStreamUtils.collect(trainDelayNotificationDataStream);
    int count = 0;
    while(iterator.hasNext()){
      iterator.next();
      count++;
    }
    Assert.assertEquals(count, 0);
  }
}