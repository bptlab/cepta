package org.bptlab.cepta.patterns;

import org.apache.flink.cep.pattern.*;
import org.apache.flink.cep.pattern.conditions.*;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventOuterClass.StaysInStationEvent;

import java.util.*;


public class StaysInStationPattern {   
    public static final Pattern<LiveTrainData, ?> staysInStationPattern = 
      Pattern.<LiveTrainData>begin("arrivesInStation", AfterMatchSkipStrategy.skipPastLastEvent())
      .where(new SimpleCondition<LiveTrainData>(){
        @Override
        public boolean filter(LiveTrainData event) {
          return event.getStatus() == 3;
        }
      })
      .followedBy("departuresFromStation")
      .where(new IterativeCondition<LiveTrainData>(){
        @Override
        public boolean filter (LiveTrainData incoming, Context<LiveTrainData> context){
          if (incoming.getStatus() != 4) {
            //this is not a departure event
            return false;
          }
          
          try {
            //as we only have exactly one previous event we only need to grab the first from the pattern so far
            LiveTrainData firstEvent = context.getEventsForPattern("arrivesInStation").iterator().next();
            if (firstEvent.getStationId() == incoming.getStationId() && firstEvent.getTrainId() == incoming.getTrainId()) {
              return true;
            }
          } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
          }
          return false;
        }      
    });

    public static PatternProcessFunction<LiveTrainData, StaysInStationEvent> staysInStationProcessFunction(){
      return new PatternProcessFunction<LiveTrainData, StaysInStationEvent>(){
          @Override
          public void processMatch(Map<String, List<LiveTrainData>> match, Context ctx, Collector<StaysInStationEvent> out) throws Exception{
              LiveTrainData  first= match.get("arrivesInStation").get(0);
              out.collect(StaysInStationEvent.newBuilder()
                    .setTrainSectionId(first.getTrainSectionId())
                    .setStationId(first.getStationId())
                    .setTrainId(first.getTrainId())
                    .setEventTime(first.getEventTime())
                    .build());
          }
      };
  }
}