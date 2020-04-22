package org.bptlab.cepta.operators;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;

import java.util.*;


public class CountOfEventsInStreamFunction <T extends Object> {

    public static <T> int countOfEventsInStream(DataStream<T> input) throws Exception{
        int count = 0;
        Iterator<T> inputIterator = DataStreamUtils.collect(input);
        while(inputIterator.hasNext()){
            T event = inputIterator.next();
            count ++;
        }
        return count;
    }

}