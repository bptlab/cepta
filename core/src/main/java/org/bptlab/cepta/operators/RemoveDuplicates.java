package org.bptlab.cepta.operators;

import java.lang.Object;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.IterativeCondition;
import org.apache.flink.api.common.functions.Context;
// import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

public class RemoveDuplicates<T> {
    public Pattern<T, ?> duplicates = Pattern.<T>begin("first")
        .followedBy("second").where(
            new IterativeCondition<T>(){
                @Override
                public boolean filter(T event, Context ctx){
                    return event.equals(ctx.getEventForPattern("first"))
                };
            };
        )


    public DataStream<T> removeDuplicates(DataStream<T> stream, int windowSize){
        DataStream<T> resultStream = inputStream.filter(new FilterFunction<T>() {
            @Override
            public boolean filter(T value) throws Exception {
              return (!(value.equals(filter)));
            }
          });
        return resultStream;
    }
}