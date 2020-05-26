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
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static junit.framework.TestCase.*;
import static org.bptlab.cepta.providers.MongoDbProvider.insertToDb;
import static org.bptlab.cepta.providers.MongoDbProvider.setupMongoContainer;
import static org.bptlab.cepta.utils.database.Mongo.protoToBson;

public class DelayShiftFunctionMongoTests {

    @Test
    public void testOneDirectMatch() throws Exception {
        StreamExecutionEnvironment env = setupEnv();
        MongoConfig mongoConfig = setupMongoContainer();

        PlannedTrainData planned = PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent();
        insertToDb(mongoConfig, planned);
        LiveTrainData train = LiveTrainDataProvider.getDefaultLiveTrainDataEvent();

        DataStream<LiveTrainData> inputStream = env.fromElements(train);
        DataStream<Notification> resultStream = AsyncDataStream
                .unorderedWait(inputStream, new DelayShiftFunctionMongo(mongoConfig, 0),
                        100000, TimeUnit.MILLISECONDS, 1);

        ArrayList<Notification> expectedNotifications = new ArrayList<>();
        Notification expectedNotification = NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(train.getTrainSectionId()), 0, "DelayShift from Station: " + train.getStationId(), planned.getStationId());
        expectedNotifications.add(expectedNotification);

        env.execute();
        ArrayList<Notification> notificationArrayList = StreamUtils.collectStreamToArrayList(resultStream);
        assertEquals(expectedNotifications, notificationArrayList);
    }

    @Test
    public void testOnlyFollowingStations() throws Exception {
        StreamExecutionEnvironment env = setupEnv();
        MongoConfig mongoConfig = setupMongoContainer();

        PlannedTrainData planned0 = PlannedTrainDataProvider.trainEventWithStationIdPlannedEventTime(1, Timestamp.newBuilder().setSeconds(100).build());
        PlannedTrainData planned1 = PlannedTrainDataProvider.trainEventWithStationIdPlannedEventTime(2, Timestamp.newBuilder().setSeconds(110).build());
        PlannedTrainData planned2 = PlannedTrainDataProvider.trainEventWithStationIdPlannedEventTime(3, Timestamp.newBuilder().setSeconds(90).build());
        PlannedTrainData planned3 = PlannedTrainDataProvider.trainEventWithStationIdPlannedEventTime(4, Timestamp.newBuilder().setSeconds(120).build());
        insertToDb(mongoConfig, planned0);
        insertToDb(mongoConfig, planned1);
        insertToDb(mongoConfig, planned2);
        insertToDb(mongoConfig, planned3);

        LiveTrainData live = LiveTrainDataProvider.trainEventWithEventTime(Timestamp.newBuilder().setSeconds(100).build());
        DataStream<LiveTrainData> inputStream = env.fromElements(live);

        DataStream<Notification> resultStream = AsyncDataStream
                .unorderedWait(inputStream, new DelayShiftFunctionMongo(mongoConfig, 0),
                        100000, TimeUnit.MILLISECONDS, 1);

        ArrayList<Notification> expectedNotifications = new ArrayList<>();
        expectedNotifications.add(NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(live.getTrainSectionId()),
                0, "DelayShift from Station: " + live.getStationId(), planned3.getStationId()));
        expectedNotifications.add(NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(live.getTrainSectionId()),
                0, "DelayShift from Station: " + live.getStationId(), planned1.getStationId()));
        expectedNotifications.add(NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(live.getTrainSectionId()),
                0, "DelayShift from Station: " + live.getStationId(), planned0.getStationId()));

        env.execute();
        ArrayList<Notification> resultNotifications = StreamUtils.collectStreamToArrayList(resultStream);
        assertEquals(expectedNotifications, resultNotifications);
    }

    private StreamExecutionEnvironment setupEnv() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        return env;
    }

}
