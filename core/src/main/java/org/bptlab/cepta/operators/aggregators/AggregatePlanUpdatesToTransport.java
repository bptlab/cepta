package org.bptlab.cepta.operators.aggregators;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.bptlab.cepta.models.internal.transport.TransportOuterClass;
import org.bptlab.cepta.models.internal.station.StationOuterClass.Station;
import org.bptlab.cepta.models.internal.updates.plan.PlanUpdateOuterClass;
import java.util.stream.Collectors;

public class AggregatePlanUpdatesToTransport implements AggregateFunction<PlanUpdateOuterClass.PlanUpdate, TransportOuterClass.Transport.Builder, TransportOuterClass.Transport> {
    @Override
    public TransportOuterClass.Transport.Builder createAccumulator() {
        return TransportOuterClass.Transport.newBuilder();
    }

    @Override
    public TransportOuterClass.Transport.Builder add(PlanUpdateOuterClass.PlanUpdate planUpdate, TransportOuterClass.Transport.Builder acc) {
        acc.addAllStations(planUpdate.getSectionUpdatesList().parallelStream().map((update) -> {
            return Station.newBuilder().build();
        }).collect(Collectors.toList()));
        return acc;
    }

    @Override
    public TransportOuterClass.Transport getResult(TransportOuterClass.Transport.Builder acc) {
        return acc.build();
    }

    @Override
    public TransportOuterClass.Transport.Builder merge(TransportOuterClass.Transport.Builder a, TransportOuterClass.Transport.Builder b) {
        a.mergeFrom(b.buildPartial());
        return null;
    }
}
