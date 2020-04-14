package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.*;


import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.util.Collector;

import jdk.nashorn.internal.objects.Global;

// CEP packages
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.IterativeCondition.Context;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

import org.apache.flink.api.java.functions.KeySelector;

public class RemoveDuplicatesFunction<T extends Object> {
    
    /*public Pattern<T, ?> duplicates = Pattern.<T>begin("first")
        .followedBy("second").where(
            new IterativeCondition<T>(){
                @Override
                public boolean filter(T event, Context<T> ctx) throws Exception {
                    System.out.println("IN PATTERN EVENT: " + event);
                    
                    /*for (T iteratorEvent : ctx.getEventsForPattern("first")) {
                      
                        return event.equals(iteratorEvent);
                    }*/

                    //return true;
                    //return event.equals(ctx.getEventsForPattern("first").get(0));
                    //return true;
               /* }
            }
        );*/
    
       

    public DataStream<T> removeDuplicates(DataStream<T> inputStream, int windowSize){

        
        //PatternStream<T> patternStream = CEP.pattern(inputStream, duplicates);
        DataStream<T> resultStream = inputStream.keyBy(
            new KeySelector<T, T>(){
                public T getKey(T event){
                    return event;
                } 
            }
        ).countWindow(1).process(new ProcessWindowFunction<T, T, T, GlobalWindow>() {
            @Override
            public void process(T key, Context context, Iterable<T> input, Collector<T> out) throws Exception {
                System.out.println("IN PROCESS");
                out.collect(input.iterator().next());
            }
        });
       
      /*  DataStream<T> resultStream = patternStream.process(new PatternProcessFunction<T,T>(){
            @Override
            public void processMatch(Map<String, List<T>> match, Context ctx, Collector<T> out) throws Exception {
                T startEvent = match.get("first").get(0);
                System.out.println("IN PROCESS MATCH: " + startEvent);
                out.collect(startEvent);
            }
        });
*/
        return resultStream;
    }
} 