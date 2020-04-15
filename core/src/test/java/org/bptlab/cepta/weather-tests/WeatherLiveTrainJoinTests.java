package org.bptlab.cepta;

import java.io.IOException;
import java.util.Iterator;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.WeatherLiveTrainJoinFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass.TrainDelayNotification;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.providers.LiveTrainDataProvider;

public class WeatherLiveTrainJoinTests {

  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test
  public void testMatchesOne() throws IOException {
    Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>> input = LiveTrainDataProvider.oneMatchingLiveTrainWeatherData();
    DataStream<LiveTrainData> liveTrainStream = input.getValue0();
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = input.getValue1();

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

  @Test
  public void testMatchesMultiple() throws IOException {
    Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>> input = LiveTrainDataProvider.multipleMatchingLiveTrainWeatherData();
    DataStream<LiveTrainData> liveTrainStream = input.getValue0();
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = input.getValue1();

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

  @Test
  public void testMatchesNone() throws IOException {
    Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>> input = LiveTrainDataProvider.noMatchingLiveTrainWeatherData();
    DataStream<LiveTrainData> liveTrainStream = input.getValue0();
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = input.getValue1();

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