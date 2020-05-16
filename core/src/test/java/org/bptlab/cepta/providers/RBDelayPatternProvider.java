package org.bptlab.cepta.providers;

import java.util.ArrayList;
import com.google.protobuf.Timestamp;
import static com.google.protobuf.util.Timestamps.*;

import org.bptlab.cepta.containers.ReplayerContainer;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.QueryOptions;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;


public class RBDelayPatternProvider {
    public static void getDataEvent() {
        RedBullExampleProvider.getDataEvent();
        // return
      }

    public static DataStream<Event> receiveEventsFromReplayer(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<Event> events = new ArrayList<>();


      ReplayerContainer container = new ReplayerContainer("bp@localhost", 2222);
      ReplayerProvider provider = new ReplayerProvider(container);

      java.lang.System.out.println("Successful querying");


      QueryOptions options = RedBullExampleProvider.getQueryOptions();

      DataStream<Event> eventStream = provider.query(options);


      java.lang.System.out.println("Successful querying");
      java.lang.System.out.println(provider.query(options));

      return eventStream;
      /*


      // @DataProvider(name = "live-train-data-provider")
    public static DataStream<LiveTrainData> matchingLiveTrainDatas(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);
      ArrayList<LiveTrainData> liveTrains = new ArrayList<>();

      liveTrains.add(trainEventWithTrainSectionIdStationId(42382923, 11111111));
      liveTrains.add(trainEventWithTrainSectionIdStationId(42093766, 11111111));
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