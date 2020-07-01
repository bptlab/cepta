package org.bptlab.cepta;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
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
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.operators.ContextualCorrelationFunction;
import org.bptlab.cepta.operators.LiveTrainToCorrelateable;
import org.bptlab.cepta.operators.StationToCoordinateMap;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.utils.functions.EuclideanDistanceFunction;
import org.bptlab.cepta.utils.functions.StreamUtils;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.text.ParseException;
import java.util.*;

public class ContextualCorrelationTests{

    @Rule
    public Timeout globalTimeout = Timeout.seconds(Long.MAX_VALUE);

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

        DataStream<LiveTrainData> liveTrainDataDataStream = env
//                .fromCollection(getLiveTrainsOfEuroRailRunId(35770866L))
                .fromCollection(getLiveTrainsOfEuroRailRunId(40510063L))
                .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        StationToCoordinateMap stationToCoordinateMap = new StationToCoordinateMap("replay", "eletastations", this.getMongoConfig());
        LiveTrainToCorrelateable liveTrainToCorrelateable = new LiveTrainToCorrelateable().setStationToCoordinateMap(stationToCoordinateMap);

        DataStream<CorrelateableEvent> enrichedEvents = liveTrainDataDataStream.flatMap(liveTrainToCorrelateable);

        DataStream<CorrelateableEvent> sampleStream = enrichedEvents.flatMap(new ContextualCorrelationFunction());


        Vector<CorrelateableEvent> sampleStreamElements = StreamUtils.collectStreamToVector(sampleStream);
//        System.out.println(sampleStreamElements);


        Vector<Ids.CeptaTransportID> allIds = new Vector<>();
        sampleStreamElements.forEach(event -> allIds.add(event.getCeptaId()));
        HashSet<Ids.CeptaTransportID> distinctIds = new HashSet<>(allIds);
        Assert.assertEquals("One Trainrun should be correlated to one ID",1,distinctIds.size());
    }

    public void testSameAmountOfCorrelatedTrainrunsAsInputTrainruns(Collection<Long> euroRailRunIds) throws IOException {
        StreamExecutionEnvironment env = setupEnv();
        Vector<LiveTrainData> allInputEvents = new Vector<>();
        euroRailRunIds.forEach(id -> allInputEvents.addAll(getLiveTrainsOfEuroRailRunId(id)));
        DataStream<LiveTrainData> inputStream = env.fromCollection(allInputEvents).assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());
        DataStream<CorrelateableEvent> outputStream =
                inputStream
                        .flatMap(new LiveTrainToCorrelateable().setStationToCoordinateMap(new StationToCoordinateMap("replay", "eletastations", this.getMongoConfig())))
                        .flatMap(new ContextualCorrelationFunction());
        Vector<Ids.CeptaTransportID> allIds = new Vector<>();
        StreamUtils.collectStreamToVector(outputStream).forEach(event -> allIds.add(event.getCeptaId()));
        HashSet<Ids.CeptaTransportID> distinctIds = new HashSet<>(allIds);

        Assert.assertEquals(
                "There should be as many correlated runs as the input runs in " + euroRailRunIds,
                euroRailRunIds.size(),
                distinctIds.size());
    }

    @Test
    public void testDifferentAmountsOfTrainRuns() throws IOException {
        testSameAmountOfCorrelatedTrainrunsAsInputTrainruns(Arrays.asList(35770866L));
        testSameAmountOfCorrelatedTrainrunsAsInputTrainruns(Arrays.asList(40510063L));
        testSameAmountOfCorrelatedTrainrunsAsInputTrainruns(Arrays.asList(40510063L, 35770866L));

    }

    @Test
    public void testDontCorrelateIfNotWithingTimewindow() throws ParseException, IOException {
        Timestamp timestamp = Timestamps.parse("1972-01-01T10:00:20.021-05:00");

        ContextualCorrelationFunction testFunction = new ContextualCorrelationFunction();

        CorrelateableEvent event1 = CorrelateableEvent
                .newBuilder()
                .setCoordinate(
                        CoordinateOuterClass.Coordinate
                                .newBuilder()
                                .setLongitude(0)
                                .setLatitude(0)
                                .build())
                .setTimestamp(timestamp)
                .setLiveTrain(LiveTrainData.getDefaultInstance())
                .build();

        // the second event is just a minute before the window closes
        CorrelateableEvent event2 = CorrelateableEvent
                .newBuilder()
                .setCoordinate(
                        CoordinateOuterClass.Coordinate
                                .newBuilder()
                                .setLongitude(0)
                                .setLatitude(0)
                                .build())
                .setTimestamp(Timestamps.add(
                        timestamp,
                        Durations.add(
                                testFunction.getMaxTimespan(),
                                Durations.fromMinutes(1))))
                .setLiveTrain(LiveTrainData.getDefaultInstance())
                .build();
        StreamExecutionEnvironment env = setupEnv();
        DataStream<CorrelateableEvent> inputStream =  env.fromElements(event1, event2);
        DataStream<CorrelateableEvent> outputStream =  inputStream.flatMap(testFunction);

        Vector<Ids.CeptaTransportID> allIds = new Vector<>();
        StreamUtils.collectStreamToVector(outputStream).forEach(event -> allIds.add(event.getCeptaId()));
        HashSet<Ids.CeptaTransportID> distinctIds = new HashSet<>(allIds);

        Assert.assertEquals(
                "Two events in the same timewindow should be correlated together",
                2,
                distinctIds.size());

    }

    @Test
    public void testCorrelateEventsIfWithinTimeWindow() throws ParseException, IOException {
        Timestamp timestamp = Timestamps.parse("1972-01-01T10:00:20.021-05:00");

        ContextualCorrelationFunction testFunction = new ContextualCorrelationFunction();

        CorrelateableEvent event1 = CorrelateableEvent
                .newBuilder()
                .setCoordinate(
                        CoordinateOuterClass.Coordinate
                                .newBuilder()
                                .setLongitude(0)
                                .setLatitude(0)
                                .build())
                .setTimestamp(timestamp)
                .setLiveTrain(LiveTrainData.getDefaultInstance())
                .build();

        // the second event is just a minute before the window closes
        CorrelateableEvent event2 = CorrelateableEvent
                .newBuilder()
                .setCoordinate(
                        CoordinateOuterClass.Coordinate
                                .newBuilder()
                                .setLongitude(0)
                                .setLatitude(0)
                                .build())
                .setTimestamp(Timestamps.add(
                        timestamp,
                        Durations.subtract(
                                testFunction.getMaxTimespan(),
                                Durations.fromMinutes(1))))
                .setLiveTrain(LiveTrainData.getDefaultInstance())
                .build();
        StreamExecutionEnvironment env = setupEnv();
        DataStream<CorrelateableEvent> inputStream =  env.fromElements(event1, event2);
        DataStream<CorrelateableEvent> outputStream =  inputStream.flatMap(testFunction);

        Vector<Ids.CeptaTransportID> allIds = new Vector<>();
        StreamUtils.collectStreamToVector(outputStream).forEach(event -> allIds.add(event.getCeptaId()));
        HashSet<Ids.CeptaTransportID> distinctIds = new HashSet<>(allIds);

        Assert.assertEquals(
                "Two events in the same timewindow should be correlated together",
                1,
                distinctIds.size());

    }

    @Test
    public void testStraightAngleDirection(){
        CorrelateableEvent start = this.eventWithCoordinates(0,0);
        CorrelateableEvent middle = this.eventWithCoordinates(0,1);
        CorrelateableEvent end = this.eventWithCoordinates(0,2);
        Assert.assertEquals("Events on a straight line should have a straight angle",
                Math.PI, new EuclideanDistanceFunction().angleBetween(start,middle,end), 0.1);
    }

    @Test
    public void testRightAngleDirection(){
        CorrelateableEvent start = this.eventWithCoordinates(0,0);
        CorrelateableEvent middle = this.eventWithCoordinates(0,1);
        CorrelateableEvent end = this.eventWithCoordinates(1,1);
        Assert.assertEquals("Events around a right-angle corner should have a right angle",
                Math.PI / 2, new EuclideanDistanceFunction().angleBetween(start,middle,end), 0.1);
    }

    @Test
    public void test0RadiansAngle(){
        CorrelateableEvent start = this.eventWithCoordinates(0,0);
        CorrelateableEvent middle = this.eventWithCoordinates(0,1);
        CorrelateableEvent end = this.eventWithCoordinates(0,0);
        Assert.assertEquals("Events from A -> B -> A should have an angle of 0 radians",
                0, new EuclideanDistanceFunction().angleBetween(start,middle,end), 0.1);
    }

    private CorrelateableEvent eventWithCoordinates(long latitude, long longitude){
        return CorrelateableEvent
                .newBuilder()
                .setCoordinate(
                        CoordinateOuterClass.Coordinate
                                .newBuilder()
                                .setLongitude(longitude)
                                .setLatitude(latitude)
                                .build())
                .build();
    }

    @Test
    public void testCorrelationAccuracyAcrossDifferentSamples() throws Exception {
        //first we need to retrieve our testdata
        SubscriberHelpers.OperationSubscriber<Document> subscriber = new SubscriberHelpers.OperationSubscriber<>();

        MongoDatabase database = mongoClient.getDatabase("replay");
        MongoCollection<Document> collection = database.getCollection("testing_data_for_correlation");
        collection.find().subscribe(subscriber);
        List<Document> result = subscriber.get();
        Vector<Vector<LiveTrainData>> allTrainruns= new Vector<>();
        Hashtable<Long, Long> correctCorrelation = new Hashtable<>();

        /*
        this is really not nice :<
        each testSample has a trainrunId (entry.get("_id")), an array of sectionIds
        and an array of
         */
        result.forEach(entry -> {
            Vector<LiveTrainData> aTrainrun = new Vector<>();
            //this takes all liveTrains for this run and converts the documents into live-events
            ((ArrayList<Document>) entry.get("matchedLiveTrains")).forEach(liveTrainDocument-> {
                aTrainrun.add(Mongo.documentToLiveTrainData(liveTrainDocument));
            });
            allTrainruns.add(aTrainrun);

            ((ArrayList<Long>) entry.get("sectionIds")).forEach(sectionId -> {
                correctCorrelation.put(sectionId, (Long) entry.get("_id"));
            });
        });

//        int[] testSizes = new int[] {1,5,10, 20, 50 , 100};
        int[] testSizes = new int[] {500};
//        Long[] distanceWindows = new Long[] {50};

        //time windows are in minutes
//        int[] timeWindows = new int[] {720};
        int repeatCount = 1;

        //we set the same mapping to all conversions, that way we only have to load the stations once
        StationToCoordinateMap map = new StationToCoordinateMap("replay", "eletastations", this.getMongoConfig());
        LiveTrainToCorrelateable conversion = new LiveTrainToCorrelateable().setStationToCoordinateMap(map);

        File dataFile = new File("/tmp/dataFile.csv");
        System.out.println(dataFile.getAbsolutePath());
        dataFile.createNewFile();
        System.out.println(dataFile.exists());
        System.out.println(dataFile.canWrite());
        PrintWriter printWriter = new PrintWriter(dataFile, "UTF-8");

//        for (Long distanceWindow : distanceWindows){
//            for (int timeWindow : timeWindows){
                for (double timeWeight = 0; timeWeight <= 1; timeWeight += 0.25) {
                    for (double distanceWeight = 0.0; distanceWeight <= 1; distanceWeight += 0.25) {
                        for (double directionWeight = 0.0; directionWeight <= 1; directionWeight += 0.25) {
                            for (int i = 0; i < repeatCount; i++) {
                                for (int testSize : testSizes) {
                                    Vector<LiveTrainData> testData = new Vector<>();
                                    Collections.shuffle(allTrainruns);
                                    //add testSize-many trainruns
                                    int nextRandomTrainRun = 0;
                                    for (int trainRun = 0; trainRun < testSize; trainRun++){
                                        if (nextRandomTrainRun > allTrainruns.size()){
                                            Collections.shuffle(allTrainruns);
                                            nextRandomTrainRun = 0;
                                        }
                                        testData.addAll(allTrainruns.get(nextRandomTrainRun));
                                        nextRandomTrainRun++;
                                    }

                                    EuclideanDistanceFunction distanceFunction = new EuclideanDistanceFunction()
                                            .setDirectionWeight(directionWeight)
                                            .setDistanceWeight(distanceWeight)
                                            .setTimeWeight(timeWeight);

                                    ContextualCorrelationFunction correlationFunctionWithWeights = new ContextualCorrelationFunction()
                                            .setDistanceFunction(distanceFunction);
//                                            .setMaxDistance(distanceWindow)
//                                            .setMaxTimespan(Durations.fromMinutes(timeWindow));

                                    /*
                                    this is a workaround for the currentEvents being stored in a class variable,
                                    we just clear the variable before every run. kinda sucks but eh
                                     */
                                    correlationFunctionWithWeights.clearData();

                                    testData.sort(Comparator.comparingLong(a -> a.getEventTime().getSeconds()));
                                    StreamExecutionEnvironment env = setupEnv();
                                    DataStream<CorrelateableEvent> testStream =
                                            env.fromCollection(testData)
                                                    //this part takes a pretty long time which is fairly unfortunate :(
                                                    .flatMap(conversion)
                                                    .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor())
                                                    .flatMap(correlationFunctionWithWeights);

                                    //now we want to check how accurate the correlation was
                                    Vector<CorrelateableEvent> correlatedEvents = StreamUtils.collectStreamToVector(testStream);

                                    int countOfWronglyCorrelated = 0;

                                    for (CorrelateableEvent correlatedEvent : correlatedEvents) {
                                        /*
                                            for our testing purposes, each ID is the trainSectionID "@" the time it occured
                                            that way we have different IDs when a train start is detected
                                         */
                                        Long correlatedTrainRun = correctCorrelation.get(Long.valueOf(correlatedEvent.getCeptaId().getId().split("@")[0]));
                                        Long actualCorrectTrainRun = correctCorrelation.get(correlatedEvent.getLiveTrain().getTrainSectionId());

                                        if (!correlatedTrainRun.equals(actualCorrectTrainRun)){
                                            countOfWronglyCorrelated++;
                                        }
                                    }

                                    Vector<Ids.CeptaTransportID> allIds = new Vector<>();
                                    correlatedEvents.forEach(event -> allIds.add(event.getCeptaId()));
                                    HashSet<Ids.CeptaTransportID> distinctIds = new HashSet<>(allIds);

                                    printWriter.println(String.format("%f;%f;%f;%d;%d,%d,%d", timeWeight, distanceWeight, directionWeight, testSize, testData.size(), distinctIds.size(), countOfWronglyCorrelated));
                                    System.out.println(String.format("%f;%f;%f;%d;%d,%d,%d", timeWeight, distanceWeight, directionWeight, testSize, testData.size(), distinctIds.size(), countOfWronglyCorrelated));
                                }
                                printWriter.flush();
                            }
                        }
                    }
                }
//            }
//        }

//        printWriter.close();
    }

    private MongoConfig getMongoConfig(){
        return new MongoConfig()
                .withHost("localhost")
                .withPort(27017)
                .withPassword("example")
                .withUser("root")
                .withName("mongodb");
    }

    private MongoClient getMongoClient(){
        return Mongo.getMongoClient(this.getMongoConfig());
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
//                ,Aggregates.sort(Document.parse("{eventTime:1}"))
            )).subscribe(findMultipleSubscriber);

        List<Document> trainSectionIdsDocuments = findMultipleSubscriber.get();
        Vector<Long> trainSectionIds = new Vector<>();
        trainSectionIdsDocuments.forEach(document -> trainSectionIds.add(document.getLong("trainSectionId")));

        //since we could have multiple entries for each trainRun, we make these values distinct
        makeCollectionDistinct(trainSectionIds);

        //remember which each trainSectionId belongs to this rail run for the evaluation
        //remember which each trainSectionId belongs to this rail run for the evaluation
        trainSectionIds.forEach(id -> this.correctCorrelation.put(id, euroRailRunId));

        Vector<LiveTrainData> allLiveTrainEvents = new Vector<>();
        //now we query for each liveTrain event within these sections
        trainSectionIds.forEach(trainSectionId -> addLiveTrains(trainSectionId, allLiveTrainEvents));
        allLiveTrainEvents.sort(Comparator.comparingLong(a -> a.getEventTime().getSeconds()));
        return allLiveTrainEvents;
    }



}