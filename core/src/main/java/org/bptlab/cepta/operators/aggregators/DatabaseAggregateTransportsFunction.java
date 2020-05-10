package org.bptlab.cepta.operators.aggregators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.models.internal.transport.TransportOuterClass;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DatabaseAggregateTransportsFunction extends AggregateTransportsFunction {

    public DatabaseAggregateTransportsFunction(AggregateTransportsFunction.Builder builder) {
        env = builder.env;
        liveStream = builder.liveStream;
        planStream = builder.planStream;
    }

    public static class Builder extends AggregateTransportsFunction.Builder<DatabaseAggregateTransportsFunction> {
        @Override
        public DatabaseAggregateTransportsFunction build() {
            return new DatabaseAggregateTransportsFunction(this);
        }
    }

    public AggregateTransportsFunction.Builder newBuilder() {
        return new DatabaseAggregateTransportsFunction.Builder();
    }

    public DataStream<TransportOuterClass.Transport> aggregate() {
        // TODO: Might implement for comparison
        throw new NotImplementedException();
    };
}
