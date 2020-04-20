package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.junit.Assert;
import org.junit.Test;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventProtos.StaysInStationEvent;
import java.util.*;

import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public class PatternTests {

    @Test
    public void TestStaysInStationDirectly() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationDirectly();
        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationIterativePattern);

        DataStream<StaysInStationEvent> generatedEvents = 
            StaysInStationPattern.generateEvents(patternStream);

        int count = 0;
        System.out.println("Count = " + count);
        Iterator<StaysInStationEvent> detectedEventsItr = DataStreamUtils.collect(generatedEvents);
        while(detectedEventsItr.hasNext()){
            StaysInStationEvent event = detectedEventsItr.next();
            count ++;
        }

        Assert.assertTrue(count==1);
    }
    
}