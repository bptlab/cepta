package org.bptlab.cepta.providers;

import java.util.ArrayList;
import org.javatuples.Pair;
import com.google.protobuf.Timestamp;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.weather.WeatherDataProtos.WeatherData;

public class LiveTrainDataProvider {

  public static LiveTrainData getDefaultLiveTrainDataEvent() {
    long millis = System.currentTimeMillis();
    Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
         .setNanos((int) ((millis % 1000) * 1000000)).build();
    LiveTrainData.Builder builder = LiveTrainData.newBuilder();
    builder.setId(1);
    builder.setTrainId(1);
    builder.setLocationId(1);
    builder.setActualTime(timestamp);
    builder.setStatus(1);
    builder.setFirstTrainNumber(1);
    builder.setTrainNumberReference(1);
    builder.setArrivalTimeReference(timestamp);
    builder.setPlannedArrivalDeviation(1);
    builder.setTransferLocationId(1);
    builder.setReportingImId(1);
    builder.setNextImId(1);
    builder.setMessageStatus(1);
    builder.setMessageCreation(timestamp);
    return builder.build();
  }

  // @DataProvider(name = "live-train-data-provider")
  public static DataStream<LiveTrainData> matchingLiveTrainDatas(){
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData> liveTrains = new ArrayList<>();

    liveTrains.add(trainEventWithTrainIdLocationId(42382923, 11111111));
    liveTrains.add(trainEventWithTrainIdLocationId(42093766, 11111111));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(liveTrains)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return liveTrainData.getMessageCreation().getSeconds();
              }
            });

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    return liveTrainStream;
  }

  // @DataProvider(name = "live-train-data-provider")
  public static DataStream<LiveTrainData> unmatchingLiveTrainDatas(){
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData> liveTrains = new ArrayList<>();

    liveTrains.add(trainEventWithTrainID(11111111));
    liveTrains.add(trainEventWithTrainID(22222222));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(liveTrains)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return liveTrainData.getMessageCreation().getSeconds();
              }
            });

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    return liveTrainStream;
  }

  // @DataProvider(name = "one-matching-live-train-weather-data-provider")
  public static Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>> oneMatchingLiveTrainWeatherData() {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData> oneMatchingTrain = new ArrayList<>();

    oneMatchingTrain.add(trainEventWithLocationID(1));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return liveTrainData.getMessageCreation().getSeconds();
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
              return weatherDataIntegerTuple2.f0.getStarttimestamp().getSeconds();
            }
        });
    return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>>(liveTrainStream, weatherStream);
  }

  // @DataProvider(name = "several-matching-live-train-weather-data-provider")
  public static Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>> multipleMatchingLiveTrainWeatherData() {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
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
                return liveTrainData.getMessageCreation().getSeconds();
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
                return weatherDataIntegerTuple2.f0.getStarttimestamp().getSeconds();
              }
            });
    return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>>(liveTrainStream, weatherStream);
    // return new Object[][] { {liveTrainStream, weatherStream} };
  }

  // @DataProvider(name = "not-matching-live-train-weather-data-provider")
  public static Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>> noMatchingLiveTrainWeatherData(){
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<LiveTrainData> oneMatchingTrain = new ArrayList<>();

    oneMatchingTrain.add(trainEventWithLocationID(1));
    DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return liveTrainData.getMessageCreation().getSeconds();
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
                return weatherDataIntegerTuple2.f0.getStarttimestamp().getSeconds();
              }
            });
    return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>>(liveTrainStream, weatherStream);
  }

  private static LiveTrainData trainEventWithLocationID(int locationId){
    return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setLocationId(locationId).build();
  }
  private static LiveTrainData trainEventWithTrainID(int trainId){
    return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setTrainId(trainId).build();
  }
  private static LiveTrainData trainEventWithTrainIdLocationId(int trainId, int locationId){
    return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setTrainId(trainId).setLocationId(locationId).build();
  }

  private static Tuple2<WeatherData, Integer> correlatedWeatherEventWithLocationIDClass(int locationId, String eventClass){
    WeatherData weather = WeatherDataProvider.getDefaultWeatherEvent().toBuilder()
      .setEventclass(eventClass).build();
    return new Tuple2<>(weather, locationId);
  }

}
