package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.bptlab.cepta.models.events.correlatedEvents.CountOfTrainsAtStationEventOuterClass.CountOfTrainsAtStationEvent;
import org.bptlab.cepta.models.events.correlatedEvents.NoMatchingPlannedTrainDataEventOuterClass.NoMatchingPlannedTrainDataEvent;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventOuterClass.StaysInStationEvent;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass.Notification;
import org.bptlab.cepta.models.monitoring.monitor.MonitorOuterClass.Monitor;

enum EventType {EVENT,NOTIFICATION,STAYS,NOMATCH,COUNTOF};

public class MonitorMapFunction<T /*extends Message*/> implements MapFunction<T, Monitor> {

    @Override
    public Monitor map(T t) throws Exception {
        EventType currentType = EventType.EVENT;
        Event embeddedEvent = Event.newBuilder().build();
        Monitor monitorEvent = Monitor.newBuilder().build();

        //Analyse Input
        if (t instanceof LiveTrainData){
            embeddedEvent = Event.newBuilder().setLiveTrain((LiveTrainData) t).build();
        } else if (t instanceof PlannedTrainData) {
            embeddedEvent = Event.newBuilder().setPlannedTrain((PlannedTrainData) t).build();
        } else if (t instanceof Tuple2) {
            if (((Tuple2) t).f0 instanceof LiveTrainData && ((Tuple2) t).f1 instanceof PlannedTrainData) {
                embeddedEvent = Event.newBuilder()
                        .setLiveTrain((LiveTrainData) ((Tuple2) t).f0)
                        .setPlannedTrain((PlannedTrainData) ((Tuple2) t).f1)
                        .build();
            }
        } else if (t instanceof Notification) {
            currentType = EventType.NOTIFICATION;
        } else if (t instanceof StaysInStationEvent) {
            currentType = EventType.STAYS;
        } else if (t instanceof CountOfTrainsAtStationEvent) {
            currentType = EventType.COUNTOF;
        } else if (t instanceof NoMatchingPlannedTrainDataEvent) {
            currentType = EventType.NOMATCH;
        }

        // Construct Output Event
        switch (currentType) {
            case EVENT:
                monitorEvent = Monitor.newBuilder().setEvent(embeddedEvent).build();
                break;
            case COUNTOF:
                monitorEvent = Monitor.newBuilder().setCountOfTrainsAtStationEvent((CountOfTrainsAtStationEvent) t).build();
                break;
            case STAYS:
                monitorEvent = Monitor.newBuilder().setStaysInStationEvent((StaysInStationEvent) t).build();
                break;
            case NOMATCH:
                monitorEvent = Monitor.newBuilder().setNoMatchingPlannedTrainDataEvent((NoMatchingPlannedTrainDataEvent) t).build();
                break;
            case NOTIFICATION:
                monitorEvent = Monitor.newBuilder().setNotification((Notification) t).build();
                break;
        }
        return monitorEvent;
    }
}
