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
import org.bptlab.cepta.models.internal.types.ids.Ids;
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
    public void testInitializesCoordinateMappingOfMultipleStations() throws Exception {
        LocationDataOuterClass.LocationData station1 = LocationDataOuterClass.LocationData
                .newBuilder()
                    .setStationId(1)
                    .setLatitude(1)
                    .setLongitude(1)
                .build();
        LocationDataOuterClass.LocationData station2 = LocationDataOuterClass.LocationData
                .newBuilder()
                    .setStationId(2)
                    .setLatitude(2)
                    .setLongitude(2)
                .build();
        LocationDataOuterClass.LocationData station3 = LocationDataOuterClass.LocationData
                .newBuilder()
                    .setStationId(3)
                    .setLatitude(3)
                    .setLongitude(3)
                .build();

        MongoConfig mongoConfig = setupMongoContainer();
        try {
            insertToDb(mongoConfig, station1);
            insertToDb(mongoConfig, station2);
            insertToDb(mongoConfig, station3);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to initialize Db");
        }

        EnrichDelayWithCoordinatesFunction testedFunction = new EnrichDelayWithCoordinatesFunction(mongoConfig);
        testedFunction.open(new Configuration());
        Hashtable<String, CoordinateOuterClass.Coordinate> actualHashtable = testedFunction.getMapping();

        CoordinateOuterClass.Coordinate coordinate1 =  CoordinateOuterClass.Coordinate
                .newBuilder()
                    .setLatitude(1)
                    .setLongitude(1)
                .build();

        CoordinateOuterClass.Coordinate coordinate2 =  CoordinateOuterClass.Coordinate
                .newBuilder()
                    .setLatitude(2)
                    .setLongitude(2)
                .build();

        CoordinateOuterClass.Coordinate coordinate3 =  CoordinateOuterClass.Coordinate
                .newBuilder()
                    .setLatitude(3)
                    .setLongitude(3)
                .build();

        Hashtable<String, CoordinateOuterClass.Coordinate> expectedHastable = new Hashtable<>();
        expectedHastable.put("1", coordinate1);
        expectedHastable.put("2", coordinate2);
        expectedHastable.put("3", coordinate3);

        Assert.assertEquals(expectedHastable, actualHashtable);
    }

    @Test
    public void testInitializesCoordinateMappingOfSingleStation() throws Exception {
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
        EnrichDelayWithCoordinatesFunction testedFunction = new EnrichDelayWithCoordinatesFunction(mongoConfig);
        testedFunction.open(new Configuration());
        Hashtable<String, CoordinateOuterClass.Coordinate> actualHashtable = testedFunction.getMapping();

        CoordinateOuterClass.Coordinate expectedCoordinate =  CoordinateOuterClass.Coordinate
                .newBuilder()
                    .setLatitude(100)
                    .setLongitude(100)
                .build();

        Hashtable<String, CoordinateOuterClass.Coordinate> expectedHastable = new Hashtable<>();
        expectedHastable.put("3", expectedCoordinate);

        Assert.assertEquals(expectedHastable, actualHashtable);
    }

    @Test
    public void testEnrichesNotificationWithCorrectCoordinates() throws IOException {
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

        NotificationOuterClass.Notification unenrichedNotification =
                NotificationHelper.getTrainDelayNotificationFrom("testId", 1, stationId);

        StreamExecutionEnvironment env = setupEnv();
        DataStream<NotificationOuterClass.Notification> enrichedStream = env.fromElements(unenrichedNotification).flatMap(new EnrichDelayWithCoordinatesFunction(mongoConfig));

        ArrayList<NotificationOuterClass.Notification> enrichedStreamCollection = StreamUtils.collectStreamToArrayList(enrichedStream);

        NotificationOuterClass.Notification enrichedNotification = enrichedStreamCollection.get(0);
        CoordinateOuterClass.Coordinate expectedCoordinate = CoordinateOuterClass.Coordinate.newBuilder().setLatitude(latitude).setLongitude(longitude).build();
        Assert.assertEquals(expectedCoordinate, enrichedNotification.getDelay().getCoordinate());


    }

    @Test
    public void testNoFoundStationKeepsTheNotification() throws IOException {
        Long stationId1 = 420L;

        Long stationId2 = 3L;
        Long latitude = 100L;
        Long longitude = 100L;

        MongoConfig mongoConfig = setupMongoContainer();


        LocationDataOuterClass.LocationData testStation = LocationDataOuterClass.LocationData
                .newBuilder()
                    .setStationId(stationId2)
                    .setLatitude(latitude)
                    .setLongitude(longitude)
                .build();

        try {
            insertToDb(mongoConfig, testStation);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to initialize Db");
        }

        NotificationOuterClass.Notification unenrichedNotification =
                NotificationHelper.getTrainDelayNotificationFrom("testId", 1, stationId1);

        StreamExecutionEnvironment env = setupEnv();
        DataStream<NotificationOuterClass.Notification> enrichedStream = env.fromElements(unenrichedNotification).flatMap(new EnrichDelayWithCoordinatesFunction(mongoConfig));


        ArrayList<NotificationOuterClass.Notification> enrichedStreamCollection = StreamUtils.collectStreamToArrayList(enrichedStream);

        NotificationOuterClass.Notification enrichedNotification = enrichedStreamCollection.get(0);


        Assert.assertEquals(unenrichedNotification, enrichedNotification);
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