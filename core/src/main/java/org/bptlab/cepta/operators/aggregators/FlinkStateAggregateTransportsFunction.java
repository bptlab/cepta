package org.bptlab.cepta.operators.aggregators;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.internal.event.Event;
import org.bptlab.cepta.models.internal.transport.TransportOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.models.internal.updates.live.LiveUpdateOuterClass;
import org.bptlab.cepta.models.internal.updates.plan.PlanUpdateOuterClass;

public class FlinkStateAggregateTransportsFunction extends AggregateTransportsFunction {

    public FlinkStateAggregateTransportsFunction(AggregateTransportsFunction.Builder builder) {
        env = builder.env;
        liveStream = builder.liveStream;
        planStream = builder.planStream;
    }

    public static class Builder extends AggregateTransportsFunction.Builder<FlinkStateAggregateTransportsFunction> {
        @Override
        public FlinkStateAggregateTransportsFunction build() {
            return new FlinkStateAggregateTransportsFunction(this);
        }
    }

    public static AggregateTransportsFunction.Builder newBuilder() {
        return new FlinkStateAggregateTransportsFunction.Builder();
    }

    public DataStream<TransportOuterClass.Transport> aggregate() {
        // WindowedStream<PlanUpdateOuterClass.PlanUpdate, Ids.CeptaTransportID, TimeWindow> plan =

        // Plan and live data by transport ID
        KeyedStream<PlanUpdateOuterClass.PlanUpdate, Ids.CeptaTransportID> keyedPlanStream = planStream
                .flatMap(new FlatMapFunction<Event.NormalizedEvent, PlanUpdateOuterClass.PlanUpdate>() {
                    @Override
                    public void flatMap(Event.NormalizedEvent normalizedEvent, Collector<PlanUpdateOuterClass.PlanUpdate> collector) throws Exception {
                        if (!normalizedEvent.hasPlanUpdate()) return;
                        collector.collect(normalizedEvent.getPlanUpdate());
                    }
                })
                .keyBy((KeySelector<PlanUpdateOuterClass.PlanUpdate, Ids.CeptaTransportID>) PlanUpdateOuterClass.PlanUpdate::getTransportCeptaId);

        KeyedStream<LiveUpdateOuterClass.LiveUpdate, Ids.CeptaTransportID> liveUpdateStream = liveStream
                .flatMap(new FlatMapFunction<Event.NormalizedEvent, LiveUpdateOuterClass.LiveUpdate>() {
                    @Override
                    public void flatMap(Event.NormalizedEvent normalizedEvent, Collector<LiveUpdateOuterClass.LiveUpdate> collector) throws Exception {
                        if (!normalizedEvent.hasLiveUpdate()) return;
                        collector.collect(normalizedEvent.getLiveUpdate());
                    }
                })
                .keyBy((KeySelector<LiveUpdateOuterClass.LiveUpdate, Ids.CeptaTransportID>) LiveUpdateOuterClass.LiveUpdate::getTransportCeptaId);

        // Expect plan data to arrive in one flush
        SingleOutputStreamOperator<TransportOuterClass.Transport> sessionPlanStream  = keyedPlanStream
                .window(EventTimeSessionWindows.withGap(Time.minutes(10)))
                .aggregate(new AggregatePlanUpdatesToTransport());

        // .window(TumblingEventTimeWindows.of(Time.days(10)));

        /*
        WindowedStream<Event.NormalizedEvent, Ids.CeptaTransportID, GlobalWindow> global = planStream
                .flatMap((FlatMapFunction<Event.NormalizedEvent, PlanUpdateOuterClass.PlanUpdate>) (normalizedEvent, collector) -> {
                    if (!normalizedEvent.hasLiveUpdate()) return;
                    collector.collect(normalizedEvent.getPlanUpdate());
                })
                .keyBy((KeySelector<PlanUpdateOuterClass.PlanUpdate, Ids.CeptaTransportID>) PlanUpdateOuterClass.PlanUpdate::getTransportCeptaId)
                .window(TumblingEventTimeWindows.of(Time.days(10)));

                // .reduce(new Summer());
         */
        return null;
    };
}
