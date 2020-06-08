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
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.operators.ContextualCorrelationFunction;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.utils.functions.StreamUtils;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class ContextualCorrelationTests{

    private MongoClient mongoClient = this.getMongoClient();

    private Hashtable<Long, Long> correctCorrelation = new Hashtable<>();

    private StreamExecutionEnvironment setupEnv(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        return env;
    }

    @Test
    public void TestOneGlobalTrainRunGetsCorrelatedTogether() throws ParseException, IOException {
        StreamExecutionEnvironment env = setupEnv();

        MongoConfig mongoConfig = new MongoConfig()
                .withHost("localhost")
                .withPort(27017)
                .withPassword("example")
                .withUser("root")
                .withName("mongodb");

        DataStream<LiveTrainData> liveTrainDataDataStream = env
                .fromCollection(getLiveTrainsOfEuroRailRunId(40510063))
                .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        DataStream<CorrelateableEvent> sampleStream = liveTrainDataDataStream
                        .flatMap(new ContextualCorrelationFunction("replay", "eletastations", mongoConfig));


        Vector<CorrelateableEvent> sampleStreamElements = StreamUtils.collectStreamToVector(sampleStream);
        System.out.println(sampleStreamElements);


        Vector<Ids.CeptaTransportID> allIds = new Vector<>();
        sampleStreamElements.forEach(event -> allIds.add(event.getCeptaId()));
        HashSet<Ids.CeptaTransportID> distinctIds = new HashSet<>(allIds);
        Assert.assertEquals("One Trainrun should be correlated to one ID",1,distinctIds.size());
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

    private void addLiveTrains(Long trainSectionId, Vector<LiveTrainData> outputVector){
        SubscriberHelpers.OperationSubscriber<Document> subscriber = new SubscriberHelpers.OperationSubscriber<Document>();
        this.mongoClient
                .getDatabase("replay")
                .getCollection("livetraindata")
                .find(Filters.eq("trainSectionId", trainSectionId))
                .subscribe(subscriber);
        List<Document> allLiveTrainEvents = subscriber.get();
        allLiveTrainEvents.forEach(document -> outputVector.add(Mongo.documentToLiveTrainData(document)));
    }

    private <T> void makeCollectionDistinct(Collection<T> collection){
        HashSet<T> distinctValues = new HashSet<>(collection);
        collection.clear();
        collection.addAll(distinctValues);
    }

    private Vector<LiveTrainData> getLiveTrainsOfEuroRailRunId(Long euroRailRunId){
        SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();

        MongoDatabase database = mongoClient.getDatabase("replay");
        MongoCollection<Document> collection = database.getCollection("trainsectiondata");
        collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("euroRailRunId", euroRailRunId)),
                Aggregates.project(Document.parse("{trainSectionId:1}"))
            )).subscribe(findMultipleSubscriber);

        List<Document> trainSectionIdsDocuments = findMultipleSubscriber.get();
        Vector<Long> trainSectionIds = new Vector<>();
        trainSectionIdsDocuments.forEach(document -> trainSectionIds.add(document.getLong("trainSectionId")));

        //since we could have multiple entries for each trainRun, we make these values distinct
        makeCollectionDistinct(trainSectionIds);

        //remember which each trainSectionId belongs to this rail run for the evaluation
        trainSectionIds.forEach(id -> this.correctCorrelation.put(id, euroRailRunId));

        Vector<LiveTrainData> allLiveTrainEvents = new Vector<>();
        //now we query for each liveTrain event within these sections
        trainSectionIds.forEach(trainSectionId -> addLiveTrains(trainSectionId, allLiveTrainEvents));

        return allLiveTrainEvents;
    }



}