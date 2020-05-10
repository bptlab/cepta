package org.bptlab.cepta.normalization;

import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.models.internal.event.Event.NormalizedEvent;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.models.internal.updates.live.LiveUpdateOuterClass.LiveUpdate;
import org.bptlab.cepta.utils.IDGenerator;

public class LiveTrainDataNormalizationFunction extends NormalizationFunction {

    @Override
    public void flatMap(Event event, Collector<NormalizedEvent> collector) throws Exception {
        if (!event.hasLiveTrain()) return;
        LiveTrainDataOuterClass.LiveTrainData liveEvent = event.getLiveTrain();
        LiveUpdate.Builder update = LiveUpdate.newBuilder();
        update.setTransportCeptaId(
                Ids.CeptaTransportID.newBuilder()
                        .setId(IDGenerator.hashed(liveEvent.getTrainId()))
                        .build());
        collector.collect(NormalizedEvent.newBuilder().setLiveUpdate(update.build()).build());
    }
}
