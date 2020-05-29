package org.bptlab.cepta.operators;

import java.util.HashMap;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;

import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;

import org.apache.flink.util.Collector;

import org.bptlab.cepta.utils.triggers.CustomCountTrigger;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;

import org.apache.flink.api.java.functions.KeySelector;

public class SumOfDelayAtStationFunction {

    /* This function expects a Stream of TrainDelayNotification events and a window size
    and sums up all delay based on the location Id's in the given window size.
    The window is a fixed event number window.
    It will return a Stream of Tuple2 with the location Id and the sum of delay.
    */
    public static DataStream<Tuple2<Long, Long>> sumOfDelayAtStation(DataStream<NotificationOuterClass.Notification> inputStream, int windowSize) {
        DataStream<Tuple2<Long, Long>> resultStream = inputStream
                .keyBy(
                        new KeySelector<NotificationOuterClass.Notification, Integer>(){
                            Integer key = 0;
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

    public static ProcessWindowFunction<NotificationOuterClass.Notification, Tuple2<Long, Long>, Integer, GlobalWindow> sumOfDelayAtStationWindowProcessFunction() {
        return new ProcessWindowFunction<NotificationOuterClass.Notification, Tuple2<Long, Long>, Integer, GlobalWindow>() {
            @Override
            public void process(Integer key, Context context, Iterable<NotificationOuterClass.Notification> input, Collector<Tuple2<Long, Long>> out) throws Exception {
                //TODO CHANGE this to use Flink State eg. MapState
                HashMap<Long, Long> sums = new HashMap<Long, Long>();
                for (NotificationOuterClass.Notification outer_in: input) {
                    NotificationOuterClass.DelayNotification in = outer_in.getDelay();
                    Long trainId = Long.valueOf(in.getTransportId().getId());
                    Long locationId = Long.valueOf(in.getStationId().getId());
                    Long delay = in.getDelay().getDelta().getSeconds();
                    if (!sums.containsKey(locationId)) {
                        sums.put(locationId, delay);
                    } else {
                        Long tmp;
                        tmp = sums.get(locationId);
                        sums.replace(locationId, (tmp + delay));
                    }
                }

                for (Long location: sums.keySet()) {

                    Long delay = sums.get(location);
                    out.collect(new Tuple2<Long, Long>(location, delay) );
                }
            }
        };
    }
}