package org.bptlab.cepta.providers;

import java.util.ArrayList;
import com.google.protobuf.Timestamp;
import static com.google.protobuf.util.Timestamps.*;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;


public class RBDelayPatternProvider {
    public static void getDataEvent() {
        RedBullExampleProvider.getDataEvent();
        // return
      }

    //public static DataStream<Event> receiveEventsFromReplayer(){
    public static boolean receiveEventsFromReplayer(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<Event> events = new ArrayList<>();

      getDataEvent();

      return true;
      /*
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
      */
    }
    
}