package org.bptlab.cepta;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.internal.updates.plan.PlanUpdateOuterClass;
import org.bptlab.cepta.serialization.FlinkProtoSerializer;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.junit.Test;

import java.util.ArrayList;

public class AverageSpeedTest {

    // This is a test and will be removed soon
    private PlanUpdateOuterClass.PlanUpdate buildPlanUpdate(int sectionIndex) {
        PlanUpdateOuterClass.PlanUpdate.Builder planUpdate = PlanUpdateOuterClass.PlanUpdate.newBuilder();
        PlanUpdateOuterClass.PlanSectionUpdate.Builder section = PlanUpdateOuterClass.PlanSectionUpdate.newBuilder();
        section.setPlanStationIndex(sectionIndex);
        planUpdate.addSectionUpdates(section.build());
        return planUpdate.build();
    }

    @Test
    public void TestBasicAggregation() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        FlinkProtoSerializer.registerTypes(env, PlanUpdateOuterClass.PlanUpdate.getDefaultInstance());

        DataStream<PlanUpdateOuterClass.PlanUpdate> planUpdates = env.fromElements(
                buildPlanUpdate(1),
                buildPlanUpdate(4),
                buildPlanUpdate(8),
                buildPlanUpdate(9),
                buildPlanUpdate(10)
        );

        ArrayList<PlanUpdateOuterClass.PlanUpdate> test = StreamUtils.collectStreamToArrayList(planUpdates);
        System.out.println("IN: "+test);
    }
}
