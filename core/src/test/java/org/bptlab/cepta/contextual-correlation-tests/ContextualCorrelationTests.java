package org.bptlab.cepta;

import com.google.protobuf.util.Timestamps;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.*;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass;
import org.bptlab.cepta.operators.ContextualCorrelationFunction;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;

public class ContextualCorrelationTests{

    private StreamExecutionEnvironment setupEnv(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        return env;
    }

    @Test
    public void TestFunctionCallThrowsNoErrors() throws ParseException {
        StreamExecutionEnvironment env = setupEnv();

        LiveTrainData sampleTrain1 =
                LiveTrainData
                        .newBuilder()
                        .setStationId(4019302)
                        .setEventTime(Timestamps.parse("2019-04-28T09:04:00.000Z"))
                        .build();

        MongoConfig mongoConfig = new MongoConfig()
                .withHost("localhost")
                .withPort(27017)
                .withPassword("example")
                .withUser("root")
                .withName("mongodb");

        DataStream<CorrelateableEventOuterClass.CorrelateableEvent> sampleStream =
                env.fromElements(sampleTrain1)
                        .flatMap(new ContextualCorrelationFunction("replay", "eletastations",mongoConfig));

        sampleStream.print();
        Assert.fail();

    }



}