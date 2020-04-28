package org.bptlab.cepta.providers;

import java.util.ArrayList;
import org.javatuples.Pair;
import com.google.protobuf.Timestamp;
import static com.google.protobuf.util.Timestamps.*;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;



public class LiveTrainDataProvider {

    public static LiveTrainData getDefaultLiveTrainDataEvent() {
      // this represents the timestamp 2020-04-28 10:03:40.0
      // equals the proto timestamp {seconds: 1588068220, nanos: 471000000}
      long millis = 1588068220471l;
      Timestamp timestamp = Timestamp.newBuilder().setSeconds((int)(millis / 1000))
          .setNanos((int) ((millis % 1000) * 1000000)).build();
      LiveTrainData.Builder builder = LiveTrainData.newBuilder();
      builder.setId(1);
      builder.setTrainSectionId(1);
      builder.setStationId(1);
      builder.setEventTime(timestamp);
      builder.setStatus(1);
      builder.setFirstTrainId(1);
      builder.setTrainId(1);
      builder.setPlannedArrivalTimeEndStation(timestamp);
      builder.setDelay(1);
      builder.setEndStationId(1);
      builder.setImId(1);
      builder.setFollowingImId(1);
      builder.setMessageStatus(1);
      builder.setIngestionTime(timestamp);
      return builder.build();
    }

    // @DataProvider(name = "live-train-data-provider")
    public static DataStream<LiveTrainData> matchingLiveTrainDatas(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData> liveTrains = new ArrayList<>();

      liveTrains.add(trainEventWithTrainSectionIdLocationId(42382923, 11111111));
      liveTrains.add(trainEventWithTrainSectionIdLocationId(42093766, 11111111));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(liveTrains)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
                }
              });

      env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
      return liveTrainStream;
    }

    public static DataStream<LiveTrainData> LiveTrainDatStream(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);

      LiveTrainData ele1 = trainEventWithTrainSectionID(2);
      LiveTrainData ele2 = trainEventWithTrainSectionID(3);
      LiveTrainData ele3 = trainEventWithTrainSectionID(4);

      DataStream<LiveTrainData> liveTrainStream = env.fromElements(ele1, ele2, ele3);

      return liveTrainStream;
    }

    public static DataStream<LiveTrainData> liveTrainDatStreamWithDuplicates(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);

      LiveTrainData ele1 = trainEventWithTrainSectionID(5);
      LiveTrainData ele2 = trainEventWithTrainSectionID(2);
      LiveTrainData ele3 = trainEventWithTrainSectionID(2);

      DataStream<LiveTrainData> liveTrainStream = env.fromElements(ele1, ele2, ele3);

      return liveTrainStream;
    }

    // @DataProvider(name = "live-train-data-provider")
    public static DataStream<LiveTrainData> unmatchingLiveTrainDatas(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData> liveTrains = new ArrayList<>();

      liveTrains.add(trainEventWithTrainSectionID(11111111));
      liveTrains.add(trainEventWithTrainSectionID(22222222));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(liveTrains)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
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

      oneMatchingTrain.add(trainEventWithStationID(1));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
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
                return weatherDataIntegerTuple2.f0.getStartTime().getSeconds();
              }
          });
      return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>>(liveTrainStream, weatherStream);
    }

    // @DataProvider(name = "several-matching-live-train-weather-data-provider")
    public static Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>> multipleMatchingLiveTrainWeatherData() {
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData>  matchingTrains = new ArrayList<>();

      matchingTrains.add(trainEventWithStationID(1));
      matchingTrains.add(trainEventWithStationID(2));
      matchingTrains.add(trainEventWithStationID(3));
      matchingTrains.add(trainEventWithStationID(4));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(matchingTrains)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
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
                  return weatherDataIntegerTuple2.f0.getStartTime().getSeconds();
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

      oneMatchingTrain.add(trainEventWithStationID(1));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
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
                  return weatherDataIntegerTuple2.f0.getStartTime().getSeconds();
                }
              });
      return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Integer>>>(liveTrainStream, weatherStream);
    }

    public static LiveTrainData trainEventWithStationID(int locationId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setStationId(locationId).build();
    }
    public static LiveTrainData trainEventWithTrainSectionID(int trainId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainId).build();
    }
    public static LiveTrainData trainEventWithTrainSectionIdEventTime(int trainId, Timestamp eventTime){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainId).setEventTime(eventTime).build();
    }
    public static LiveTrainData trainEventWithTrainSectionIdLocationId(int trainId, int locationId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainId).setStationId(locationId).build();
    }

    public static Tuple2<WeatherData, Integer> correlatedWeatherEventWithLocationIDClass(int locationId, String eventClass){
      WeatherData weather = WeatherDataProvider.getDefaultWeatherEvent().toBuilder()
        .setEventClass(eventClass).build();
      return new Tuple2<>(weather, locationId);
    }
}
