package org.bptlab.cepta;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.operators.*;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.utils.functions.StreamUtils;

import org.bson.Document;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import org.testcontainers.containers.GenericContainer;

import com.google.protobuf.GeneratedMessage;

import sun.awt.image.SunWritableRaster.DataStealer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.*;

public class DataToMongoDBTests {

    @Test
    public void inputAmount() throws IOException {
       /*  StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); */
        GenericContainer mongoContainer = newMongoContainer();
        mongoContainer.start();
        String address = mongoContainer.getContainerIpAddress();
        Integer port = mongoContainer.getFirstMappedPort();
        MongoConfig mongoConfig = new MongoConfig().withHost(address).withPort(port).withPassword("example").withUser("root").withName("mongodb");
  
        DataStream<PlannedTrainData> inputStream = PlannedTrainDataProvider.plannedTrainDatas();
        
        DataStream<PlannedTrainData> resultStream = AsyncDataStream
            .unorderedWait(inputStream, new DataToMongoDB("plannedTrainData", mongoConfig),
                100000, TimeUnit.MILLISECONDS, 1);
        resultStream.print();

        MongoClient mongoClient = Mongo.getMongoClient(mongoConfig);
        MongoDatabase database = mongoClient.getDatabase("plannedTrainData");
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");
        SubscriberHelpers.OperationSubscriber<Document> insertOneSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        plannedTrainDataCollection.find().subscribe(insertOneSubscriber);
        List<Document> docs = insertOneSubscriber.get();
        int count = docs.size();
        int s = StreamUtils.countOfEventsInStream(inputStream);
        Assert.assertEquals(2, count);
    }

    public int getNumberOfDatabaseEntries(){
        return 2;
    }

    public GenericContainer newMongoContainer(){
        return new GenericContainer("mongo")
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");
    }
}
