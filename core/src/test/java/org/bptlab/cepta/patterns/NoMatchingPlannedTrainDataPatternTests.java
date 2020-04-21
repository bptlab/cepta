package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.bptlab.cepta.operators.CountOfEventsInStreamFunction;
import org.bptlab.cepta.providers.CorrelatedLivePlannedDataProvider;
import org.bptlab.cepta.patterns.NoMatchingPlannedTrainDataPattern;
import org.junit.Assert;
import org.junit.Test;

import sun.reflect.annotation.ExceptionProxy;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.NoMatchingPlannedTrainDataEventProtos.NoMatchingPlannedTrainDataEvent;
import java.util.*;

import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;

public class NoMatchingPlannedTrainDataPatternTests {

    @Test
    public void oneWithoutMatchingPlanned() throws Exception {
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> stream = CorrelatedLivePlannedDataProvider.oneWithoutMatchingPlanned();
        Assert.assertEquals(1, countOfMatchesIn(stream));   
    }
  
    @Test
    public void threeWithoutMatchingPlanned() throws Exception {
        Assert.assertEquals(3, countOfMatchesIn(CorrelatedLivePlannedDataProvider.threeWithoutMatchingPlanned()));   
    }

    @Test
    public void oneWithMatchingPlanned() throws Exception {
        Assert.assertEquals(0, countOfMatchesIn(CorrelatedLivePlannedDataProvider.oneWithMatchingPlanned()));   
    }
  
    @Test
    public void threeWithMatchingPlanned() throws Exception {
        Assert.assertEquals(0, countOfMatchesIn(CorrelatedLivePlannedDataProvider.threeWithMatchingPlanned()));   
    }

    private int countOfMatchesIn(DataStream<Tuple2<LiveTrainData, PlannedTrainData>> input) throws Exception{
        PatternStream<Tuple2<LiveTrainData, PlannedTrainData>> patternStream = CEP.pattern(input, NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataPattern);

        DataStream<NoMatchingPlannedTrainDataEvent> generatedEvents = 
            patternStream.process(NoMatchingPlannedTrainDataPattern.generateNMPTDEventsFunc());
        
        return CountOfEventsInStreamFunction.countOfEventsInStream(generatedEvents);
    }
}