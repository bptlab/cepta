package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import javax.xml.crypto.Data;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.WeatherLiveTrainJoinFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WeatherLiveTrainJoinTests {

  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test(dataProvider = "one-matching-live-train-weather-data-provider", dataProviderClass = LiveTrainDataProvider.class)
  public void testDirectLocationMatch(Object[] input) throws Exception {
    DataStream<LiveTrainData> liveTrainStream = (DataStream<LiveTrainData>) input[0];
    DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = (DataStream<Tuple2<WeatherData, Integer>>) input[1];
    correlatedWeatherStream.assignTimestampsAndWatermarks(
        new AscendingTimestampExtractor<Tuple2<WeatherData, Integer>>() {
          @Override
          public long extractAscendingTimestamp(
              Tuple2<WeatherData, Integer> weatherDataIntegerTuple2) {
            return weatherDataIntegerTuple2.f0.getStarttimestamp();
          }
        });


    liveTrainStream.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<LiveTrainData>() {
      @Override
      public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
        return liveTrainData.getMessageCreation();
      }
    });

    DataStream<TrainDelayNotification> trainDelayNotificationDataStream =
        WeatherLiveTrainJoinFunction.delayFromWeather(correlatedWeatherStream, liveTrainStream);

    Iterator<TrainDelayNotification> iterator = DataStreamUtils.collect(trainDelayNotificationDataStream);
    int count = 0;
    while(iterator.hasNext()){
      Assert.assertEquals(iterator.next().getDelay(), 9000.0);
      count++;
    }
    Assert.assertEquals(1, count);
  }
}