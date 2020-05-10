package org.bptlab.cepta.normalization;

import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass;
import org.bptlab.cepta.models.internal.updates.plan.PlanUpdateOuterClass.PlanSectionUpdate;
import org.bptlab.cepta.models.internal.updates.plan.PlanUpdateOuterClass.PlanUpdate;
import org.bptlab.cepta.models.internal.event.Event.NormalizedEvent;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;

public class PlannedTrainDataNormalizationFunction extends NormalizationFunction {

    @Override
    public void flatMap(Event event, Collector<NormalizedEvent> collector) throws Exception {
        if (!event.hasPlannedTrain()) return;
        PlannedTrainDataOuterClass.PlannedTrainData planEvent = event.getPlannedTrain();
        PlanUpdate.Builder update = PlanUpdate.newBuilder();
        PlanSectionUpdate.Builder sectionUpdate = PlanSectionUpdate.newBuilder();

        sectionUpdate.setPlannedArrivalTimeEndStation(planEvent.getPlannedArrivalTimeEndStation());
        sectionUpdate.setPlannedDepartureTimeStartStation(planEvent.getPlannedDepartureTimeStartStation());

        update.setSectionUpdates(0, sectionUpdate.build());
        collector.collect(NormalizedEvent.newBuilder().setPlanUpdate(update.build()).build());
    }
}
