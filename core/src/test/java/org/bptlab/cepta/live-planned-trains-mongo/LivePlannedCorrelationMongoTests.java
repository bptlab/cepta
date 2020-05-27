package org.bptlab.cepta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunctionMongo;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.TimestampProvider;
import org.bptlab.cepta.providers.TrainAttributeValueProvider;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.junit.*;

import static org.bptlab.cepta.providers.MongoDbProvider.insertToDb;
import static org.bptlab.cepta.providers.MongoDbProvider.setupMongoContainer;

public class LivePlannedCorrelationMongoTests {

    private static MongoConfig mongoConfig;
    private static PlannedTrainData plannedPrefab;
    private static LiveTrainData livePrefab;
    private static StreamExecutionEnvironment env;

    @BeforeClass
    public static void initialize() throws Exception {
        mongoConfig = setupMongoContainer();
        plannedPrefab = PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent().toBuilder()
                .setTrainSectionId(TrainAttributeValueProvider.getTrainSectionIdA())
                .setStationId(2)
                .setEndStationId(5)
                .setPlannedArrivalTimeEndStation(TimestampProvider.getDefaultTimestampWithAddedMinutes(60))
                .setIngestionTime(TimestampProvider.getDefaultTimestamp())
                .build();

        livePrefab = LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                .setTrainSectionId(TrainAttributeValueProvider.getTrainSectionIdA())
                .setStationId(2)
                .setEndStationId(5)
                .setPlannedArrivalTimeEndStation(TimestampProvider.getDefaultTimestampWithAddedMinutes(60))
                .build();

        insertToDb(mongoConfig,PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent().toBuilder()
                .setTrainSectionId(TrainAttributeValueProvider.getTrainSectionIdA())
                .setEndStationId(2).build());
        insertToDb(mongoConfig,PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent().toBuilder()
                .setTrainSectionId(TrainAttributeValueProvider.getTrainSectionIdA())
                .setStationId(TrainAttributeValueProvider.getStationIdA())
                .build());
    }

    @Before
    public void setUp() {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        CollectSink.values.clear();
    }

    @Test
    public void oneMatchoneDefault() throws Exception {

        DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.matchingLiveTrainDatas();
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
                .unorderedWait(liveStream, new LivePlannedCorrelationFunctionMongo(mongoConfig),
                        100000, TimeUnit.MILLISECONDS, 1);

        correlatedTrainStream.addSink(new CollectSink());
        liveStream.getExecutionEnvironment().execute();

        List<Tuple2<LiveTrainData, PlannedTrainData>> expected = new ArrayList<>();
        Tuple2<LiveTrainData, PlannedTrainData> expectedMatchA = new Tuple2<LiveTrainData, PlannedTrainData>(
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setTrainSectionId(TrainAttributeValueProvider.getTrainSectionIdA())
                        .setStationId(TrainAttributeValueProvider.getStationIdA())
                        .build()
                ,PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent().toBuilder()
                        .setTrainSectionId(TrainAttributeValueProvider.getTrainSectionIdA())
                        .setStationId(TrainAttributeValueProvider.getStationIdA())
                        .build()
        ) {};
        Tuple2<LiveTrainData, PlannedTrainData> expectedDefault = new Tuple2<LiveTrainData, PlannedTrainData>(
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setTrainSectionId(TrainAttributeValueProvider.getTrainSectionIdB())
                        .setStationId(TrainAttributeValueProvider.getStationIdA())
                        .build()
                , PlannedTrainData.newBuilder().build()
        ) {};
        expected.add(expectedDefault);
        expected.add(expectedMatchA);

        List<Tuple2<LiveTrainData, PlannedTrainData>> streamContent = CollectSink.values;
        Assert.assertTrue(streamContent.containsAll(expected));
        Assert.assertEquals(streamContent.size(),expected.size());
    }

    @Test
    public void useMostRecentPlan() throws Exception {
        PlannedTrainData planned1 = plannedPrefab.toBuilder()
                .setPlannedEventTime(TimestampProvider.getDefaultTimestamp())
                .setIngestionTime(TimestampProvider.getDefaultTimestamp())
                .build();
        PlannedTrainData planned2 = plannedPrefab.toBuilder()
                .setPlannedEventTime(TimestampProvider.getDefaultTimestampWithAddedMinutes(10))
                .setIngestionTime(TimestampProvider.getDefaultTimestampWithAddedMinutes(10))
                .build();

        try {
            insertToDb(mongoConfig,planned1);
            insertToDb(mongoConfig,planned2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LiveTrainData live1 = livePrefab.toBuilder()
                .build();

        DataStream<LiveTrainData> liveTrainStream= env.fromElements(live1)
                .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
                .unorderedWait(liveTrainStream, new LivePlannedCorrelationFunctionMongo(mongoConfig),
                        100000, TimeUnit.MILLISECONDS, 1);

        correlatedTrainStream.addSink(new CollectSink());
        env.execute();

        Tuple2<LiveTrainData, PlannedTrainData> expectedMatch = new Tuple2<LiveTrainData, PlannedTrainData>(
                live1,planned2
        ) {};

        List<Tuple2<LiveTrainData, PlannedTrainData>> streamContent = CollectSink.values;
        Assert.assertTrue(streamContent.size() == 1);
        Assert.assertTrue(streamContent.contains(expectedMatch) );
    }

    // create a testing sink that collects PlannedTrainData values
    private static class CollectSink implements SinkFunction<Tuple2<LiveTrainData, PlannedTrainData>> {

        // must be static
        public static final List<Tuple2<LiveTrainData, PlannedTrainData>> values = new ArrayList<>();

        @Override
        public synchronized void invoke(Tuple2<LiveTrainData, PlannedTrainData> value, SinkFunction.Context context) throws Exception {
            values.add(value);
        }
    }
}
