package org.bptlab.cepta;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.info.LocationDataOuterClass;

import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.operators.EnrichDelayWithCoordinatesFunction;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;
import org.bptlab.cepta.utils.notification.NotificationHelper;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import static org.bptlab.cepta.utils.database.Mongo.protoToBson;
import static org.bptlab.cepta.utils.functions.StreamUtils.collectStreamToArrayList;

public class EnrichDelayWithCoordinatesFunctionTests {

    @Test
    public void testInitializesCoordinateMapping() throws IOException {
        StreamExecutionEnvironment env = setupEnv();
        MongoConfig mongoConfig = setupMongoContainer();

        LocationDataOuterClass.LocationData testStation = LocationDataOuterClass.LocationData.newBuilder().setStationId(3).setLatitude(100).setLongitude(100).build();

        try {
            insertToDb(mongoConfig, testStation);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to initialize Db");
        }

        NotificationOuterClass.Notification testNotification = NotificationHelper.getTrainDelayNotificationFrom("1",2,3);
        DataStream<NotificationOuterClass.Notification> testStream = env.fromElements(testNotification);

//        for (NotificationOuterClass.Notification n : StreamUtils.collectStreamToArrayList(testStream)){
//            System.out.println(n);
//        }

        EnrichDelayWithCoordinatesFunction testedFunction = new EnrichDelayWithCoordinatesFunction(mongoConfig);
        /*
        FIXME: currently the input testedFunction is apparently not the same function we have in the flatMap, so their
                initialisations are different
         */

        DataStream<NotificationOuterClass.Notification> enrichedStream = testStream.flatMap(testedFunction);
        ArrayList<NotificationOuterClass.Notification> enrichedEvents = StreamUtils.collectStreamToArrayList(enrichedStream);
        for (NotificationOuterClass.Notification n :enrichedEvents){
            System.out.println(n);
        }

        String mapString = testedFunction.getMapString();
        System.out.println(mapString);
        CoordinateOuterClass.Coordinate expectedCoordinate =  CoordinateOuterClass.Coordinate.newBuilder().setLatitude(100).setLongitude(100).build();
        Assert.fail();
    }

    public void insertToDb(MongoConfig mongoConfig, LocationDataOuterClass.LocationData dataset) throws Exception {
        MongoClient mongoClient = Mongo.getMongoClientSync(mongoConfig);
        //MongoIterable<String> databases = mongoClient.listDatabaseNames();
        //TODO Fix to mongodb later after Demo
        MongoDatabase database = mongoClient.getDatabase("replay"/*"mongodb"*/);
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("eletastations");

        plannedTrainDataCollection.insertOne(protoToBson(dataset));
    }

    private StreamExecutionEnvironment setupEnv() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        return env;
    }

    private MongoConfig setupMongoContainer() {
        GenericContainer mongoContainer = newMongoContainer();
        mongoContainer.start();
        String address = mongoContainer.getContainerIpAddress();
        Integer port = mongoContainer.getFirstMappedPort();
        MongoConfig mongoConfig = new MongoConfig().withHost(address).withPort(port).withPassword("example").withUser("root").withName("mongodb");

        return mongoConfig;
    }

    public GenericContainer newMongoContainer() {
        return new GenericContainer("mongo")
                .withExposedPorts(27017)
                .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");
    }
}