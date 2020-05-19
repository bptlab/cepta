package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.HashMap;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;

import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;

import org.apache.flink.util.Collector;

import org.bptlab.cepta.utils.triggers.CustomCountTrigger;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;

import org.apache.flink.api.java.functions.KeySelector;

public class EnrichDelayWithCoordinatesFunction {
    
    /* This Function takes an inputstream of DelayNotifications and enriches the events 
    with information about the coordinations of the dedicated station */

    
}