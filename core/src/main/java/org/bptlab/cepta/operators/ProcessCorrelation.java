package org.bptlab.cepta.operators;

import com.google.protobuf.Timestamp;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;

public class ProcessCorrelation extends KeyedCoProcessFunction<Long, LiveTrainDataOuterClass.LiveTrainData, PlannedTrainDataOuterClass.PlannedTrainData, NotificationOuterClass.MyDelayNotification>{

  public ProcessCorrelation(){

  }


  public transient MapState<Long, Timestamp> stationsWithTimestampsState;
  /*public transient ValueState<TreeSet<StationWithTimestamp>> stationsOrderState; // ordered list representing the stations' order
  public transient ValueState<Long> delayState;
  public transient ValueState<Long> currentStationState;*/

  @Override
  public void open(Configuration config){
    // set up the states
/*
            ValueStateDescriptor<TreeSet<StationWithTimestamp>> stationsOrderStateDesc = new ValueStateDescriptor<TreeSet<StationWithTimestamp>>(
                "StationsOrderState",
                TypeInformation.of(new TypeHint<TreeSet<StationWithTimestamp>>() { }));
            stationsOrderState = getRuntimeContext().getState(stationsOrderStateDesc);

            ValueStateDescriptor<Long> delayStateDesc = new ValueStateDescriptor<Long>(
                "DelayState",
                TypeInformation.of(Long.class));
            delayState = getRuntimeContext().getState(delayStateDesc);

            ValueStateDescriptor<Long> currentStationStateDesc = new ValueStateDescriptor<Long>(
                "CurrentStationState",
                TypeInformation.of(Long.class));
            currentStationState = getRuntimeContext().getState(currentStationStateDesc);*/

    MapStateDescriptor<Long, Timestamp> stationsWithTimestampsStateDesc = new MapStateDescriptor<Long, Timestamp>(
    "StationsWithTimestampsState",
    TypeInformation.of(Long.class), TypeInformation.of(Timestamp.class));
    stationsWithTimestampsState = getRuntimeContext().getMapState(stationsWithTimestampsStateDesc);
  }

  @Override
  public void processElement1(LiveTrainDataOuterClass.LiveTrainData liveTrainData, Context context, Collector<NotificationOuterClass.MyDelayNotification> collector) throws Exception {
  /*
   * retrieve stuff from state
   * update state
   */

  Long delay = 0L;
  try{
  //System.out.println(stationsWithTimestampsState.get(liveTrainData.getStationId()));
  delay = stationsWithTimestampsState.get(liveTrainData.getStationId()).getSeconds() - liveTrainData.getEventTime().getSeconds();
  }catch (NullPointerException e){
  // there is no corresponding planned data available
  }
          /*delayState.update(delay);
          currentStationState.update(liveTrainData.getStationId());*/
  collector.collect(
  NotificationOuterClass.MyDelayNotification.newBuilder()
    .setStationId(liveTrainData.getStationId())
    .setTrainId(liveTrainData.getTrainId())
    .setDelay(delay).build());
  }
  @Override
  public void processElement2(PlannedTrainDataOuterClass.PlannedTrainData plannedTrainData,
                              Context context,
                              Collector<NotificationOuterClass.MyDelayNotification> collector)
  throws Exception {

  // add entry to state or update earlier value
  stationsWithTimestampsState.put(plannedTrainData.getStationId(), plannedTrainData.getPlannedEventTime());
  /*try{
    stationsOrderState.value().add(new StationWithTimestamp(plannedTrainData.getStationId(), plannedTrainData.getPlannedEventTime()));}
  catch (NullPointerException e){
    //e.printStackTrace();
    stationsOrderState.update(new TreeSet<StationWithTimestamp>());
  }*/
  }
}
