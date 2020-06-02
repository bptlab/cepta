package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.*;

import org.apache.flink.util.Collector;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;

import org.bptlab.cepta.utils.triggers.CustomCountTrigger;

import org.apache.flink.api.java.functions.KeySelector;

public class RemoveDuplicatesFunction<T extends Object> {

    public DataStream<T> removeDuplicates(DataStream<T> inputStream, int windowSize){
        /**
         * removes the duplicates in a stream using a tumbling event number window 
         * the window's size is given by the parameter windowSize
         */
        
        DataStream<T> resultStream = inputStream.keyBy(
            new KeySelector<T, Integer>(){
                Integer key = Integer.valueOf(0);
                public Integer getKey(T event){
                    Integer returnKey = key/windowSize;
                    key++;
                    return returnKey;
                } 
            }
        )
        .window(GlobalWindows.create())
        .trigger(
            CustomCountTrigger.of(windowSize)
        )
        .process(new ProcessWindowFunction<T, T, Integer, GlobalWindow>() {
            @Override
            public void process(Integer key, Context context, Iterable<T> input, Collector<T> out) throws Exception {
                LinkedHashSet<T> hashSet = new LinkedHashSet<>();
                for (T in: input) {
                    hashSet.add(in);
                }
                for (T in: hashSet) {
                    out.collect(in);
                }   
            }
        });

        return resultStream;
    }
} 