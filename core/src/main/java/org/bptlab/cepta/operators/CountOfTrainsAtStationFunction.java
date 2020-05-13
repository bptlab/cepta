package org.bptlab.cepta.operators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.bptlab.cepta.models.events.correlatedEvents.CountOfTrainsAtStationEventOuterClass.*;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.apache.flink.util.Collector;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * This class implements a function that takes in an LiveTrainStream and returns a stream of Tuple2<Long, Integer>
 * where each tuple describes a station and the count of trains inside that station in an one hour
 * window.
 *
 * This Window slides every 15 minutes. So each train will potentially be counted multiple times
 * in different windows.
 */
public class CountOfTrainsAtStationFunction {

    private final static Time WINDOW_SIZE = Time.hours(1);
    private final static Time SLIDING_INTERVAL = Time.minutes(15);

    public static DataStream<CountOfTrainsAtStationEvent> countOfTrainsAtStation(DataStream<LiveTrainData> inputStream) {
        DataStream<CountOfTrainsAtStationEvent> resultStream = inputStream
        .keyBy(
            new KeySelector<LiveTrainData, Long>(){
                public Long getKey(LiveTrainData event){
                    return event.getStationId();
                }
            }
        )
        .window(SlidingEventTimeWindows.of(WINDOW_SIZE, SLIDING_INTERVAL))
        .process(
            CountOfTrainsAtStationProcessFunction()
        );
        return resultStream;
    };

    public static ProcessWindowFunction<LiveTrainData, CountOfTrainsAtStationEvent, Long, TimeWindow> CountOfTrainsAtStationProcessFunction() {
        return new ProcessWindowFunction<LiveTrainData, CountOfTrainsAtStationEvent, Long, TimeWindow>() {
            @Override
            public void process(Long key, Context context, Iterable<LiveTrainData> input, Collector<CountOfTrainsAtStationEvent> out) throws Exception {
                int counter = 0;
                for (LiveTrainData i : input) {
                    counter++;
                }
                out.collect(
                        CountOfTrainsAtStationEvent
                                .newBuilder()
                                .setCount(counter)
                                .setStationId(key)
                                .build());
                }
        };
    }
}