package org.bptlab.cepta.operators;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;

public class BA_State_Planned extends ProcessFunction<PlannedTrainDataOuterClass.PlannedTrainData, PlannedTrainDataOuterClass.PlannedTrainData> {

    private transient MapState<Integer, Timestamp> stationsWithPlannedTime; // List with stations and planned arrival time

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
       MapStateDescriptor<Integer, Timestamp> descriptor = new MapStateDescriptor<Integer, Timestamp>(
               "bloedeMap",
               TypeInformation.of(Integer.class), TypeInformation.of(Timestamp.class));
        stationsWithPlannedTime = getRuntimeContext().getMapState(descriptor);
    }

    @Override
    public void close() throws Exception {
        stationsWithPlannedTime.clear();

    }

    @Override
    public void processElement(PlannedTrainDataOuterClass.PlannedTrainData plannedTrainData, Context context, Collector<PlannedTrainDataOuterClass.PlannedTrainData> collector) throws Exception {

    }
}
