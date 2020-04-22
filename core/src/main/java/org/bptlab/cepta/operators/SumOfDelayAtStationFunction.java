package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.HashMap;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;

import org.bptlab.cepta.models.events.train.TrainDelayNotificationProtos.TrainDelayNotification;

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
    public DataStream<Tuple2<Long, Double>> SumOfDelayAtStation(DataStream<TrainDelayNotification> inputStream, int windowSize) {
        DataStream<Tuple2<Long, Double>> resultStream = inputStream
        .keyBy(
            new KeySelector<TrainDelayNotification, Integer>(){
                Integer key = Integer.valueOf(0);
                public Integer getKey(TrainDelayNotification event){
                    Integer returnKey = key/windowSize;
                    key++;
                    return returnKey;
                } 
            }
        )
        .window(GlobalWindows.create()).trigger(
            CustomCountTrigger.of(windowSize)
        ).process(
            new ProcessWindowFunction<TrainDelayNotification, Tuple2<Long, Double>, Integer, GlobalWindow>() {
                @Override
                public void process(Integer key, Context context, Iterable<TrainDelayNotification> input, Collector<Tuple2<Long, Double>> out) throws Exception {
                    //System.out.println("IN PROCESS");
                    HashMap<Long, Double> sums = new HashMap<Long, Double>();
                    for (TrainDelayNotification in: input) {
                        //System.out.println("IN TRAINDELAY FOR" + in);
                        Long trainId = in.getTrainId();
                        Long locationId = in.getLocationId();
                        Double delay = in.getDelay();
                        if (!sums.containsKey(locationId)) {
                            sums.put(locationId, delay);
                        } else {
                            double tmp;
                            tmp = sums.get(locationId);
                            sums.replace(locationId, (tmp + delay));
                        }
                    }

                    for (Long location: sums.keySet()) {
                      
                        Double delay = sums.get(location);
                        //System.out.println("IN LOCATION: " + location + " DELAY: " + delay);
                        out.collect(new Tuple2<Long, Double>(location, delay) );
                    }
             
                    
                }
            }
        );
        return resultStream;
    }

    // get Stream of DelayNotification

    // have a Window of X Time to sum up
    // key after station id

    // sum up delays at each station

    // when window triggers after time

    // process and create Stream of DelayAtStation events


}