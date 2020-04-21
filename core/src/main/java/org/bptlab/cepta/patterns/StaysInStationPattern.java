package org.bptlab.cepta.patterns;

import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.*;
import org.apache.flink.cep.pattern.conditions.*;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventProtos.StaysInStationEvent;

import java.util.*;


public class StaysInStationPattern {   
    public static final Pattern<LiveTrainData, ?> staysInStationPattern = 
      Pattern.<LiveTrainData>begin("arrivesInStation")
      .where(new SimpleCondition<LiveTrainData>(){
        @Override
        public boolean filter(LiveTrainData event) {
          return event.getStatus() == 3;
        }
      })
      .next("departuresFromStation")
      .where(new IterativeCondition<LiveTrainData>(){
        @Override
        public boolean filter (LiveTrainData incoming, Context<LiveTrainData> context){
        if (incoming.getStatus() != 4) {
          return false;
        }
        
        LiveTrainData first = null;
        try {
          for (LiveTrainData previous : context.getEventsForPattern("arrivesInStation")){
            first = previous;
            break;
          }
        } catch (Exception e) {
          //TODO: handle exception
        }
       
        if (first.getStationId() == incoming.getStationId()) {
          return true;
        }

        return false;

        }      
    });

    public static DataStream<StaysInStationEvent> generateEvents(PatternStream<LiveTrainData> patternStream){
      return patternStream.select(
        (Map<String, List<LiveTrainData>> pattern) -> {
            LiveTrainData first = (LiveTrainData) pattern.get("arrivesInStation").get(0);

            StaysInStationEvent detected = StaysInStationEvent.newBuilder()
                .setTrainSectionId(first.getTrainSectionId())
                .setStationId(first.getStationId())
                .setTrainId(first.getTrainId())
                .setEventTime(first.getEventTime())
                .build();

            return detected;
        });
    }
}