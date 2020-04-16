package org.bptlab.cepta.operators;

import java.lang.Object;
import java.util.*;


import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.util.Collector;

import jdk.nashorn.internal.objects.Global;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;

import org.bptlab.cepta.utils.triggers.CustomCountTrigger;

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

        inputStream.print();
        //PatternStream<T> patternStream = CEP.pattern(inputStream, duplicates);
        DataStream<T> resultStream = inputStream.keyBy(
            new KeySelector<T, Integer>(){
                Integer key = Integer.valueOf(0);
                public Integer getKey(T event){
                    Integer returnKey = key/windowSize;
                    key++;
                    return returnKey;
                } 
            }
        // countWindow counts the occurences of Keys 
        )
        //.countWindow(2)
        .window(GlobalWindows.create())
        //.window(TumblingProcessingTimeWindows.of(Time.seconds(1)))
        .trigger(
            CustomCountTrigger.of(windowSize)
        )
        .process(new ProcessWindowFunction<T, T, Integer, GlobalWindow>() {
            @Override
            public void process(Integer key, Context context, Iterable<T> input, Collector<T> out) throws Exception {
                System.out.println("IN PROCESS");
               
                LinkedHashSet<T> hashSet = new LinkedHashSet<>();
                for (T in: input) {
                    hashSet.add(in);
                }
                for (T in: hashSet) {
                    out.collect(in);
                }   
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