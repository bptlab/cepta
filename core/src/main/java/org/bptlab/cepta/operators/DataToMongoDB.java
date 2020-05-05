package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.config.MongoConfig;

public class DataToMongoDB<T extends Message> implements FlatMapFunction<T,T> {

    private String collection_name;
    private MongoConfig mongoConfig = new MongoConfig();

    public DataToMongoDB(String collection_name, MongoConfig mongoConfig){
        this.collection_name = collection_name;
        this.mongoConfig = mongoConfig;
    }

    @Override
    public void flatMap(T dataset, Collector<T> collector) throws Exception {

    }
}
