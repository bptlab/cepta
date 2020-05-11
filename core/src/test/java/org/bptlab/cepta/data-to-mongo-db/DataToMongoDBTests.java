package org.bptlab.cepta;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.mongodb.client.result.InsertOneResult;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.operators.testutils.MockEnvironment;
import org.apache.flink.runtime.operators.testutils.MockEnvironmentBuilder;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.streaming.api.functions.async.AsyncFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.graph.StreamEdge;
import org.apache.flink.streaming.api.operators.async.AsyncWaitOperator;
import org.apache.flink.streaming.api.operators.async.queue.UnorderedStreamElementQueue;

import org.apache.flink.streaming.util.*;
import org.apache.flink.test.streaming.runtime.util.*;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.util.FlinkRuntimeException;
import org.apache.flink.util.function.RunnableWithException;
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

import org.junit.*;
import org.testcontainers.containers.GenericContainer;

import com.google.protobuf.GeneratedMessage;

import sun.awt.image.SunWritableRaster.DataStealer;

import java.sql.*;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertTrue;

public class DataToMongoDBTests {
    private OneInputStreamOperatorTestHarness<PlannedTrainData, PlannedTrainData> testHarness;
    private DataToMongoDB dataToMongoDBFunction;

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(2)
                            .setNumberTaskManagers(1)
                            .build());

    @Before
    public void setupTestHarness() throws Exception {
        MiniClusterWithClientResource resource;

        GenericContainer mongoContainer = newMongoContainer();
        mongoContainer.start();
        String address = mongoContainer.getContainerIpAddress();
        Integer port = mongoContainer.getFirstMappedPort();
        MongoConfig mongoConfig = new MongoConfig().withHost(address).withPort(port).withPassword("example").withUser("root").withName("mongodb");

                TestListResultSink testListResultSink;
        //instantiate user-defined function
        dataToMongoDBFunction = new DataToMongoDB("test",mongoConfig);

        MockEnvironment environment = MockEnvironment.builder().build();
        ExecutionConfig executionConfig = environment.getExecutionConfig();

        ByteArrayOutputStream streamEdgesBytes = new ByteArrayOutputStream();
        ObjectOutputStream oosStreamEdges = new
                ObjectOutputStream(streamEdgesBytes);
        oosStreamEdges.writeObject(Collections.<StreamEdge>emptyList());

        KryoSerializer<PlannedTrainData> kryoSerializer = new KryoSerializer<>(
                PlannedTrainData.class, executionConfig);
        ByteArrayOutputStream kryoSerializerBytes = new ByteArrayOutputStream();
        ObjectOutputStream oosKryoSerializer = new
                ObjectOutputStream(kryoSerializerBytes);
        oosKryoSerializer.writeObject(kryoSerializer);

        Configuration configuration = new Configuration();
        configuration.setBytes("edgesInOrder", streamEdgesBytes.toByteArray());
        configuration.setBytes("typeSerializer_in_1",
                kryoSerializerBytes.toByteArray());


        environment.getTaskConfiguration().addAll(configuration);
//        MailboxExecutorFactory mailboxExecutorFactory = ;
//        MailboxExecutor mailboxExecutor = mailboxExecutorFactory.createExecutor(configuration.getChainIndex());
        // wrap user defined function into a the corresponding operator
        testHarness = new OneInputStreamOperatorTestHarness<PlannedTrainData, PlannedTrainData>(
                new AsyncWaitOperator<PlannedTrainData, PlannedTrainData>(
                        dataToMongoDBFunction,
                        100000,
                        1,
                        AsyncDataStream.OutputMode.UNORDERED
                )
                ,environment );

        // optionally configured the execution environment
        testHarness.getExecutionConfig().setAutoWatermarkInterval(50);

        // open the test harness (will also call open() on RichFunctions)
        testHarness.open();
    }

    @Test
    public void oneUploaded() throws Exception {
        PlannedTrainData train = PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent();
        //push (timestamped) elements into the operator (and hence user defined function)
        synchronized (testHarness.getCheckpointLock()) {
            testHarness.processElement(train, 1);
        }
        //trigger event time timers by advancing the event time of the operator with a watermark
        testHarness.processWatermark(100L);

        //trigger processing time timers by advancing the processing time of the operator directly
        testHarness.setProcessingTime(100L);

        //retrieve list of emitted records for assertions
        Assert.assertEquals(testHarness.getOutput(), train);
    }

    @Test
    public void inputAmount() throws Exception {
         StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        CollectSink.values.clear();

        GenericContainer mongoContainer = newMongoContainer();
        mongoContainer.start();
        String address = mongoContainer.getContainerIpAddress();
        Integer port = mongoContainer.getFirstMappedPort();
        MongoConfig mongoConfig = new MongoConfig().withHost(address).withPort(port).withPassword("example").withUser("root").withName("mongodb");

        TestListResultSink<PlannedTrainData> sink = new TestListResultSink<>();
        PlannedTrainData train = PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent();
        DataStream<PlannedTrainData> inputStream = env.fromElements(train);
        
        DataStream<PlannedTrainData> resultStream = AsyncDataStream
            .unorderedWait(inputStream, new DataToMongoDB("plannedTrainData", mongoConfig),
                100000, TimeUnit.MILLISECONDS, 1);
        resultStream.print();


        DataStream<List<Document>> checkStream = AsyncDataStream
                .unorderedWait(resultStream, new RichAsyncFunction<PlannedTrainData, List<Document>>() {
                            @Override
                            public void asyncInvoke(PlannedTrainData plannedTrainData, ResultFuture<List<Document>> resultFuture) throws Exception {
                                MongoClient mongoClient = Mongo.getMongoClient(mongoConfig);
                                MongoDatabase database = mongoClient.getDatabase("mongodb");
                                MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");

                                SubscriberHelpers.OperationSubscriber<Document> findSubscriber = new SubscriberHelpers.OperationSubscriber<>();

                                plannedTrainDataCollection.find().subscribe(findSubscriber);

                                CompletableFuture<Void> queryFuture = CompletableFuture.supplyAsync(new Supplier<List<Document>>() {
                                    @Override
                                    public List<Document> get() {
//                                        try {
//                                            TimeUnit.SECONDS.sleep(50);
//                                        } catch (InterruptedException e) {
//                                            throw new IllegalStateException();
//                                        }
                                        List<Document> result = findSubscriber.get();
                                        return result;
                                    }
                                }).thenAccept(result ->{
                                    resultFuture.complete(Collections.singletonList(result));
                                    System.out.println(result);
                                });

                            }
                        },100000, TimeUnit.MILLISECONDS, 1);

        resultStream.addSink(new CollectSink());
        checkStream.addSink(new CheckSink());
        checkStream.print();

        env.execute();

        assertTrue(CollectSink.values.contains(train));
        assertTrue(CheckSink.values.size() == 1);
        
////        List<Document> docs = findSubscriber.get();
//
//        int count = result.size();
//        int s = StreamUtils.countOfEventsInStream(inputStream);
//        Assert.assertEquals(2, count);
//        assertTrue(sink.getResult().contains(train));
    }

    // create a testing sink
    private static class CollectSink implements SinkFunction<PlannedTrainData> {

        // must be static
        public static final List<PlannedTrainData> values = new ArrayList<>();

        @Override
        public synchronized void invoke(PlannedTrainData value) throws Exception {
            values.add(value);
        }
    }

    private static class CheckSink implements SinkFunction<List<Document>> {

        // must be static
        public static final List<List<Document>> values = new ArrayList<>();

        @Override
        public synchronized void invoke(List<Document> value) throws Exception {
            values.add(value);
        }
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
