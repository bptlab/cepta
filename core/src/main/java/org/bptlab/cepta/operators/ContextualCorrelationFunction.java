package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.models.internal.correlatable_event.CorrelateableEventOuterClass;

public class ContextualCorrelationFunction extends RichFlatMapFunction<LiveTrainDataOuterClass.LiveTrainData, CorrelateableEventOuterClass.CorrelateableEvent {

    private StationToCoordinateMap stationToCoordinateMap;

    ContextualCorrelationFunction(String datebaseName, String tableName, MongoConfig mongoConfig) {
        this.stationToCoordinateMap = new StationToCoordinateMap(datebaseName, tableName, mongoConfig);
    }

    public StationToCoordinateMap getMapping(){ return stationToCoordinateMap;}

    @Override
    public void flatMap(LiveTrainDataOuterClass.LiveTrainData liveTrainData, Collector<CorrelateableEventOuterClass.CorrelateableEvent> collector) throws Exception {


    }
}
