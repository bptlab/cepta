package org.bptlab.cepta;

import com.google.protobuf.Timestamp;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.bptlab.cepta.models.events.train.TrainDelayNotificationProtos.TrainDelayNotification;
import org.bptlab.cepta.operators.DetectStationArrivalDelay;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectStationArrivalDelayTest {
    @Test
    public void testDelayPositive() throws IOException {
        AtomicBoolean pass = new AtomicBoolean(true);
        //Duration delay = Duration.newBuilder().setSeconds(3600).build();

        long millis = System.currentTimeMillis();
        Timestamp plannedTime = Timestamp.newBuilder().setSeconds(millis/1000).build();
        Timestamp liveTime = Timestamp.newBuilder().setSeconds(plannedTime.getSeconds() + 3600).build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        LiveTrainData liveEvent = LiveTrainDataProvider.trainEventWithEventTime(liveTime);
        PlannedTrainData plannedEvent = PlannedTrainDataProvider.trainEventWithPlannedEventTime(plannedTime);

        DataStream<Tuple2<LiveTrainData,PlannedTrainData>> matchedStream = env.fromElements(new Tuple2<LiveTrainData,PlannedTrainData>(liveEvent,plannedEvent));

        DataStream<TrainDelayNotification> trainDelayStream = matchedStream.process(new DetectStationArrivalDelay()).name("train-delays");

        Iterator<TrainDelayNotification> iterator = DataStreamUtils.collect(trainDelayStream);
        ArrayList<TrainDelayNotification> delayNotification = new ArrayList<>();
        while (iterator.hasNext()){
            TrainDelayNotification notification = iterator.next();
            delayNotification.add(notification);
        }

        if (delayNotification.size() != 1) {
            pass.set(false);
        }

        delayNotification.forEach( (trainDelayNotification) -> {
            if ( trainDelayNotification.getDelay() != 3600 ) {
                pass.set(false);
            }
        });

        Assert.assertTrue(pass.get());
    }
}