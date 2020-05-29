package org.bptlab.cepta.patterns;

import java.util.Map;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cep.pattern.*;
import org.apache.flink.cep.pattern.conditions.*;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.NoMatchingPlannedTrainDataEventOuterClass.NoMatchingPlannedTrainDataEvent;

import java.util.*;

public class NoMatchingPlannedTrainDataPattern{
    /**
     * this "pattern" recognizes, when a LiveTrainDataEvent
     * does not have a corresponding PlannedTrainData
     * 
     * it only works on a DataStream with already correlated live and planned data
     */

    public static Pattern<Tuple2<LiveTrainData, PlannedTrainData>, ?> noMatchingPlannedTrainDataPattern(){
        // for all statuses
        return Pattern.<Tuple2<LiveTrainData, PlannedTrainData>>begin("start")
            .where(new SimpleCondition<Tuple2<LiveTrainData, PlannedTrainData>>(){
                @Override
                public boolean filter(Tuple2<LiveTrainData, PlannedTrainData> income){
                    Tuple2<LiveTrainData, PlannedTrainData> event = income;
                    if (event.f1 == null){
                        return true;
                    }
                    return false;
                }
            });
    }

    public static Pattern<Tuple2<LiveTrainData, PlannedTrainData>, ?> noMatchingPlannedTrainDataForArrivingPattern(){
        // for arriving statuses (1, 3)
        List<Long> arrivingStatuses = Arrays.asList(new Long[]{1l, 3l});
        return Pattern.<Tuple2<LiveTrainData, PlannedTrainData>>begin("start")
            .where(new SimpleCondition<Tuple2<LiveTrainData, PlannedTrainData>>(){
                @Override
                public boolean filter(Tuple2<LiveTrainData, PlannedTrainData> income){
                    Tuple2<LiveTrainData, PlannedTrainData> event = income;
                    if (arrivingStatuses.contains(event.f0.getStatus()) && event.f1 == null){
                        return true;
                    }
                    return false;
                }
            });
    }

    public static Pattern<Tuple2<LiveTrainData, PlannedTrainData>, ?> noMatchingPlannedTrainDataForDepartingPattern(){
        // for departing statuses (2, 4)
        List<Long> departingStatuses = Arrays.asList(new Long[]{2l, 4l});
        return Pattern.<Tuple2<LiveTrainData, PlannedTrainData>>begin("start")
            .where(new SimpleCondition<Tuple2<LiveTrainData, PlannedTrainData>>(){
                @Override
                public boolean filter(Tuple2<LiveTrainData, PlannedTrainData> income){
                    Tuple2<LiveTrainData, PlannedTrainData> event = income;
                    if (departingStatuses.contains(event.f0.getStatus()) && event.f1 == null){
                        return true;
                    }
                    return false;
                }
            });
    }

    public static Pattern<Tuple2<LiveTrainData, PlannedTrainData>, ?> noMatchingPlannedTrainDataForDriveThroughsPattern(){
        // for drive through statuses (5)
        return Pattern.<Tuple2<LiveTrainData, PlannedTrainData>>begin("start")
            .where(new SimpleCondition<Tuple2<LiveTrainData, PlannedTrainData>>(){
                @Override
                public boolean filter(Tuple2<LiveTrainData, PlannedTrainData> income){
                    Tuple2<LiveTrainData, PlannedTrainData> event = income;
                    if (event.f0.getStatus() == 5 && event.f1 == null){
                        return true;
                    }
                    return false;
                }
            });
    }
    
    public static PatternProcessFunction<Tuple2<LiveTrainData, PlannedTrainData>, NoMatchingPlannedTrainDataEvent> generateNMPTDEventsFunc(){
        return new PatternProcessFunction<Tuple2<LiveTrainData, PlannedTrainData>, NoMatchingPlannedTrainDataEvent>(){
            @Override
            public void processMatch(Map<String, List<Tuple2<LiveTrainData, PlannedTrainData>>> match, Context ctx, Collector<NoMatchingPlannedTrainDataEvent> out) throws Exception{
                LiveTrainData liveEvent = match.get("start").get(0).f0;
                out.collect(NoMatchingPlannedTrainDataEvent.newBuilder()
                    .setTrainSectionId(liveEvent.getTrainSectionId())
                    .setStationId(liveEvent.getStationId())
                    .setTrainId(liveEvent.getTrainId())
                    .setEventTime(liveEvent.getEventTime())
                    .build());
            }
        };
    }
}