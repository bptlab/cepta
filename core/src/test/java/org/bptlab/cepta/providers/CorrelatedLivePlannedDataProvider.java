package org.bptlab.cepta.providers;

import java.util.ArrayList;
import org.javatuples.Pair;
import com.google.protobuf.Timestamp;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;

public class CorrelatedLivePlannedDataProvider {
    public static LiveTrainData getDefaultLiveTrainDataEvent() {
        Timestamp timestamp = TimestampProvider.getDefaultTimestamp();

        LiveTrainData.Builder builder = LiveTrainData.newBuilder();
        builder.setId(1);
        builder.setTrainSectionId(1);
        builder.setStationId(1);
        builder.setEventTime(timestamp);
        builder.setStatus(1);
        builder.setFirstTrainId(1);
        builder.setTrainId(1);
        builder.setPlannedArrivalTimeEndStation(timestamp);
        builder.setDelay(1);
        builder.setEndStationId(1);
        builder.setImId(1);
        builder.setFollowingImId(1);
        builder.setMessageStatus(1);
        builder.setIngestionTime(timestamp);
        return builder.build();
    }
    public static PlannedTrainData getDefaultPlannedTrainDataEvent() {
        Timestamp timestamp = TimestampProvider.getDefaultTimestamp();

        PlannedTrainData.Builder builder = PlannedTrainData.newBuilder();
        builder.setId(1);
        builder.setTrainSectionId(1);
        builder.setStationId(1);
        builder.setPlannedEventTime(timestamp);
        builder.setStatus(1);
        builder.setFirstTrainId(1);
        builder.setTrainId(1);
        builder.setPlannedDepartureTimeStartStation(timestamp);
        builder.setPlannedArrivalTimeEndStation(timestamp);
        builder.setRuId(1);
        builder.setEndStationId(1);
        builder.setImId(1);
        builder.setFollowingImId(1);
        builder.setMessageStatus(1);
        builder.setIngestionTime(timestamp);
        builder.setOriginalTrainId(1);
        return builder.build();
    }

    public static DataStream<Tuple2<LiveTrainData, PlannedTrainData>> oneWithoutMatchingPlanned(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<Tuple2<LiveTrainData, PlannedTrainData>>  trains = new ArrayList<>();

        trains.add(withoutMatchingPlanned());

        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> liveTrainStream = env.fromCollection(trains, 
            new TypeHint<Tuple2<LiveTrainData, PlannedTrainData>>(){}.getTypeInfo());
        return liveTrainStream;
    }

    public static DataStream<Tuple2<LiveTrainData, PlannedTrainData>> threeWithoutMatchingPlanned(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<Tuple2<LiveTrainData, PlannedTrainData>> trains = new ArrayList<>();
    
        trains.add(withoutMatchingPlanned());
        trains.add(withoutMatchingPlanned());
        trains.add(withoutMatchingPlanned());

        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> liveTrainStream = env.fromCollection(trains, 
            TypeInformation.of(new TypeHint<Tuple2<LiveTrainData, PlannedTrainData>>(){}));

        return liveTrainStream;
    }

    public static DataStream<Tuple2<LiveTrainData, PlannedTrainData>> oneWithMatchingPlanned(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<Tuple2<LiveTrainData, PlannedTrainData>>  trains = new ArrayList<>();

        trains.add(withMatchingPlanned());

        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> liveTrainStream = env.fromCollection(trains, 
            TypeInformation.of(new TypeHint<Tuple2<LiveTrainData, PlannedTrainData>>(){}));

        return liveTrainStream;
    }

    public static DataStream<Tuple2<LiveTrainData, PlannedTrainData>> threeWithMatchingPlanned(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<Tuple2<LiveTrainData, PlannedTrainData>> trains = new ArrayList<>();
    
        trains.add(withMatchingPlanned());
        trains.add(withMatchingPlanned());
        trains.add(withMatchingPlanned());

        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> liveTrainStream = env.fromCollection(trains, 
            TypeInformation.of(new TypeHint<Tuple2<LiveTrainData, PlannedTrainData>>(){}));

        return liveTrainStream;
    }
    
    public static DataStream<Tuple2<LiveTrainData, PlannedTrainData>> mixed(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<Tuple2<LiveTrainData, PlannedTrainData>> trains = new ArrayList<>();
    
        trains.add(withMatchingPlanned());
        trains.add(withoutMatchingPlanned());
        trains.add(withMatchingPlanned());
        DataStream<Tuple2<LiveTrainData, PlannedTrainData>> liveTrainStream = env.fromCollection(trains, 
            TypeInformation.of(new TypeHint<Tuple2<LiveTrainData, PlannedTrainData>>(){}));

        return liveTrainStream;
    }

    public static Tuple2<LiveTrainData, PlannedTrainData> withMatchingPlanned(){
        return new Tuple2(getDefaultLiveTrainDataEvent(), getDefaultPlannedTrainDataEvent());
    }
    public static Tuple2<LiveTrainData, PlannedTrainData> withoutMatchingPlanned(){
        return new Tuple2(getDefaultLiveTrainDataEvent(), null);
    }
}