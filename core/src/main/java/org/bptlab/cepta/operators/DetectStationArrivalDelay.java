package org.bptlab.cepta.operators;


import com.google.protobuf.Duration;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.internal.delay.DelayOuterClass;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;

public class DetectStationArrivalDelay extends
        ProcessFunction<Tuple2<LiveTrainData, PlannedTrainData>, NotificationOuterClass.Notification> {

    @Override
    public void processElement(Tuple2<LiveTrainData, PlannedTrainData> liveTrainDataPlannedTrainDataTuple2,
                               Context context,
                               Collector<NotificationOuterClass.Notification> collector) throws Exception {
        LiveTrainData observed = liveTrainDataPlannedTrainDataTuple2.f0;
        PlannedTrainData expected = liveTrainDataPlannedTrainDataTuple2.f1;

        /*
          Delay is defined as the difference between the observed time of a train id at a location id.
          delay > 0 is bad, the train might arrive later than planned
          delay < 0 is good, the train might arrive earlier than planned
         */
        try {
            double delay = observed.getEventTime().getSeconds() - expected.getPlannedEventTime().getSeconds();

            // Only send a delay notification if some threshold is exceeded
            if (Math.abs(delay) > 10) {
                NotificationOuterClass.DelayNotification.Builder delayBuilder = NotificationOuterClass.DelayNotification.newBuilder();
                delayBuilder.setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds((long) delay).build()).build());
                delayBuilder.setTransportId(Ids.CeptaTransportID.newBuilder());
                delayBuilder.setStationId(Ids.CeptaStationID.newBuilder().setId(String.valueOf(observed.getStationId())).build());

                collector.collect(NotificationOuterClass.Notification.newBuilder().setDelay(delayBuilder.build()).build());
            }
        } catch ( NullPointerException e) {
            // Do not send a delay event
        }
    }
}
