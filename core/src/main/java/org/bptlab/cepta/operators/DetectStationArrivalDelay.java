package org.bptlab.cepta.operators;


import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.utils.notification.NotificationHelper;

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
            long delay;
            String delayDetail;
            if (expected.hasPlannedEventTime()){
                delay = observed.getEventTime().getSeconds() - expected.getPlannedEventTime().getSeconds();
                delayDetail ="ArrivalDelay derived from PlannedTrainData Correlation (ReferenceDelay: "+observed.getDelay()*60+" seconds)";
            } else {
                //Send already known Delay of LiveTrainData Event if not PlannedTrainData is available
                delay = observed.getDelay()*60;
                delayDetail ="ArrivalDelay derived from LiveTrainData";
            }

            // Only send a delay notification if some threshold is exceeded (DIRTY FIX for now 0 )
            if (Math.abs(delay) >= 0) {
                NotificationOuterClass.Notification notification = NotificationHelper.getTrainDelayNotificationFrom(String.valueOf(observed.getTrainSectionId()), delay,delayDetail,observed.getStationId() );
                collector.collect(notification);
            }
        } catch ( NullPointerException e) {
            // Do not send a delay event
        }
    }
}
