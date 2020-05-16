package org.bptlab.cepta;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.apache.flink.api.java.tuple.Tuple2;

import org.bptlab.cepta.operators.SumOfDelayAtStationFunction;

import org.bptlab.cepta.providers.TrainDelayNotificationDataProvider;

import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;

public class SumOfDelayAtStationTests {
    @Test
    public void TestSumOfDelaysAtStation() throws IOException {

        boolean pass = true;
        Long expectedStation1 = 1L;
        Long expectedStation2 = 2L;
        Long expectedDelayAtStation1 = 25L;
        Long expectedDelayAtStation2 = 13L;

//        SumOfDelayAtStationFunction sumOfDelayAtStationFunction = new SumOfDelayAtStationFunction();
        // the provider provides four TrainDelayNotification elements
        // element 1 has stationId 1, trainId 1, delay 10
        // element 2 has stationId 2, trainId 2, delay 5
        // element 3 has stationId 1, trainId 2, delay 15
        // element 4 has stationId 2, trainId 1, delay 8
        DataStream<NotificationOuterClass.Notification> delayNotificationStream = TrainDelayNotificationDataProvider.NotificationDataStream();

        DataStream<Tuple2<Long, Long>> locationAndDelayStream = SumOfDelayAtStationFunction.sumOfDelayAtStation(delayNotificationStream, 4);
        ArrayList<Tuple2<Long, Long>> locationAndDelayArray = new ArrayList<>();
        Iterator<Tuple2<Long, Long>> iterator = DataStreamUtils.collect(locationAndDelayStream);
        while(iterator.hasNext()){
            Tuple2<Long, Long> tuple = iterator.next();
            locationAndDelayArray.add(tuple);
        }

        for (Tuple2<Long, Long> tuple : locationAndDelayArray) {
            if (tuple.f0.equals(expectedStation1)) {
                if (!tuple.f1.equals(expectedDelayAtStation1)) {
                    pass = false;
                }
            }

            if (tuple.f0.equals(expectedStation2)) {
                if (!tuple.f1.equals(expectedDelayAtStation2)) {
                    pass = false;
                }
            }
        }

        Assert.assertTrue(pass);
    }
}