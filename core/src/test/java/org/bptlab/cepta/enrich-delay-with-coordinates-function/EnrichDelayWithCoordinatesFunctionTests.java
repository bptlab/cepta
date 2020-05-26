package org.bptlab.cepta;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.flink.configuration.Configuration;
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
    public void testInitializesCoordinateMapping() throws Exception {
        MongoConfig mongoConfig = setupMongoContainer();

        Long stationId = 3L;
        Long latitude = 100L;
        Long longitude = 100L;

        LocationDataOuterClass.LocationData testStation = LocationDataOuterClass.LocationData
                .newBuilder()
                    .setStationId(stationId)
                    .setLatitude(latitude)
                    .setLongitude(longitude)
                .build();

        try {
            insertToDb(mongoConfig, testStation);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to initialize Db");
        }

        NotificationOuterClass.Notification testNotification = NotificationHelper.getTrainDelayNotificationFrom("1",2,3);

        EnrichDelayWithCoordinatesFunction testedFunction = new EnrichDelayWithCoordinatesFunction(mongoConfig);

        testedFunction.open(new Configuration());

        Hashtable<String, CoordinateOuterClass.Coordinate> actualHashtable = testedFunction.getMapping();
        System.out.println(actualHashtable);

        CoordinateOuterClass.Coordinate expectedCoordinate =  CoordinateOuterClass.Coordinate
                .newBuilder()
                    .setLatitude(100)
                    .setLongitude(100)
                .build();

        Hashtable<String, CoordinateOuterClass.Coordinate> expectedHastable = new Hashtable<String, CoordinateOuterClass.Coordinate>();
        expectedHastable.put("3", expectedCoordinate);

        Assert.assertEquals(expectedHastable, actualHashtable);
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