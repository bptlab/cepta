package org.bptlab.cepta;

import com.google.protobuf.Message;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.operators.KeyedProcessOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.api.operators.OutputTypeConfigurable;
import org.apache.flink.streaming.api.transformations.OneInputTransformation;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.runtime.operators.windowing.TimeWindowTest;
import org.apache.flink.streaming.runtime.operators.windowing.WindowOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.AbstractStreamOperatorTestHarness;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.internal.cargo.CargoOuterClass;
import org.bptlab.cepta.models.internal.modalities.air.Air;
import org.bptlab.cepta.models.internal.modalities.maritime.Maritime;
import org.bptlab.cepta.models.internal.modalities.rail.Rail;
import org.bptlab.cepta.models.internal.modalities.road.Road;
import org.bptlab.cepta.models.internal.station.StationOuterClass;
import org.bptlab.cepta.models.internal.transport.TransportOuterClass;
import org.bptlab.cepta.models.internal.types.carrier.CarrierOuterClass;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.models.internal.types.country.CountryOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.models.internal.types.station.Station;
import org.bptlab.cepta.models.internal.types.transport.Transport;
import org.bptlab.cepta.models.internal.updates.plan.PlanUpdateOuterClass;
import org.bptlab.cepta.operators.aggregators.AggregatePlanUpdatesToTransport;
import org.bptlab.cepta.serialization.KryoProtoSerializer;
import org.bptlab.cepta.serialization.NonLazyKryoProtoSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
// import com.twitter.chill.protobuf.ProtobufSerializer;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

// Reference: https://github.com/knaufk/flink-testing-pyramid/blob/master/src/test/java/com/github/knaufk/testing/java/udfs/EventTimeWindowCounterHarnessTest.java

public class AggregatePlanUpdatesToTransportTest {

    private PlanUpdateOuterClass.PlanUpdate buildPlanUpdate(int sectionIndex) {
        PlanUpdateOuterClass.PlanUpdate.Builder planUpdate = PlanUpdateOuterClass.PlanUpdate.newBuilder();
        PlanUpdateOuterClass.PlanSectionUpdate.Builder section = PlanUpdateOuterClass.PlanSectionUpdate.newBuilder();

        section.setPlanStationIndex(sectionIndex);
        // TODO: Add more

        planUpdate.addSectionUpdates(section.build());
        return planUpdate.build();
    }

    // TODO / Test ideas: test with event time: test ordering of section based on index or time; also test the indices are correctly infered

    // private KeyedOneInputStreamOperatorTestHarness<Ids.CeptaTransportID, PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport> testHarness;
    private AggregatePlanUpdatesToTransport aggregateFunction;
    private StreamExecutionEnvironment env;

    public KeyedOneInputStreamOperatorTestHarness<Ids.CeptaTransportID, PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport> setupTestHarnessForSource(DataStream<PlanUpdateOuterClass.PlanUpdate> planUpdates) throws Exception {
        SingleOutputStreamOperator<TransportOuterClass.Transport> transportsStream = planUpdates
                .keyBy(PlanUpdateOuterClass.PlanUpdate::getTransportCeptaId)
                .window(TumblingEventTimeWindows.of(Time.seconds(1)))
                .aggregate(new AggregatePlanUpdatesToTransport());

        final OneInputTransformation<PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport> transform =
                (OneInputTransformation<PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport>) transportsStream.getTransformation();

        final OneInputStreamOperator<PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport> operator = transform.getOperator();
        WindowOperator<Ids.CeptaTransportID, PlanUpdateOuterClass.PlanUpdate, ?, TransportOuterClass.Transport, ?> winOperator = (WindowOperator<Ids.CeptaTransportID, PlanUpdateOuterClass.PlanUpdate, ?, TransportOuterClass.Transport, ?>) operator;

        KeyedOneInputStreamOperatorTestHarness<Ids.CeptaTransportID, PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(
                        operator,
                        winOperator.getKeySelector(),
                        TypeInformation.of(Ids.CeptaTransportID.class));

        testHarness.open();
        return testHarness;
    }

    @Before
    public void setup() throws ClassNotFoundException {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().registerTypeWithKryoSerializer(TransportOuterClass.Transport.class, KryoProtoSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(StationOuterClass.Station.class, KryoProtoSerializer.class);
        /*
        env.getConfig().registerTypeWithKryoSerializer(PlanUpdateOuterClass.PlanUpdate.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(TransportOuterClass.Transport.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(StationOuterClass.Station.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Ids.CeptaTransportID.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Ids.CeptaStationID.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(CargoOuterClass.Cargo.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Transport.TransportType.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(CarrierOuterClass.Carrier.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(CountryOuterClass.Country.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(CoordinateOuterClass.Coordinate.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Station.InfrastructureProvider.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Rail.RailTransport.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Road.RoadTransport.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Maritime.MaritimeTransport.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Air.AirTransport.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Rail.RailStation.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Road.RoadStation.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Maritime.MaritimeStation.class, ProtobufSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Air.AirStation.class, ProtobufSerializer.class);
        */

        // See: https://stackoverflow.com/questions/54291615/flink-consuming-s3-parquet-file-kyro-serialization-error
        // See: https://stackoverflow.com/questions/46818293/how-to-set-unmodifiable-collection-serializer-of-kryo-in-spark-code/46838306
        // See: https://github.com/jerrinot/subzero/issues/8
        // See: https://stackoverflow.com/questions/32453030/using-an-collectionsunmodifiablecollection-with-apache-flink
        // env.getConfig().registerTypeWithKryoSerializer(TransportOuterClass.Transport.class, NonLazyKryoProtoSerializer.class);
        Class<?> unmodColl = Class.forName("java.util.Collections$UnmodifiableCollection");
        env.getConfig().addDefaultKryoSerializer(unmodColl, UnmodifiableCollectionsSerializer.class);
        // env.getConfig().registerTypeWithKryoSerializer(Message.class, NonLazyKryoProtoSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Message.class, KryoProtoSerializer.class);
        // env.getConfig().registerTypeWithKryoSerializer(Collections.EMPTY_LIST.getClass(), CollectionsEmptyListSerializer.class);
        // env.getConfig().registerTypeWithKryoSerializer(java.util.Collections.emptyList().getClass(), CollectionsEmptyListSerializer.class);
        env.getConfig().registerTypeWithKryoSerializer(Arrays.asList( "" ).getClass(), ArraysAsListSerializer.class );
        // env.getConfig().regiser (Collections.unmodifiableList(new ArrayList<>()).getClass(), UnmodifiableCollectionsSerializer.class);
        // java.util.Collections.unmodifiableList(stations_)
        // env.getConfig().addDefaultKryoSerializer(Message.class, NonLazyKryoProtoSerializer.class);
        // env.getConfig().registerTypeWithKryoSerializer(Message.class, ProtobufSerializer.class);
        /*
        FlinkProtoSerializer.registerTypes(
                env,
                PlanUpdateOuterClass.PlanUpdate.getDefaultInstance(),
                TransportOuterClass.Transport.getDefaultInstance(),
                StationOuterClass.Station.getDefaultInstance(),
                Ids.CeptaTransportID.getDefaultInstance());
         */
    }

    @Test
    public void TestBasicAggregation() throws Exception {
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        DataStream<PlanUpdateOuterClass.PlanUpdate> planUpdates = env.fromElements(
                buildPlanUpdate(1),
                buildPlanUpdate(4),
                buildPlanUpdate(8),
                buildPlanUpdate(9),
                buildPlanUpdate(10)
        );

        // ArrayList<PlanUpdateOuterClass.PlanUpdate> in = StreamUtils.collectStreamToArrayList(planUpdates);


        KeyedOneInputStreamOperatorTestHarness<Ids.CeptaTransportID, PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport> testHarness = setupTestHarnessForSource(planUpdates);

        testHarness.getExecutionConfig().registerTypeWithKryoSerializer(TransportOuterClass.Transport.class, KryoProtoSerializer.class);
        testHarness.getExecutionConfig().registerTypeWithKryoSerializer(StationOuterClass.Station.class, KryoProtoSerializer.class);

        // ArrayList<PlanUpdateOuterClass.PlanUpdate> transports = StreamUtils.collectStreamToArrayList(planUpdates);
        testHarness.processWatermark(Long.MIN_VALUE);
        testHarness.processElement(buildPlanUpdate(1), 300L);
        testHarness.processElement(buildPlanUpdate(2), 999L);
        testHarness.processWatermark(Long.MAX_VALUE);

        System.out.println("IN: " + null + " OUT: " + testHarness.extractOutputStreamRecords());
        /* Assert.assertFalse(
                "cleansed Stream should not contain Integer.MIN_VALUE",
                cleansedInteger.contains(Integer.MIN_VALUE));
         */
    }

    // See: https://github.com/apache/flink/blob/master/flink-streaming-java/src/test/java/org/apache/flink/streaming/runtime/operators/windowing/WindowTranslationTest.java#L1610
    private static <K, IN, OUT> void processElementAndEnsureOutput(
            OneInputStreamOperator<IN, OUT> operator,
            KeySelector<IN, K> keySelector,
            TypeInformation<K> keyType,
            IN element) throws Exception {

        KeyedOneInputStreamOperatorTestHarness<K, IN, OUT> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(
                        operator,
                        keySelector,
                        keyType);

        if (operator instanceof OutputTypeConfigurable) {
            // use a dummy type since window functions just need the ExecutionConfig
            // this is also only needed for Fold, which we're getting rid off soon.
            ((OutputTypeConfigurable) operator).setOutputType(BasicTypeInfo.STRING_TYPE_INFO, new ExecutionConfig());
        }

        testHarness.open();

        testHarness.setProcessingTime(0);
        testHarness.processWatermark(Long.MIN_VALUE);

        testHarness.processElement(new StreamRecord<>(element, 0));

        // provoke any processing-time/event-time triggers
        testHarness.setProcessingTime(Long.MAX_VALUE);
        testHarness.processWatermark(Long.MAX_VALUE);

        // we at least get the two watermarks and should also see an output element
        Assert.assertTrue(testHarness.getOutput().size() >= 3);

        testHarness.close();
    }


}
