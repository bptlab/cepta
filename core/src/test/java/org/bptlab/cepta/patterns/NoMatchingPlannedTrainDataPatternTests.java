package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.Pattern;

import org.bptlab.cepta.utils.functions.StreamUtils;
import org.bptlab.cepta.providers.CorrelatedLivePlannedDataProvider;
import org.bptlab.cepta.patterns.NoMatchingPlannedTrainDataPattern;
import org.junit.Assert;
import org.junit.Test;

import sun.reflect.annotation.ExceptionProxy;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.NoMatchingPlannedTrainDataEventOuterClass.NoMatchingPlannedTrainDataEvent;
import java.util.*;

import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;

public class NoMatchingPlannedTrainDataPatternTests {

    @Test
    public void oneWithoutMatchingPlanned() throws Exception {
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> stream = CorrelatedLivePlannedDataProvider.oneWithoutMatchingPlanned();
        Assert.assertEquals(1, countOfMatchesIn(stream, NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataPattern()));
    }

    @Test
    public void oneArrivingWithoutMatchingPlanned() throws Exception {
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> stream = CorrelatedLivePlannedDataProvider.oneWithoutMatchingPlanned();
        Assert.assertEquals(1, countOfMatchesIn(stream, NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataForArrivingPattern()));
    }
    @Test
    public void oneArrivingWithoutMatchingPlannedForDeparting() throws Exception {
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> stream = CorrelatedLivePlannedDataProvider.oneWithoutMatchingPlanned();
        Assert.assertEquals(0, countOfMatchesIn(stream, NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataForDepartingPattern()));
    }
    @Test
    public void oneArrivingWithoutMatchingPlannedForDriveThrough() throws Exception {
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> stream = CorrelatedLivePlannedDataProvider.oneWithoutMatchingPlanned();
        Assert.assertEquals(0, countOfMatchesIn(stream, NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataForDriveThroughsPattern()));
    }
  
    @Test
    public void threeWithoutMatchingPlanned() throws Exception {
        Assert.assertEquals(3, countOfMatchesIn(CorrelatedLivePlannedDataProvider.threeWithoutMatchingPlanned(), NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataPattern()));   
    }

    @Test
    public void oneWithMatchingPlanned() throws Exception {
        Assert.assertEquals(0, countOfMatchesIn(CorrelatedLivePlannedDataProvider.oneWithMatchingPlanned(), NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataPattern()));   
    }
  
    @Test
    public void threeWithMatchingPlanned() throws Exception {
        Assert.assertEquals(0, countOfMatchesIn(CorrelatedLivePlannedDataProvider.threeWithMatchingPlanned(), NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataPattern()));   
    }

    private int countOfMatchesIn(DataStream<Tuple2<LiveTrainData, PlannedTrainData>> input, Pattern<Tuple2<LiveTrainData, PlannedTrainData>, ?> pattern ) throws Exception{
        PatternStream<Tuple2<LiveTrainData, PlannedTrainData>> patternStream = CEP.pattern(input, pattern);

        DataStream<NoMatchingPlannedTrainDataEvent> generatedEvents = 
            patternStream.process(NoMatchingPlannedTrainDataPattern.generateNMPTDEventsFunc());

        return StreamUtils.countOfEventsInStream(generatedEvents);
    }
}