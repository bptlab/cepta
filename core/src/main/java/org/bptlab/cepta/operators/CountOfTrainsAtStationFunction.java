package org.bptlab.cepta.operators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.utils.triggers.CustomCountTrigger;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.api.java.tuple.Tuple2;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;

import org.apache.flink.util.Collector;

import org.apache.flink.api.java.functions.KeySelector;

import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

public class CountOfTrainsAtStationFunction {

    public static DataStream<Tuple2<Long, Integer>> countOfTrainsAtStation(DataStream<LiveTrainData> inputStream) {
        DataStream<Tuple2<Long, Integer>> resultStream = inputStream
        .keyBy(
            new KeySelector<LiveTrainData, Long>(){
                public Long getKey(LiveTrainData event){
                    return event.getStationId();
                }
            }
        )
        .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(5)))
        .process(
            CountOfTrainsAtStationProcessFunction()
        );
        return resultStream;
    };

    public static ProcessWindowFunction<LiveTrainData, Tuple2<Long, Integer>, Long, TimeWindow> CountOfTrainsAtStationProcessFunction() {
        return new ProcessWindowFunction<LiveTrainData, Tuple2<Long, Integer>, Long, TimeWindow>() {
            @Override
            public void process(Long key, Context context, Iterable<LiveTrainData> input, Collector<Tuple2<Long, Integer>> out) throws Exception {
                System.out.println(context.currentProcessingTime() + " and the watermarc " + context.currentWatermark());
                System.out.println("HAllO ! :D Ich bin gerade mit " + input.toString() + " beschäftigt.");
                int counter = 0;
                for (Object i : input) {
                    counter++;
                }
                out.collect(new Tuple2<Long, Integer>(key, counter) );
            }
        };
    };

}