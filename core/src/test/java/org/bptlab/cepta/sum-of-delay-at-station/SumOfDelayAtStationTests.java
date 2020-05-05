package org.bptlab.cepta;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

import org.bptlab.cepta.utils.functions.StreamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.apache.flink.api.java.tuple.Tuple2;

import org.bptlab.cepta.operators.SumOfDelayAtStationFunction;

import org.bptlab.cepta.providers.TrainDelayNotificationDataProvider;
import org.bptlab.cepta.providers.LiveTrainDataProvider;

import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass.TrainDelayNotification;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;

public class SumOfDelayAtStationTests {
    @Test
    public void TestSumOfDelaysAtStationWithTrainDelayNotification() throws IOException {

        boolean pass = true;
        Integer expectedStation1 = 1;
        Integer expectedStation2 = 2;
        Double expectedDelayAtStation1 = 25.0;
        Double expectedDelayAtStation2 = 13.0;

        SumOfDelayAtStationFunction sumOfDelayAtStationFunction = new SumOfDelayAtStationFunction<TrainDelayNotification>();
        // the provider provides four TrainDelayNotification elements
        // element 1 has stationId 1, trainId 1, delay 10
        // element 2 has stationId 2, trainId 2, delay 5
        // element 3 has stationId 1, trainId 2, delay 15
        // element 4 has stationId 2, trainId 1, delay 8
        DataStream<TrainDelayNotification> delayNotificationStream = TrainDelayNotificationDataProvider.TrainDelayNotificationDataStream();

        DataStream<Tuple2<Integer, Double>> stationAndDelayStream = sumOfDelayAtStationFunction.SumOfDelayAtStation(delayNotificationStream, 4, "StationId");

        ArrayList<Tuple2<Integer, Double>> stationAndDelayArray = StreamUtils.collectStreamToArrayList(stationAndDelayStream);

        // check if any tuple is present
        if (stationAndDelayArray.size() == 0) {
            pass = false;
        }
        for (Tuple2<Integer, Double> tuple : stationAndDelayArray) {
            // check if first station matches expected delay
            if (tuple.f0.equals(expectedStation1)) {
                if (!tuple.f1.equals(expectedDelayAtStation1)) {
                    pass = false;
                }
            } 
            // check if second station matches expected delay
            if (tuple.f0.equals(expectedStation2)) {
                if (!tuple.f1.equals(expectedDelayAtStation2)) {
                    pass = false;
                }
            } 
        }
        
        Assert.assertTrue(pass);
    }

    @Test
    public void TestSumOfDelaysAtStationWithLiveTrainData() throws IOException {

        Integer expectedStation1 = 1;
        Double expectedDelayAtStation1 = 3.0;

        SumOfDelayAtStationFunction sumOfDelayAtStationFunction = new SumOfDelayAtStationFunction<LiveTrainData>();
        DataStream<LiveTrainData> delayNotificationStream = LiveTrainDataProvider.liveTrainDatStreamWithDuplicates();

        DataStream<Tuple2<Integer, Double>> stationAndDelayStream = sumOfDelayAtStationFunction.SumOfDelayAtStation(delayNotificationStream, 3, "StationId");

        ArrayList<Tuple2<Integer, Double>> stationAndDelayArray = StreamUtils.collectStreamToArrayList(stationAndDelayStream);

        Assert.assertNotEquals("A delay should have been created",stationAndDelayArray.size(), 0);

        boolean pass = true;

        for (Tuple2<Integer, Double> tuple : stationAndDelayArray) {
            // check if first station matches expected delay
            if (tuple.f0.equals(expectedStation1)) {
                if (!tuple.f1.equals(expectedDelayAtStation1)) {
                    pass = false;
                }
            } 
        }
        Assert.assertTrue(pass);
    }

    @Test
    public void TestSumOfDelaysAtStationRightNumberOfTuples() throws IOException {

        boolean pass = true;
        SumOfDelayAtStationFunction sumOfDelayAtStationFunction = new SumOfDelayAtStationFunction<TrainDelayNotification>();
        // the provider provides four TrainDelayNotification elements
        // element 1 has stationId 1, trainId 1, delay 10
        // element 2 has stationId 2, trainId 2, delay 5
        // element 3 has stationId 1, trainId 2, delay 15
        // element 4 has stationId 2, trainId 1, delay 8
        DataStream<TrainDelayNotification> delayNotificationStream = TrainDelayNotificationDataProvider.TrainDelayNotificationDataStream();

        DataStream<Tuple2<Integer, Double>> stationAndDelayStream = sumOfDelayAtStationFunction.SumOfDelayAtStation(delayNotificationStream, 4, "StationId");
        ArrayList<Tuple2<Integer, Double>> locationAndDelayArray = new ArrayList<>();
        Iterator<Tuple2<Integer, Double>> iterator = DataStreamUtils.collect(stationAndDelayStream);
        while(iterator.hasNext()){
            Tuple2<Integer, Double> tuple = iterator.next();
            locationAndDelayArray.add(tuple);
        }
        // check if any tuple is present
        if (locationAndDelayArray.size() == 0) {
            pass = false;
        }
        // check if there are only 2 Tuples present
        if (locationAndDelayArray.size() != 2) {
            pass = false;
        }
        Assert.assertTrue(pass);
    }
}