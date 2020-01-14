package org.bptlab.cepta;

import java.util.ArrayList;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.TimestampAssigner;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.testng.annotations.DataProvider;

public class LiveTrainDataProvider {

  @DataProvider(name = "one-matching-live-train-weather-data-provider")
  public  static Object[][] weatherCorrelationTrainData(){
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData>  oneMatchingTrain = new ArrayList<>();

    oneMatchingTrain.add(trainEventWithLocationID(1));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain);
    ArrayList<Tuple2<WeatherData, Integer>> weather = new ArrayList<>();

    weather.add(correlatedWeatherEventWithLocationIDDescription(1, "weather1"));
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
    DataStream<Tuple2<WeatherData, Integer>> weatherStream = env.fromCollection(weather);
    weatherStream.assignTimestampsAndWatermarks(
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

    return new Object[][] { {liveTrainStream, weatherStream} };
  }

  private static LiveTrainData trainEventWithLocationID(int locationId){
    return new LiveTrainData(1, 1, locationId, 1L, 1, 1, 1, 1L, 1, 1, 1, 1, 1, 1L);
  }

  private static Tuple2<WeatherData, Integer> correlatedWeatherEventWithLocationIDDescription(int locationId, String description){
    return new Tuple2<>(new WeatherData("", 1D, 1D, 1L, 1L, 1L, "", description, 1D, 1D, 1D, 1D, "", "", 1D, 1D, 1D, 1, 1D, "", 1D, 1, 1, 1D), locationId);
  }

}
