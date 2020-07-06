package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.*;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.*;
import org.bptlab.cepta.models.internal.types.ids.Ids;

public class LiveTrainToCorrelateable extends RichMapFunction<LiveTrainData, CorrelateableEvent> {

    private StationToCoordinateMap stationToCoordinateMap;

    @Override
    public CorrelateableEvent map(LiveTrainData liveTrainData) throws Exception {
        CorrelateableEvent uncorrelatedEvent =
                CorrelateableEvent.newBuilder()
                        .setCoordinate(stationToCoordinateMap.get(liveTrainData.getStationId()))
                        .setTimestamp(liveTrainData.getEventTime())
                        .setLiveTrain(liveTrainData)
                        .build();
       return uncorrelatedEvent;
    }

    public StationToCoordinateMap getStationToCoordinateMap() {
        return stationToCoordinateMap;
    }

    public LiveTrainToCorrelateable setStationToCoordinateMap(StationToCoordinateMap stationToCoordinateMap) {
        this.stationToCoordinateMap = stationToCoordinateMap;
        return this;
    }
}
