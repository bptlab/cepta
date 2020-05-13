package org.bptlab.cepta.providers;

import java.util.ArrayList;
import com.google.protobuf.Timestamp;
import static com.google.protobuf.util.Timestamps.*;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;


public class StaysInStationPatternProvider {
    public static DataStream<LiveTrainData> staysInStationSurrounded(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<LiveTrainData> events = new ArrayList<>();
    
        LiveTrainData drivesAtStart = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(5).setStationId(11)
        .setEventTime(fromMillis(1000))
        .build();
    
        events.add(drivesAtStart);
    
        LiveTrainData arrives = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(3).setStationId(12)
        .setEventTime(fromMillis(1050))
        .build();
    
        events.add(arrives);
    
        LiveTrainData departures = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(4).setStationId(12)
        .setEventTime(fromMillis(1100))
        .build();
    
        events.add(departures);
    
        LiveTrainData continuesDriving = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(5).setStationId(14)
        .setEventTime(fromMillis(1150))
        .build();
    
        events.add(continuesDriving);
    
        DataStream<LiveTrainData> mockedStream = env.fromCollection(events)
        .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return toMillis(liveTrainData.getEventTime());
                }
              });
    
        return mockedStream;
    
      }
    
      public static DataStream<LiveTrainData> staysInStationDoubleEvents(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<LiveTrainData> events = new ArrayList<>();
    
        LiveTrainData firstArrival = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(3).setStationId(12)
        .setEventTime(fromMillis(950))
        .build();
    
        events.add(firstArrival);
    
        LiveTrainData secondArrival = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(3).setStationId(12)
        .setEventTime(fromMillis(1000))
        .build();
    
        events.add(secondArrival);
    
        LiveTrainData firstDeparture = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(4).setStationId(12)
        .setEventTime(fromMillis(1050))
        .build();
    
        events.add(firstDeparture);
    
        LiveTrainData secondDeparture = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(4).setStationId(12)
        .setEventTime(fromMillis(1100))
        .build();
    
        events.add(secondDeparture);
    
        DataStream<LiveTrainData> mockedStream = env.fromCollection(events)
        .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return toMillis(liveTrainData.getEventTime());
                }
              });
    
        return mockedStream;
      }
    
      public static DataStream<LiveTrainData> changesStation(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<LiveTrainData> events = new ArrayList<>();
    
        LiveTrainData arrives = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(3).setStationId(12)
        .setEventTime(fromMillis(1000))
        .build();
    
        events.add(arrives);
    
        LiveTrainData changedLocation = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(4).setStationId(13)
        .setEventTime(fromMillis(1050))
        .build();
    
        events.add(changedLocation);
    
        DataStream<LiveTrainData> mockedStream = env.fromCollection(events)
        .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return toMillis(liveTrainData.getEventTime());
                }
              });
    
        return mockedStream;
      }
    
      public static DataStream<LiveTrainData> staysInStationWrongOrder(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<LiveTrainData> events = new ArrayList<>();
    
        LiveTrainData departures = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(4).setStationId(12)
        .setEventTime(fromMillis(1000))
        .build();
    
        events.add(departures);
    
        LiveTrainData arrivesAfterwards = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(3).setStationId(12)
        .setEventTime(fromMillis(1050))
        .build();
    
        events.add(arrivesAfterwards);
    
        DataStream<LiveTrainData> mockedStream = env.fromCollection(events)
        .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return toMillis(liveTrainData.getEventTime());
                }
              });
    
        return mockedStream;
      }
    
    
      public static DataStream<LiveTrainData> staysInStationWithInterruption(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<LiveTrainData> events = new ArrayList<>();
    
        LiveTrainData arrives = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(3).setStationId(12)
        .setEventTime(fromMillis(1000))
        .build();
    
        events.add(arrives);
    
        LiveTrainData otherEvent = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(5).setStationId(13)
        .setTrainId(42)
        .setEventTime(fromMillis(1050))
        .build();
    
        events.add(otherEvent);
    
        LiveTrainData departuresFromAnotherStation = 
        LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
        .setStatus(4).setStationId(12)
        .setEventTime(fromMillis(1100))
        .build();
    
        events.add(departuresFromAnotherStation);
    
        DataStream<LiveTrainData> mockedStream = env.fromCollection(events)
        .assignTimestampsAndWatermarks(
              new AscendingTimestampExtractor<LiveTrainData>() {
                @Override
                public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                  return toMillis(liveTrainData.getEventTime());
                }
              });
    
        return mockedStream;
      }
    
      public static DataStream<LiveTrainData> staysInStationDirectly(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<LiveTrainData> events = new ArrayList<>();
    
        LiveTrainData arrives = 
          LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setStatus(3).setStationId(12)
          .setEventTime(fromMillis(1000))
          .build();
    
        events.add(arrives);
    
        //the second event is 50 Milliseconds later
        LiveTrainData departures = 
          LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
          .setStatus(4).setStationId(12)
          .setEventTime(fromMillis(1050))
          .build();
          
        events.add(departures);
    
        DataStream<LiveTrainData> mockedStream = env.fromCollection(events)
        .assignTimestampsAndWatermarks(
            new AscendingTimestampExtractor<LiveTrainData>() {
              @Override
              public long extractAscendingTimestamp(LiveTrainData liveTrainData) {
                return toMillis(liveTrainData.getEventTime());
              }
            });
        return mockedStream;
    
      }
    
}