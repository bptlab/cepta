package org.bptlab.cepta;

import com.google.protobuf.util.Timestamps;
import com.mongodb.client.model.Aggregates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.*;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.*;
import org.bptlab.cepta.operators.ContextualCorrelationFunction;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.plaf.basic.BasicListUI;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
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
                        .setTrainSectionId(42)
                        .build();
        LiveTrainData sampleTrain2 =
                LiveTrainData
                        .newBuilder()
                        .setTrainId(2)
                        .setStationId(4012434)
                        .setEventTime(Timestamps.parse("2019-04-28T09:05:00.000Z"))
                        .setTrainSectionId(80085)
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

    private MongoClient getMongoClient(){
        return Mongo.getMongoClient(
                new MongoConfig()
                        .withHost("localhost")
                        .withPort(27017)
                        .withPassword("example")
                        .withUser("root")
                        .withName("mongodb")
        );
    }

    private Vector<LiveTrainData> getLiveTrainsOfEuroRailRunId(int euroRailRunId){
        MongoClient mongoClient = this.getMongoClient();
        SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();

        MongoDatabase database = mongoClient.getDatabase("replay");
        MongoCollection<Document> collection = database.getCollection("trainsectiondata");
        collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("euroRailRunId", euroRailRunId)),
                Aggregates.project(Document.parse("{euroRailRunId:1}"))
            )).subscribe(findMultipleSubscriber);
        return null;
    }


}