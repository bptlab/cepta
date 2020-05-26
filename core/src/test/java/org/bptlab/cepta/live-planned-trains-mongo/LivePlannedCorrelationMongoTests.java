package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Timestamp;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.containers.PostgresContainer;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunctionMongo;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.TrainAttributeValueProvider;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.*;

import static org.bptlab.cepta.providers.MongoDbProvider.insertToDb;
import static org.bptlab.cepta.providers.MongoDbProvider.setupMongoContainer;
import static org.bptlab.cepta.utils.database.Util.ProtoTimestampToSqlTimestamp;

public class LivePlannedCorrelationMongoTests {

    private static MongoConfig mongoConfig;

    @BeforeClass
    public static void initialize() throws Exception {
        mongoConfig = setupMongoContainer();
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

        CollectSink.values.clear();
    }

    @Test
    public void testIdMatch() throws Exception {

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

    @Ignore
    public void testIdUnmatch() throws IOException {
        DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.unmatchingLiveTrainDatas();
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
                .unorderedWait(liveStream, new LivePlannedCorrelationFunctionMongo(mongoConfig),
                        100000, TimeUnit.MILLISECONDS, 1);

        Iterator<Tuple2<LiveTrainData, PlannedTrainData>> iterator = DataStreamUtils.collect(correlatedTrainStream);
        ArrayList<Tuple2<Long, Long>> correlatedIds = new ArrayList<>();
        while (iterator.hasNext()) {
            Tuple2<LiveTrainData, PlannedTrainData> tuple = iterator.next();
            if (tuple.f1 == null) {
                correlatedIds.add(new Tuple2<>(tuple.f0.getTrainSectionId(), null));
            } else {
                correlatedIds.add(new Tuple2<>(tuple.f0.getTrainSectionId(), tuple.f1.getTrainSectionId()));
            }
        }
        Assert.assertTrue(correlatedIds.contains(new Tuple2<>(11111111L, null)));
        Assert.assertTrue(correlatedIds.contains(new Tuple2<>(22222222L, null)));
    }



    private String insertTrainWithTrainSectionIdStationIdQuery(long trainSectionId, long stationId) {
        long millis = 1588068220471l;
        com.google.protobuf.Timestamp timestamp = Timestamp.newBuilder().setSeconds((int)(millis / 1000))
                .setNanos((int) ((millis % 1000) * 1000000)).build();
        String plannedArrivalTimeEndStation = String.format("'%s'", ProtoTimestampToSqlTimestamp(timestamp).toString());
        return String.format(
                "INSERT INTO public.planned(" +
                        "id, " +
                        "train_section_id , " +
                        "station_id , " +
                        "planned_event_time , " +
                        "status , " +
                        "first_train_id , " +
                        "train_id , " +
                        "planned_departure_time_start_station , " +
                        "planned_arrival_time_end_station , " +
                        "ru_id , " +
                        "end_station_id , " +
                        "im_id , " +
                        "following_im_id , " +
                        "message_status , " +
                        "ingestion_time , " +
                        "original_train_id )" +
                        "VALUES (1, %d, %d, current_timestamp, 5, 6, 7, current_timestamp, %s, 10, 1, 12, 13, 14, current_timestamp, 16)", trainSectionId, stationId,plannedArrivalTimeEndStation );
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
