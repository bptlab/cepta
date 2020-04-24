package org.bptlab.cepta.operators;


import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.bptlab.cepta.models.events.train.TrainDelayNotificationProtos.TrainDelayNotification;
import org.bptlab.cepta.utils.converters.PlannedTrainDataDatabaseConverter;



public class DetectStationArrivalDelay extends
        ProcessFunction<Tuple2<LiveTrainData, PlannedTrainData>, TrainDelayNotification> {

    @Override
    public void processElement(Tuple2<LiveTrainData, PlannedTrainData> liveTrainDataPlannedTrainDataTuple2,
                               Context context,
                               Collector<TrainDelayNotification> collector) throws Exception {
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
                TrainDelayNotification.Builder trainDelayBuilder = TrainDelayNotification.newBuilder();
                trainDelayBuilder.setDelay(delay);
                trainDelayBuilder.setTrainId(observed.getTrainSectionId());
                trainDelayBuilder.setStationId(observed.getStationId());

                collector.collect(trainDelayBuilder.build());
            }
        } catch ( NullPointerException e) {
            // Do not send a delay event
        }
    }
}
