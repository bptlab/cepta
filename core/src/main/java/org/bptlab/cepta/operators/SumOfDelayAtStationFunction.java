package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.HashMap;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;

import org.apache.flink.util.Collector;

import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.utils.triggers.CustomCountTrigger;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;

import org.apache.flink.api.java.functions.KeySelector;

import javax.management.Notification;

public class SumOfDelayAtStationFunction {

    /* This function expects a Stream of TrainDelayNotification events and a window size
    and sums up all delay based on the location Id's in the given window size.
    The window is a fixed event number window.
    It will return a Stream of Tuple2 with the location Id and the sum of delay. 
    */
    public DataStream<Tuple2<String, Long>> SumOfDelayAtStation(DataStream<NotificationOuterClass.Notification> inputStream, int windowSize) {
        DataStream<Tuple2<String, Long>> resultStream = inputStream
        .keyBy(
            new KeySelector<NotificationOuterClass.Notification, Integer>(){
                Integer key = Integer.valueOf(0);
                public Integer getKey(NotificationOuterClass.Notification event){
                    Integer returnKey = key/windowSize;
                    key++;
                    return returnKey;
                } 
            }
        )
        .window(GlobalWindows.create()).trigger(
            CustomCountTrigger.of(windowSize)
        ).process(
            sumOfDelayAtStationWindowProcessFunction()
        );
        return resultStream;
    }; 

    public static ProcessWindowFunction<NotificationOuterClass.Notification, Tuple2<String, Long>, Integer, GlobalWindow> sumOfDelayAtStationWindowProcessFunction() {
        return new ProcessWindowFunction<NotificationOuterClass.Notification, Tuple2<String, Long>, Integer, GlobalWindow>() {
            @Override
            public void process(Integer key, Context context, Iterable<NotificationOuterClass.Notification> input, Collector<Tuple2<String, Long>> out) throws Exception {
                HashMap<String, Long> sums = new HashMap<String, Long>();
                for (NotificationOuterClass.Notification in: input) {
                    String trainId = in.getDelay().getCeptaId().getId();
                    String locationId = in.getDelay().getStationId().getId();
                    long delay = in.getDelay().getDelay().getDelta().getSeconds();
                    if (!sums.containsKey(locationId)) {
                        sums.put(locationId, delay);
                    } else {
                        long tmp;
                        tmp = sums.get(locationId);
                        sums.replace(locationId, (tmp + delay));
                    }
                }

                for (String location: sums.keySet()) {
                  
                    long delay = sums.get(location);
                    out.collect(new Tuple2<String, Long>(location, delay) );
                }
            }
        };
    }
}