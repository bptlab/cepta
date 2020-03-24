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
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.bptlab.cepta.models.events.weather.WeatherDataProtos.WeatherData;

public class PlannedTrainDataProvider {

  public static PlannedTrainData getDefaultPlannedTrainDataEvent() {
    long millis = System.currentTimeMillis();
    Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
         .setNanos((int) ((millis % 1000) * 1000000)).build();
    PlannedTrainData.Builder builder = PlannedTrainData.newBuilder();
    builder.setId(1);
    builder.setTrainId(1);
    builder.setLocationId(1);
    builder.setPlannedTime(timestamp);
    builder.setStatus(1);
    builder.setFirstTrainNumber(1);
    builder.setTrainNumberReference(1);
    builder.setPlannedDepartureReference(timestamp);
    builder.setPlannedArrivalReference(timestamp);
    builder.setTrainOperatorId(1);
    builder.setTransferLocationId(1);
    builder.setReportingImId(1);
    builder.setNextImId(1);
    builder.setMessageStatus(1);
    builder.setMessageCreation(timestamp);
    builder.setOriginalTrainNumber(1);
    return builder.build();
  }

  public static DataStream<PlannedTrainData> plannedTrainDatas(){
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    ArrayList<PlannedTrainData> plannedTrains = new ArrayList<>();

    plannedTrains.add(getDefaultPlannedTrainDataEvent());
    plannedTrains.add(getDefaultPlannedTrainDataEvent());
    DataStream<PlannedTrainData> plannedTrainsStream = env.fromCollection(plannedTrains)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<PlannedTrainData>() {
              @Override
              public long extractAscendingTimestamp(PlannedTrainData plannedTrainData) {
                return plannedTrainData.getMessageCreation().getSeconds();
              }
            });

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    return plannedTrainsStream;
  }

  private static PlannedTrainData trainEventWithLocationID(int locationId){
    return PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent().toBuilder()
        .setLocationId(locationId).build();
  }
  private static PlannedTrainData trainEventWithTrainID(int trainId){
    return PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent().toBuilder()
        .setTrainId(trainId).build();
  }
  private static PlannedTrainData trainEventWithTrainIdLocationId(int trainId, int locationId){
    return PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent().toBuilder()
        .setTrainId(trainId).setLocationId(locationId).build();
  }

  private static Tuple2<WeatherData, Integer> correlatedWeatherEventWithLocationIDClass(int locationId, String eventClass){
    WeatherData weather = WeatherDataProvider.getDefaultWeatherEvent().toBuilder()
      .setEventclass(eventClass).build();
    return new Tuple2<>(weather, locationId);
  }

}
