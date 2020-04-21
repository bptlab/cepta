package org.bptlab.cepta.patterns;

import java.util.Map;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.*;
import org.apache.flink.cep.pattern.conditions.*;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.functions.PatternProcessFunction.Context;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventProtos.StaysInStationEvent;
import org.bptlab.cepta.models.events.correlatedEvents.NoMatchingPlannedTrainDataEventProtos.NoMatchingPlannedTrainDataEvent;

import java.util.*;

public class NoMatchingPlannedTrainDataPattern{
    /**
     * this "pattern" recognizes, when an arriving LiveTrainData 
     * does not have a corresponding PlannedTrainData
     * 
     * it only works on a DataStream with already correlated live and planned data
     */

    public static final Pattern<Tuple2<LiveTrainData, PlannedTrainData>, ?> noPlanned = Pattern.<Tuple2<LiveTrainData, PlannedTrainData>>begin("start")
        .where(new SimpleCondition<Tuple2<LiveTrainData, PlannedTrainData>>(){
            @Override
            public boolean filter(Tuple2<LiveTrainData, PlannedTrainData> income){
                Tuple2<LiveTrainData, PlannedTrainData> event = income;
                if (event.f0.status <= 3 && event.f1 == null){
                    return true;
                }
                return false;
            }
        });
    
    public static PatternProcessFunction<Tuple2<LiveTrainData, PlannedTrainData>, NoMatchingPlannedTrainDataEvent> patternProcessFunction(){
        return new PatternProcessFunction<Tuple2<LiveTrainData, PlannedTrainData>, NoMatchingPlannedTrainDataEvent>(){
            @Override
            public void processMatch(Map<String, List<Tuple2<LiveTrainData, PlannedTrainData>>> match, Context ctx, Collector<NoMatchingPlannedTrainDataEvent> out) throws Exception{
                LiveTrainData liveEvent = match.get("start").get(0).f0;
                out.collect(NoMatchingPlannedTrainDataEvent.NewBuilder()
                    .setTrainSectionId(liveEvent.getTrainSectionId())
                    .setStationId(liveEvent.getStationId())
                    .setTrainId(liveEvent.getTrainId())
                    .setEventTime(liveEvent.getEventTime())
                    .build());
            }
        };
    }
}