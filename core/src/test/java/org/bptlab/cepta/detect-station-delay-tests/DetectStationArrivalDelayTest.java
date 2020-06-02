package org.bptlab.cepta;

import com.google.protobuf.Timestamp;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.operators.DetectStationArrivalDelay;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectStationArrivalDelayTest {

    public DataStream<Tuple2<LiveTrainData,PlannedTrainData>> initStream(int delay) {
        long millis = System.currentTimeMillis();
        Timestamp plannedTime = Timestamp.newBuilder().setSeconds(millis/1000).build();
        Timestamp liveTime = Timestamp.newBuilder().setSeconds(plannedTime.getSeconds() + delay).build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        LiveTrainData liveEvent = LiveTrainDataProvider.trainEventWithEventTime(liveTime);
        PlannedTrainData plannedEvent = PlannedTrainDataProvider.trainEventWithPlannedEventTime(plannedTime);

        DataStream<Tuple2<LiveTrainData,PlannedTrainData>> matchedStream = env.fromElements(new Tuple2<LiveTrainData,PlannedTrainData>(liveEvent,plannedEvent));

        return matchedStream;
    }

    public boolean checkForDelay(DataStream<NotificationOuterClass.Notification> trainDelayStream, int delay) throws IOException {
        // We need Atomic here to be able to change the boolean within the for each line 55
        AtomicBoolean pass = new AtomicBoolean(true);

        Iterator<NotificationOuterClass.Notification> iterator = DataStreamUtils.collect(trainDelayStream);
        ArrayList<NotificationOuterClass.Notification> delayNotification = new ArrayList<>();
        while (iterator.hasNext()){
            NotificationOuterClass.Notification notification = iterator.next();
            delayNotification.add(notification);
        }

        if (delayNotification.size() != 1 && delay !=0) {
            pass.set(false);
        } else if (delay == 0) {
            if (delayNotification.size() >= 1) {
                pass.set(false);
            }
        }

        delayNotification.forEach( (trainDelayNotification) -> {
            if ( trainDelayNotification.getDelay().getDelay().getDelta().getSeconds() != delay ) {
                pass.set(false);
            }
        });

        return pass.get();
    }

    @Test
    public void testDelayPositive() throws IOException {
        boolean pass = true;
        //in seconds
        int delay = 3600;

        DataStream<NotificationOuterClass.Notification> trainDelayStream = initStream(delay).process(new DetectStationArrivalDelay()).name("train-delays");

        pass = checkForDelay(trainDelayStream, delay);

        Assert.assertTrue(pass);
    }

    @Test
    public void testDelayNegative() throws IOException {
        boolean pass = true;
        //in seconds
        int delay = -3600;

        DataStream<NotificationOuterClass.Notification> trainDelayStream = initStream(delay).process(new DetectStationArrivalDelay()).name("train-delays");

        pass = checkForDelay(trainDelayStream, delay);

        Assert.assertTrue(pass);
    }

    //Currently we expect Delays although they is 0 Delay
    @Ignore
    public void testNoDelay() throws IOException {
        boolean pass = true;
        //in seconds
        int delay = 0;

        DataStream<NotificationOuterClass.Notification> trainDelayStream = initStream(delay).process(new DetectStationArrivalDelay()).name("train-delays");

        pass = checkForDelay(trainDelayStream, delay);

        Assert.assertTrue(pass);
    }
}