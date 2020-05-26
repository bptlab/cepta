package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.bptlab.cepta.utils.functions.StreamUtils;

import org.bptlab.cepta.providers.RBDelayPatternProvider;
import org.bptlab.cepta.patterns.RBDelayPattern;
import org.junit.Assert;
import org.junit.Test;

import sun.reflect.annotation.ExceptionProxy;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
//import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
//import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventOuterClass.StaysInStationEvent;
import java.util.*;

public class RBDelayPatternTest {

    /*
    private int countOfMatchesIn(DataStream<LiveTrainData> input) throws Exception{
        PatternStream<LiveTrainData> patternStream = CEP.pattern(input, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents =
            patternStream.process(StaysInStationPattern.staysInStationProcessFunction());

        return StreamUtils.countOfEventsInStream(generatedEvents);
    }
    */

    @Test
    public void TestStaysInStationDirectly() throws Exception {
//        Assert.assertTrue(RBDelayPatternProvider.receiveEventsFromReplayer());
    }
    
}