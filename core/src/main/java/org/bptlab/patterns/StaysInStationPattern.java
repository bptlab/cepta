package org.bptlab.cepta.patterns;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

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
      .where(new SimpleCondition<LiveTrainData>(){
        @Override
        public boolean filter(LiveTrainData event) {
          return event.getStatus() == 4;
        }
      });
    

    public static final Pattern<LiveTrainData, ?> staysInStationIterativePattern = 
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
        
        LiveTrainData first;
        for (LiveTrainData previous : context.getEventsForPattern("arrivesInStation")){
          first = previous;
          break;
        }
        if (first.getStationId() == incoming.getStationId()) {
          return true;
        }

        return false;

        }      
    });



}