package org.bptlab.cepta;

import java.util.ArrayList;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.testng.annotations.DataProvider;

public class LiveTrainDataProvider {

  @DataProvider(name = "one-matching-live-train-weather-data-provider")
  public  static Object[][] weatherJoinOneLiveTrain(){
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData>  oneMatchingTrain = new ArrayList<>();

    oneMatchingTrain.add(trainEventWithLocationID(1));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return liveTrainData.getMessageCreation();
              }
        });
    ArrayList<Tuple2<WeatherData, Integer>> weather = new ArrayList<>();

    weather.add(correlatedWeatherEventWithLocationIDClass(1, "weather1"));
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    DataStream<Tuple2<WeatherData, Integer>> weatherStream = env.fromCollection(weather)
        .assignTimestampsAndWatermarks(
          new AscendingTimestampExtractor<Tuple2<WeatherData, Integer>>() {
            @Override
            public long extractAscendingTimestamp(
                Tuple2<WeatherData, Integer> weatherDataIntegerTuple2) {
              return weatherDataIntegerTuple2.f0.getStarttimestamp();
            }
        });
    liveTrainStream.print();
    weatherStream.print();
    return new Object[][] { {liveTrainStream, weatherStream} };
  }

  @DataProvider(name = "several-matching-live-train-weather-data-provider")
  public  static Object[][] weatherJoinSeveralLiveTrain(){
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData>  matchingTrains = new ArrayList<>();

    matchingTrains.add(trainEventWithLocationID(1));
    matchingTrains.add(trainEventWithLocationID(2));
    matchingTrains.add(trainEventWithLocationID(3));
    matchingTrains.add(trainEventWithLocationID(4));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(matchingTrains)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return liveTrainData.getMessageCreation();
              }
            });
    ArrayList<Tuple2<WeatherData, Integer>> weather = new ArrayList<>();

    weather.add(correlatedWeatherEventWithLocationIDClass(1, "Clear_night"));
    weather.add(correlatedWeatherEventWithLocationIDClass(2, "Clear_night"));
    weather.add(correlatedWeatherEventWithLocationIDClass(3, "Clear_night"));
    weather.add(correlatedWeatherEventWithLocationIDClass(4, "Clear_night"));
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    DataStream<Tuple2<WeatherData, Integer>> weatherStream = env.fromCollection(weather)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<Tuple2<WeatherData, Integer>>() {
              @Override
              public long extractAscendingTimestamp(
                  Tuple2<WeatherData, Integer> weatherDataIntegerTuple2) {
                return weatherDataIntegerTuple2.f0.getStarttimestamp();
              }
            });
    return new Object[][] { {liveTrainStream, weatherStream} };
  }

  @DataProvider(name = "not-matching-live-train-weather-data-provider")
  public  static Object[][] weatherJoinNotMatchingLiveTrain(){
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData>  oneMatchingTrain = new ArrayList<>();

    oneMatchingTrain.add(trainEventWithLocationID(1));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return liveTrainData.getMessageCreation();
              }
            });
    ArrayList<Tuple2<WeatherData, Integer>> weather = new ArrayList<>();

    weather.add(correlatedWeatherEventWithLocationIDClass(2, "weather1"));
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    DataStream<Tuple2<WeatherData, Integer>> weatherStream = env.fromCollection(weather)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<Tuple2<WeatherData, Integer>>() {
              @Override
              public long extractAscendingTimestamp(
                  Tuple2<WeatherData, Integer> weatherDataIntegerTuple2) {
                return weatherDataIntegerTuple2.f0.getStarttimestamp();
              }
            });
    liveTrainStream.print();
    weatherStream.print();
    return new Object[][] { {liveTrainStream, weatherStream} };
  }

  private static LiveTrainData trainEventWithLocationID(int locationId){
    return new LiveTrainData(1, 1, locationId, 1L, 1, 1, 1, 1L, 1, 1, 1, 1, 1, 1L);
  }

  private static Tuple2<WeatherData, Integer> correlatedWeatherEventWithLocationIDClass(int locationId, String $class){
    return new Tuple2<>(new WeatherData($class, 1D, 1D, 1L, 1L, 1L, "", "", 1D, 1D, 1D, 1D, "", "", 1D, 1D, 1D, 1, 1D, "", 1D, 1, 1, 1D), locationId);
  }

}
