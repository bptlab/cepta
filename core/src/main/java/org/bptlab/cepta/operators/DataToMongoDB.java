package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class DataToMongoDB<T extends Message> implements FlatMapFunction<T> {

    private String collection_name;
    private MongoConfig
}
