package org.bptlab.cepta;

import com.google.protobuf.util.Timestamps;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.*;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.*;
import org.bptlab.cepta.operators.ContextualCorrelationFunction;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

public class ContextualCorrelationTests{

    private StreamExecutionEnvironment setupEnv(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        return env;
    }

    @Test
    public void TestFunctionCallThrowsNoErrors() throws ParseException, IOException {
        StreamExecutionEnvironment env = setupEnv();

        LiveTrainData sampleTrain1 =
                LiveTrainData
                        .newBuilder()
                        .setTrainId(1)
                        .setStationId(4019302)
                        .setEventTime(Timestamps.parse("2019-04-28T09:04:00.000Z"))
                        .build();
        LiveTrainData sampleTrain2 =
                LiveTrainData
                        .newBuilder()
                        .setTrainId(2)
                        .setStationId(4012434)
                        .setEventTime(Timestamps.parse("2019-04-28T09:05:00.000Z"))
                        .build();

        MongoConfig mongoConfig = new MongoConfig()
                .withHost("localhost")
                .withPort(27017)
                .withPassword("example")
                .withUser("root")
                .withName("mongodb");

        DataStream<LiveTrainData> liveTrainDataDataStream = env.fromElements(sampleTrain1, sampleTrain2).assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        DataStream<CorrelateableEvent> sampleStream = liveTrainDataDataStream
                        .flatMap(new ContextualCorrelationFunction("replay", "eletastations",mongoConfig));


        Vector<CorrelateableEvent> sampleStreamElements = StreamUtils.collectStreamToVector(sampleStream);
        System.out.println(sampleStreamElements);

        sampleStream.print();
        Assert.assertTrue(true);

    }



}