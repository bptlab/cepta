package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.models.internal.correlatable_event.CorrelateableEvent;

public class ContextualCorrelationFunction extends RichFlatMapFunction<LiveTrainDataOuterClass.LiveTrainData, CorrelateableEvent.CorrelatableEvent> {

    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void flatMap(LiveTrainDataOuterClass.LiveTrainData liveTrainData, Collector<CorrelateableEvent.CorrelatableEvent> collector) throws Exception {

    }
}
