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

    @Test
    public void TestStaysInStationWrongOrder() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationWrongOrder();
        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        Assert.assertTrue(countOfEventsInStream(generatedEvents) == 0);
    }

    @Test
    public void TestStaysInStationDoubleDepatureOneMatch() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationDoubleEvents();
        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        Assert.assertTrue(countOfEventsInStream(generatedEvents)==1);
    }

    @Test
    public void TestStaysInStationSurrounded() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationSurrounded();
        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        Assert.assertTrue(countOfEventsInStream(generatedEvents)==1);
    }

    @Test
    public void TestNoMatchWhenChangingLocations() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.changesStation();
        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        Assert.assertTrue(countOfEventsInStream(generatedEvents)==0);
    }

    @Test
    public void TestHasInterruptionWhenStayingInStation() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationWithInterruption();
        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        Assert.assertTrue(countOfEventsInStream(generatedEvents)==0);
    }

    @Test
    public void TestStaysInStationDirectly() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationDirectly();
        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        Assert.assertTrue(countOfEventsInStream(generatedEvents)==1);
    }
    
}