package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.RichFlatMapFunction;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;

import org.bptlab.cepta.models.internal.delay.DelayOuterClass;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.utils.converters.PlannedTrainDataDatabaseConverter;
import org.bptlab.cepta.utils.notification.NotificationHelper;


public class BA_State_Live extends RichFlatMapFunction<LiveTrainData, NotificationOuterClass.Notification> {

    private transient ValueState<Integer> current_station;

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void flatMap(LiveTrainData liveEvent, Collector<NotificationOuterClass.Notification> out) throws Exception {
    }

    }
