package org.bptlab.cepta.providers;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.streaming.api.datastream.IterativeStream;

public class JavaDataProvider {

    public static DataStream<Integer> integerDataStream() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<Integer> ResultStream = env.fromElements(1,2,3);
        return ResultStream;
    }

    public static DataStream<Integer> integerDataStreamWithElement(Integer element) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<Integer> ResultStream = env.fromElements(1, 2, 3, element);
        return ResultStream;
    }

    public static DataStream<Long> longDataStream() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<Long> ResultStream = env.generateSequence(0, 1000);
        return ResultStream;
    }
    
    public static DataStream<Double> floatDataStream() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<Double> ResultStream = env.fromElements(0.0, 1.1, 2.2);
        return ResultStream;
    }

    public static DataStream<String> stringDataStream() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<String> ResultStream = env.fromElements("Eins", "Zwei", "Drei");
        return ResultStream;
    }

}