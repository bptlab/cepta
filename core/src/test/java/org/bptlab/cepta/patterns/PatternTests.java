package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.junit.Assert;
import org.junit.Test;

import sun.reflect.annotation.ExceptionProxy;

import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventProtos.StaysInStationEvent;
import java.util.*;

import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public class PatternTests {

    private <T> int countOfEventsInStream(DataStream<T> input) throws Exception{
        int count = 0;
        Iterator<T> inputIterator = DataStreamUtils.collect(input);
        while(inputIterator.hasNext()){
            T event = inputIterator.next();
            count ++;
        }
        return count;
    }

    private int countOfMatchesIn(DataStream<LiveTrainData> input) throws Exception{
        PatternStream<LiveTrainData> patternStream = CEP.pattern(input, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        return countOfEventsInStream(generatedEvents);
    }

    @Test
    public void TestStaysInStationWrongOrder() throws Exception {
        Assert.assertTrue(
            countOfMatchesIn(LiveTrainDataProvider.staysInStationWrongOrder())
             == 0);   
    }

    @Test
    public void TestStaysInStationDoubleDepatureOneMatch() throws Exception {
        Assert.assertTrue(
            countOfMatchesIn(LiveTrainDataProvider.staysInStationDoubleEvents())
             == 1);  
    }

    @Test
    public void TestStaysInStationSurrounded() throws Exception {
        Assert.assertTrue(
            countOfMatchesIn(LiveTrainDataProvider.staysInStationSurrounded())
             == 1);  
    }

    @Test
    public void TestNoMatchWhenChangingLocations() throws Exception {
        Assert.assertTrue(
            countOfMatchesIn(LiveTrainDataProvider.changesStation())
             == 0);  
    }

    @Test
    public void TestHasInterruptionWhenStayingInStation() throws Exception {
        Assert.assertTrue(
            countOfMatchesIn(LiveTrainDataProvider.staysInStationWithInterruption())
             == 0);  
    }

    @Test
    public void TestStaysInStationDirectly() throws Exception {
        Assert.assertTrue(
            countOfMatchesIn(LiveTrainDataProvider.staysInStationDirectly())
             == 1);  
    }
    
}