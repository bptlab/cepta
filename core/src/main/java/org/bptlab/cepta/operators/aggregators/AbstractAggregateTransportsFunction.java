package org.bptlab.cepta.operators.aggregators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.models.internal.transport.TransportOuterClass;

public abstract class AbstractAggregateTransportsFunction {

    public AbstractAggregateTransportsFunction() {

    }

    public abstract DataStream<TransportOuterClass.Transport> aggregate();
}
