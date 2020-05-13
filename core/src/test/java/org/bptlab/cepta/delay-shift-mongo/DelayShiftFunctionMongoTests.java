package org.bptlab.cepta;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Timestamp;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.InsertOneResult;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import org.apache.flink.test.streaming.runtime.util.*;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass.Notification;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.DelayShiftFunctionMongo;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.notification.NotificationHelper;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.utils.functions.StreamUtils;

import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.junit.*;
import org.testcontainers.containers.GenericContainer;

import java.util.function.Supplier;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static junit.framework.TestCase.*;
import static org.bptlab.cepta.utils.database.Mongo.protoToBson;

public class DelayShiftFunctionMongoTests {

    @Test
    public void testOneDirectMatch() throws Exception {
        StreamExecutionEnvironment env = setupEnv();
        MongoConfig mongoConfig = setupMongoContainer();
        // CollectSink.values.clear();

        PlannedTrainData planned = PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent();
        insertToDb(mongoConfig, planned);
        LiveTrainData train = LiveTrainDataProvider.getDefaultLiveTrainDataEvent();

        DataStream<LiveTrainData> inputStream = env.fromElements(train);
        //inputStream.print();
        DataStream<Notification> resultStream = AsyncDataStream
                .unorderedWait(inputStream, new DelayShiftFunctionMongo(mongoConfig, 0),
                        100000, TimeUnit.MILLISECONDS, 1);

        ArrayList<Notification> expectedNots = new ArrayList<>();
        Notification expectedNotification = NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(train.getTrainSectionId()), 0, "DelayShift from Station: " + train.getStationId(), planned.getStationId());
        expectedNots.add(expectedNotification);
        // resultStream.addSink(new CollectSink());

        env.execute();
        // System.out.println(getDatabaseContent(mongoConfig));
        ArrayList<Notification> nots = StreamUtils.collectStreamToArrayList(resultStream);
        assertEquals(expectedNots, nots);
    }

    @Ignore
    public void testOnlyFollowingStations() throws Exception {
        StreamExecutionEnvironment env = setupEnv();
        MongoConfig mongoConfig = setupMongoContainer();
        CollectSink.values.clear();

        PlannedTrainData planned1 = PlannedTrainDataProvider.trainEventWithStationIdPlannedEventTime(2, Timestamp.newBuilder().setSeconds(90).build());
        PlannedTrainData planned2 = PlannedTrainDataProvider.trainEventWithStationIdPlannedEventTime(3, Timestamp.newBuilder().setSeconds(120).build());
        PlannedTrainData planned3 = PlannedTrainDataProvider.trainEventWithStationIdPlannedEventTime(4, Timestamp.newBuilder().setSeconds(130).build());
        insertToDb(mongoConfig, planned1);
        insertToDb(mongoConfig, planned2);
        insertToDb(mongoConfig, planned3);
        LiveTrainData live = LiveTrainDataProvider.trainEventWithEventTime(Timestamp.newBuilder().setSeconds(100).build());
        DataStream<LiveTrainData> inputStream = env.fromElements(live);

        DataStream<Notification> resultStream = AsyncDataStream
                .unorderedWait(inputStream, new DelayShiftFunctionMongo(mongoConfig),
                        100000, TimeUnit.MILLISECONDS, 1);

        Notification unexpectedNotification = NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(live.getTrainSectionId()),
                0, "DelayShift from Station: " + live.getStationId(), planned1.getStationId());
        Notification expectedNotification1 = NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(live.getTrainSectionId()),
                0, "DelayShift from Station: " + live.getStationId(), planned2.getStationId());
        Notification expectedNotification2 = NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(live.getTrainSectionId()),
                0, "DelayShift from Station: " + live.getStationId(), planned3.getStationId());

        resultStream.addSink(new CollectSink());

        env.execute();
        assertFalse(CollectSink.values.contains(unexpectedNotification));
        assertTrue(CollectSink.values.contains(expectedNotification1));
        assertTrue(CollectSink.values.contains(expectedNotification2));
    }

    private StreamExecutionEnvironment setupEnv() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        CollectSink.values.clear();
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

    public void insertToDb(MongoConfig mongoConfig, PlannedTrainData dataset) throws Exception {
        MongoClient mongoClient = Mongo.getMongoClientSync(mongoConfig);
        //MongoIterable<String> databases = mongoClient.listDatabaseNames();
        MongoDatabase database = mongoClient.getDatabase("mongodb");
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");

        Document document = protoToBson(dataset);

        plannedTrainDataCollection.insertOne(document);
    }

    public List<Document> getDatabaseContent(MongoConfig mongoConfig) throws Exception {
        MongoClient mongoClient = Mongo.getMongoClientSync(mongoConfig);
        MongoDatabase database = mongoClient.getDatabase("mongodb");
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");
        ArrayList<Document> docs = new ArrayList<>();

        for (Document cur : plannedTrainDataCollection.find()) {
            docs.add(cur);
        }
        return docs;
    }

    public GenericContainer newMongoContainer() {
        return new GenericContainer("mongo")
                .withExposedPorts(27017)
                .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");
    }

    // create a testing sink that collects PlannedTrainData values
    private static class CollectSink implements SinkFunction<Notification> {

        // must be static
        public static final List<Notification> values = new ArrayList<>();

        @Override
        public synchronized void invoke(Notification value) throws Exception {
            values.add(value);
        }
    }

    // create a testing sink that collects List<Document> values
    private static class CheckSink implements SinkFunction<List<Document>> {

        // must be static
        public static final List<List<Document>> values = new ArrayList<>();

        @Override
        public synchronized void invoke(List<Document> value) throws Exception {
            values.add(value);
        }
    }

}
