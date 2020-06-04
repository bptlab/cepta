package org.bptlab.cepta.providers;

import java.util.ArrayList;

import org.bptlab.cepta.utils.functions.StreamUtils;
import org.javatuples.Pair;
import com.google.protobuf.Timestamp;
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
        Timestamp timestamp =TimestampProvider.getDefaultTimestamp();
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

      liveTrains.add(trainEventWithTrainSectionIdStationId(TrainAttributeValueProvider.getTrainSectionIdA(), TrainAttributeValueProvider.getStationIdA()));
      liveTrains.add(trainEventWithTrainSectionIdStationId(TrainAttributeValueProvider.getTrainSectionIdB(), TrainAttributeValueProvider.getStationIdA()));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(liveTrains)
          .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

      env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
      return liveTrainStream;
    }

    public static DataStream<LiveTrainData> LiveTrainDatStream(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);

      LiveTrainData ele1 = trainEventWithTrainSectionId(2);
      LiveTrainData ele2 = trainEventWithTrainSectionId(3);
      LiveTrainData ele3 = trainEventWithTrainSectionId(4);

      DataStream<LiveTrainData> liveTrainStream = env.fromElements(ele1, ele2, ele3);

      return liveTrainStream;
    }

    public static DataStream<LiveTrainData> liveTrainDatStreamWithDuplicates(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);

      LiveTrainData ele1 = trainEventWithTrainSectionId(5);
      LiveTrainData ele2 = trainEventWithTrainSectionId(2);
      LiveTrainData ele3 = trainEventWithTrainSectionId(2);

      DataStream<LiveTrainData> liveTrainStream = env.fromElements(ele1, ele2, ele3);

      return liveTrainStream;
    }

    // @DataProvider(name = "live-train-data-provider")
    public static DataStream<LiveTrainData> unmatchingLiveTrainDatas(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData> liveTrains = new ArrayList<>();

      liveTrains.add(trainEventWithTrainSectionId(TrainAttributeValueProvider.getStationIdA()));
      liveTrains.add(trainEventWithTrainSectionId(TrainAttributeValueProvider.getStationIdB()));
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
    public static Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>> oneMatchingLiveTrainWeatherData() {
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData> oneMatchingTrain = new ArrayList<>();

      oneMatchingTrain.add(trainEventWithStationId(1));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
                }
          });
      ArrayList<Tuple2<WeatherData, Long>> weather = new ArrayList<>();
    
    weather.add(correlatedWeatherEventWithStationIdClass(1, "weather1"));
      env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
      DataStream<Tuple2<WeatherData, Long>> weatherStream = env.fromCollection(weather)
          .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<Tuple2<WeatherData, Long>>() {
              @Override
              public long extractAscendingTimestamp(
                  Tuple2<WeatherData, Long> weatherDataIntegerTuple2) {
                return weatherDataIntegerTuple2.f0.getStartTime().getSeconds();
              }
          });
      return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>>(liveTrainStream, weatherStream);
    }

    // @DataProvider(name = "several-matching-live-train-weather-data-provider")
    public static Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>> multipleMatchingLiveTrainWeatherData() {
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData>  matchingTrains = new ArrayList<>();

      matchingTrains.add(trainEventWithStationId(1));
      matchingTrains.add(trainEventWithStationId(2));
      matchingTrains.add(trainEventWithStationId(3));
      matchingTrains.add(trainEventWithStationId(4));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(matchingTrains)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
                }
              });
      ArrayList<Tuple2<WeatherData, Long>> weather = new ArrayList<>();

      weather.add(correlatedWeatherEventWithStationIdClass(1, "Clear_night"));
      weather.add(correlatedWeatherEventWithStationIdClass(2, "Clear_night"));
      weather.add(correlatedWeatherEventWithStationIdClass(3, "Clear_night"));
      weather.add(correlatedWeatherEventWithStationIdClass(4, "Clear_night"));
      env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
      DataStream<Tuple2<WeatherData, Long>> weatherStream = env.fromCollection(weather)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<Tuple2<WeatherData, Long>>() {
                @Override
                public long extractAscendingTimestamp(
                    Tuple2<WeatherData, Long> weatherDataIntegerTuple2) {
                  return weatherDataIntegerTuple2.f0.getStartTime().getSeconds();
                }
              });
      return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>>(liveTrainStream, weatherStream);
      // return new Object[][] { {liveTrainStream, weatherStream} };
    }

    // @DataProvider(name = "not-matching-live-train-weather-data-provider")
    public static Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>> noMatchingLiveTrainWeatherData(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData> oneMatchingTrain = new ArrayList<>();

      oneMatchingTrain.add(trainEventWithStationId(1));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
                }
              });
    ArrayList<Tuple2<WeatherData, Long>> weather = new ArrayList<>();

      weather.add(correlatedWeatherEventWithStationIdClass(2, "weather1"));
      env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    DataStream<Tuple2<WeatherData, Long>> weatherStream = env.fromCollection(weather)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<Tuple2<WeatherData, Long>>() {
                @Override
                public long extractAscendingTimestamp(
                    Tuple2<WeatherData, Long> weatherDataIntegerTuple2) {
                  return weatherDataIntegerTuple2.f0.getStartTime().getSeconds();
                }
              });
      return new Pair<DataStream<LiveTrainData>, DataStream<Tuple2<WeatherData, Long>>>(liveTrainStream, weatherStream);
    }

    public static LiveTrainData trainEventWithEventTime( Timestamp timestamp ){
        return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                .setEventTime( timestamp ).build();
    }

    public static LiveTrainData trainEventWithStationId(int locationId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setStationId(locationId).build();
    }
    public static LiveTrainData trainEventWithTrainSectionId(int trainSectionId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainSectionId).build();
    }
    public static LiveTrainData trainEventWithTrainSectionIdEventTime(int trainSectionId, Timestamp eventTime){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainSectionId).setEventTime(eventTime).build();
    }
    public static LiveTrainData trainEventWithTrainSectionIdStationId(int trainSectionId, int stationId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainSectionId).setStationId(stationId).build();
    }

    public static Tuple2<WeatherData, Long> correlatedWeatherEventWithStationIdClass(int stationId, String eventClass){
      WeatherData weather = WeatherDataProvider.getDefaultWeatherEvent().toBuilder()
        .setEventClass(eventClass).build();
      return new Tuple2<>(weather, (long) stationId);
    }
}
