package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.HashMap;
import java.lang.reflect.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;

import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass.TrainDelayNotification;

import org.apache.flink.util.Collector;

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
    public DataStream<Tuple2<Integer, Double>> SumOfDelayAtStation(DataStream<T> inputStream, int windowSize, String stationAttributName) {
        DataStream<Tuple2<Integer, Double>> resultStream = inputStream
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

    public ProcessWindowFunction<T, Tuple2<Integer, Double>, Integer, GlobalWindow> sumOfDelayAtStationWindowProcessFunction(String stationAttributName) {
        return new ProcessWindowFunction<T, Tuple2<Integer, Double>, Integer, GlobalWindow>() {
            @Override
            public void process(Integer key, Context context, Iterable<T> input, Collector<Tuple2<Integer, Double>> out) throws Exception {
                HashMap<Integer, Double> sums = new HashMap<Integer, Double>();
                for (T in: input) {
                    Class c = in.getClass();
                    String methodName = "get" + stationAttributName;
                    Method method = c.getDeclaredMethod(methodName);
                    Integer stationId = Integer.valueOf(method.invoke(in).toString());
                    method = c.getDeclaredMethod("getDelay");
                    Double delay = Double.valueOf(method.invoke(in).toString());

                    if (!sums.containsKey(stationId)) {
                        sums.put(stationId, delay);
                    } else {
                        double tmp;
                        tmp = sums.get(stationId);
                        sums.replace(stationId, (tmp + delay));
                    }
                }

                for (Integer station: sums.keySet()) {
                    Double delay = sums.get(station);
                    out.collect(new Tuple2<Integer, Double>(station, delay) );
                }
            }
        };
    }
}