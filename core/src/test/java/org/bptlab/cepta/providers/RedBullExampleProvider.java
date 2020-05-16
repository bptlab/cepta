package org.bptlab.cepta.providers;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.javatuples.Pair;
import com.google.protobuf.Timestamp;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.bptlab.cepta.providers.ReplayerProvider;
import org.bptlab.cepta.containers.ReplayerContainer;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.QueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.SourceQuery;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.SourceQueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.Timerange;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;

//import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
//import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;

/*
This provider is supposed to replay one specific trainId from our DB datasets (RedBull example).
The trainId for the following example is: trainID 49054 on 2019-09-05 but this can be changed in no time.
*/


public class RedBullExampleProvider {

    // RedBull example
    public static int trainID = 49054;
    public static String dateString = "2019-09-05";

    // Build query for the Replayer
    public static QueryOptions getQueryOptions(){
      // this represents the timestamp 2019-09-05 00:00:00.0
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String dateInString = dateString;
      long epochMillis = 0l;

      try{
        Date date = sdf.parse(dateInString);
        epochMillis = date.getTime();
      } catch (ParseException e) {
        // System.out.println(e)
      }

      Timestamp timestampStart = Timestamp.newBuilder().setSeconds((int)(epochMillis / 1000))
          .setNanos((int) ((epochMillis % 1000) * 1000000)).build();
      // Wait 1 week till end (604800 seconds = 60 * 60 * 24 * 7)
      Timestamp timestampEnd = Timestamp.newBuilder().setSeconds((int)(epochMillis / 1000) + 604800)
          .setNanos((int) ((epochMillis % 1000) * 1000000)).build();

      Timerange.Builder timerangeBuilder = Timerange.newBuilder();
      timerangeBuilder.setStart(timestampStart);
      timerangeBuilder.setEnd(timestampEnd);

      SourceQueryOptions.Builder sourceQueryOptionsBuilder = SourceQueryOptions.newBuilder();
      sourceQueryOptionsBuilder.setTimerange(timerangeBuilder.build());
      sourceQueryOptionsBuilder.setLimit(1);
      sourceQueryOptionsBuilder.setOffset(1);

      SourceQuery.Builder sourceQueryBuilder = SourceQuery.newBuilder();
      sourceQueryBuilder.setSource(Topic.CHECKPOINT_DATA);
      sourceQueryBuilder.addIds(Integer.toString(trainID));
      sourceQueryBuilder.setOptions(sourceQueryOptionsBuilder.build());

      QueryOptions.Builder optionsBuilder = QueryOptions.newBuilder();
      optionsBuilder.addSources(sourceQueryBuilder.build());
      optionsBuilder.setSort(true);

      return optionsBuilder.build();
    }


/*


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

      liveTrains.add(trainEventWithTrainSectionId(11111111));
      liveTrains.add(trainEventWithTrainSectionId(22222222));
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

      oneMatchingTrain.add(trainEventWithStationId(1));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
                }
          });
      ArrayList<Tuple2<WeatherData, Integer>> weather = new ArrayList<>();

    weather.add(correlatedWeatherEventWithStationIdClass(1, "weather1"));
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
      ArrayList<Tuple2<WeatherData, Integer>> weather = new ArrayList<>();

      weather.add(correlatedWeatherEventWithStationIdClass(1, "Clear_night"));
      weather.add(correlatedWeatherEventWithStationIdClass(2, "Clear_night"));
      weather.add(correlatedWeatherEventWithStationIdClass(3, "Clear_night"));
      weather.add(correlatedWeatherEventWithStationIdClass(4, "Clear_night"));
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

      oneMatchingTrain.add(trainEventWithStationId(1));
      DataStream<LiveTrainData> liveTrainStream= env.fromCollection(oneMatchingTrain)
          .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return liveTrainData.getIngestionTime().getSeconds();
                }
              });
    ArrayList<Tuple2<WeatherData, Integer>> weather = new ArrayList<>();

      weather.add(correlatedWeatherEventWithStationIdClass(2, "weather1"));
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

    public static LiveTrainData trainEventWithEventTime( Timestamp timestamp ){
        return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                .setEventTime( timestamp ).build();
    }

    public static LiveTrainData trainEventWithStationId(int locationId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setStationId(locationId).build();
    }
    public static LiveTrainData trainEventWithTrainSectionId(int trainId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainId).build();
    }
    public static LiveTrainData trainEventWithTrainSectionIdEventTime(int trainId, Timestamp eventTime){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainId).setEventTime(eventTime).build();
    }
    public static LiveTrainData trainEventWithTrainSectionIdStationId(int trainId, int stationId){
      return LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setTrainSectionId(trainId).setStationId(stationId).build();
    }

    public static Tuple2<WeatherData, Integer> correlatedWeatherEventWithStationIdClass(int stationId, String eventClass){
      WeatherData weather = WeatherDataProvider.getDefaultWeatherEvent().toBuilder()
        .setEventClass(eventClass).build();
      return new Tuple2<>(weather, stationId);
    }
    */
}
