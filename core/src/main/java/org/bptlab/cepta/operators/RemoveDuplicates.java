package org.bptlab.cepta.operators;

import java.lang.Object;
import org.apache.flink.api.common.functions.MapFunction;
//import org.apache.flink.api.common.functions.IterativeCondition;
//import org.apache.flink.api.common.functions.Context;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

public class RemoveDuplicates<T extends Object> {
    /*public Pattern<T, ?> duplicates = Pattern.<T>begin("first")
        .followedBy("second").where(
            new IterativeCondition<T>(){
                @Override
                public boolean filter(T event, Context ctx){
                    return event.equals(ctx.getEventForPattern("first"))
                };
            };
        )

*/
    public DataStream<T> removeDuplicates(DataStream<T> stream, int windowSize){
        DataStream<T> resultStream = stream.filter(new FilterFunction<T>() {
            @Override
            public boolean filter(T value) throws Exception {
                // TO DO: detect events which already occured and filter them 
                // TO DO: implement a window size so the filter knows after which time the events can be released
                return true;
            }
          });
        return resultStream;
    }
}