package org.bptlab.cepta.operators.aggregators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.internal.event.Event;
import org.bptlab.cepta.models.internal.transport.TransportOuterClass;

public abstract class AggregateTransportsFunction extends AbstractAggregateTransportsFunction {

    protected DataStream<Event.NormalizedEvent> liveStream;
    protected DataStream<Event.NormalizedEvent> planStream;
    protected DataStream<Event.NormalizedEvent> globalDataStream;
    protected StreamExecutionEnvironment env;

    public abstract static class Builder<T extends AggregateTransportsFunction> {
        protected DataStream<Event.NormalizedEvent> liveStream;
        protected DataStream<Event.NormalizedEvent> planStream;
        protected DataStream<Event.NormalizedEvent> globalDataStream;
        protected StreamExecutionEnvironment env;

        public Builder() {}

        public AggregateTransportsFunction.Builder setLiveUpdates(DataStream<Event.NormalizedEvent> liveStream) {
            this.liveStream = liveStream;
            return this;
        }

        public AggregateTransportsFunction.Builder setPlanUpdates(DataStream<Event.NormalizedEvent> planStream) {
            this.planStream = planStream;
            return this;
        }

        public AggregateTransportsFunction.Builder setGlobalData(DataStream<Event.NormalizedEvent> globalDataStream) {
            this.globalDataStream = globalDataStream;
            return this;
        }

        public AggregateTransportsFunction.Builder setEnv(StreamExecutionEnvironment env) {
            this.env = env;
            return this;
        }

        public abstract T build();
    }

    public abstract DataStream<TransportOuterClass.Transport> aggregate();
}
