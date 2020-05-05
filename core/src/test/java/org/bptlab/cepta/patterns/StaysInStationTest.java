package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.bptlab.cepta.utils.functions.StreamUtils;

import org.bptlab.cepta.providers.StaysInStationPatternProvider;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.junit.Assert;
import org.junit.Test;

import sun.reflect.annotation.ExceptionProxy;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventOuterClass.StaysInStationEvent;
import java.util.*;

import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;

public class StaysInStationTest {

   
    private int countOfMatchesIn(DataStream<LiveTrainData> input) throws Exception{
        PatternStream<LiveTrainData> patternStream = CEP.pattern(input, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            patternStream.process(StaysInStationPattern.staysInStationProcessFunction());

        return StreamUtils.countOfEventsInStream(generatedEvents);
    }

    @Test
    public void TestStaysInStationWrongOrder() throws Exception {
        Assert.assertEquals(
            countOfMatchesIn(StaysInStationPatternProvider.staysInStationWrongOrder()), 0);   
    }

    @Test
    public void TestStaysInStationDoubleDepatureOneMatch() throws Exception {
        Assert.assertEquals(
            countOfMatchesIn(StaysInStationPatternProvider.staysInStationDoubleEvents()), 1);  
    }

    @Test
    public void TestStaysInStationSurrounded() throws Exception {
        Assert.assertEquals(
            countOfMatchesIn(StaysInStationPatternProvider.staysInStationSurrounded()), 1);  
    }

    @Test
    public void TestNoMatchWhenChangingLocations() throws Exception {
        Assert.assertEquals(
            countOfMatchesIn(StaysInStationPatternProvider.changesStation()), 0);  
    }

    @Test
    public void TestOtherIncomingEventsWhileStayingInStation() throws Exception {
        Assert.assertEquals(
            countOfMatchesIn(StaysInStationPatternProvider.staysInStationWithInterruption()), 1);  
    }

    @Test
    public void TestStaysInStationDirectly() throws Exception {
        Assert.assertEquals(
            countOfMatchesIn(StaysInStationPatternProvider.staysInStationDirectly()), 1);  
    }
    
}