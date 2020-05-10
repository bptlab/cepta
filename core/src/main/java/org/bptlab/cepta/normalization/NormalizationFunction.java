package org.bptlab.cepta.normalization;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.bptlab.cepta.models.internal.event.Event.NormalizedEvent;

public abstract class NormalizationFunction implements FlatMapFunction<Event, NormalizedEvent> {

    @Override
    public abstract void flatMap(Event event, Collector<NormalizedEvent> collector) throws Exception;
}
