package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.HashMap;
import java.lang.reflect.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;

import org.apache.flink.util.Collector;

import org.bptlab.cepta.models.internal.delay.DelayOuterClass;
import org.bptlab.cepta.utils.triggers.CustomCountTrigger;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;

import org.apache.flink.api.java.functions.KeySelector;

public class SumOfDelayAtStationFunction<T extends Object> {

    /* This function expects a Stream of TrainDelayNotification events and a window size
    and sums up all delay based on the location Id's in the given window size.
    The window is a fixed event number window.
    It will return a Stream of Tuple2 with the location Id and the sum of delay.
    */
    public DataStream<Tuple2<Integer, Long>> SumOfDelayAtStation(DataStream<T> inputStream, int windowSize, String stationAttributName) {
        DataStream<Tuple2<Integer, Long>> resultStream = inputStream
                .keyBy(
                        new KeySelector<T, Integer>(){
                            Integer key = Integer.valueOf(0);
                            public Integer getKey(T event){
                                Integer returnKey = key/windowSize;
                                key++;
                                return returnKey;
                            }
                        }
                )
                .window(GlobalWindows.create()).trigger(
                        CustomCountTrigger.of(windowSize)
                ).process(
                        sumOfDelayAtStationWindowProcessFunction(stationAttributName)
                );
        return resultStream;
    };

    public ProcessWindowFunction<T, Tuple2<Integer, Long>, Integer, GlobalWindow> sumOfDelayAtStationWindowProcessFunction(String stationAttributName) {
        return new ProcessWindowFunction<T, Tuple2<Integer, Long>, Integer, GlobalWindow>() {
            @Override
            public void process(Integer key, Context context, Iterable<T> input, Collector<Tuple2<Integer, Long>> out) throws Exception {
                HashMap<Integer, Long> sums = new HashMap<Integer, Long>();
                for (T in: input) {
                    // This code is very very very bad
                    System.out.println(in);
                    Class c = in.getClass();
                    String methodName = "get" + stationAttributName;
                    Method method = c.getDeclaredMethod(methodName);
                    Integer stationId = Integer.valueOf(method.invoke(in).toString());
                    method = c.getDeclaredMethod("getDelay");
                    DelayOuterClass.Delay delay = (DelayOuterClass.Delay) method.invoke(in);

                    if (!sums.containsKey(stationId)) {
                        sums.put(stationId, delay.getDelta().getSeconds());
                    } else {
                        long tmp;
                        tmp = sums.get(stationId);
                        sums.replace(stationId, (tmp + delay.getDelta().getSeconds()));
                    }
                }

                for (Integer station: sums.keySet()) {
                    long delay = sums.get(station);
                    out.collect(new Tuple2<Integer, Long>(station, delay) );
                }
            }
        };
    }
}