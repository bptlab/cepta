package org.bptlab.cepta;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.bptlab.cepta.utils.functions.StreamUtils;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.WeatherLiveTrainJoinFunction;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.providers.LiveTrainDataProvider;

public class WeatherLiveTrainJoinTests {

  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test
  public void testMatchesOne() throws IOException {
    Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>> input = LiveTrainDataProvider.oneMatchingLiveTrainWeatherData();
    DataStream<LiveTrainData> liveTrainStream = input.getValue0();
    DataStream<Tuple2<WeatherData, Long>> correlatedWeatherStream = input.getValue1();

    DataStream<NotificationOuterClass.Notification> trainDelayNotificationDataStream =
        WeatherLiveTrainJoinFunction.delayFromWeather(correlatedWeatherStream, liveTrainStream);

    int count = StreamUtils.countOfEventsInStream(trainDelayNotificationDataStream);

    Assert.assertEquals(1, count);
  }

  @Test
  public void testMatchesMultiple() throws IOException {
    Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>> input = LiveTrainDataProvider.multipleMatchingLiveTrainWeatherData();
    DataStream<LiveTrainData> liveTrainStream = input.getValue0();
    DataStream<Tuple2<WeatherData, Long>> correlatedWeatherStream = input.getValue1();

    DataStream<NotificationOuterClass.Notification> trainDelayNotificationDataStream =
        WeatherLiveTrainJoinFunction.delayFromWeather(correlatedWeatherStream, liveTrainStream);

    int count = StreamUtils.countOfEventsInStream(trainDelayNotificationDataStream);

    Assert.assertEquals(4, count);
  }

  @Test
  public void testMatchesNone() throws IOException {
    Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>> input = LiveTrainDataProvider.noMatchingLiveTrainWeatherData();
    DataStream<LiveTrainData> liveTrainStream = input.getValue0();
    DataStream<Tuple2<WeatherData, Long>> correlatedWeatherStream = input.getValue1();

    DataStream<NotificationOuterClass.Notification> trainDelayNotificationDataStream =
        WeatherLiveTrainJoinFunction.delayFromWeather(correlatedWeatherStream, liveTrainStream);

    int count = StreamUtils.countOfEventsInStream(trainDelayNotificationDataStream);
    Assert.assertEquals(count, 0);
  }
}