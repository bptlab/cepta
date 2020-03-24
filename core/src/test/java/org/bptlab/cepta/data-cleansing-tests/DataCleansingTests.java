package org.bptlab.cepta;

import java.util.ArrayList;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import java.util.Iterator;
import jdk.internal.jline.internal.TestAccessible;
import sun.security.util.Length;

import org.junit.Ignore;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.operators.DataCleansingFunction;
import org.bptlab.cepta.providers.JavaDataProvider;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;

public class DataCleansingTests {

    @Test
    public void TestIntegerCleansing() throws IOException {
        boolean pass = true;
        // get Stream of Integers with an element with the value of Integer.MIN_VALUE
        DataStream<Integer> integerStream = JavaDataProvider.integerDataStreamWithElement(Integer.MIN_VALUE);  
        // cleanse all Integer.MIN_VALUE elements from our Stream
        DataStream<Integer> cleansedStream = DataCleansingFunction.cleanseStream(integerStream, Integer.MIN_VALUE);
        // add all remaining elements of the Stream in an ArrayList
        ArrayList<Integer> cleansedInteger = new ArrayList<>();
        Iterator<Integer> iterator = DataStreamUtils.collect(cleansedStream);
        while(iterator.hasNext()){
            Integer integer = iterator.next();
            cleansedInteger.add(integer);
        }
        // check if remaining elements still have MIN.VALUE and fail if true
        int len = cleansedInteger.size();
        for (int i = 0; i < len; i++ ) {
            if (cleansedInteger.get(i) == Integer.MIN_VALUE) {
                System.out.println("Failed cause MIN VALUE exists");
                pass = false; 
            }
        }
        Assert.assertTrue(pass);
    }

    @Test
    public void TestStringCleansing() throws IOException {
        boolean pass = true;
        // get Stream of Integers with an element with the value of Integer.MIN_VALUE
        DataStream<String> integerStream = JavaDataProvider.integerDataStreamWithElement(Integer.MIN_VALUE);  
        // cleanse all Integer.MIN_VALUE elements from our Stream
        DataStream<String> cleansedStream = DataCleansingFunction.cleanseStream(integerStream, Integer.MIN_VALUE);
        // add all remaining elements of the Stream in an ArrayList
        ArrayList<Integer> cleansedInteger = new ArrayList<>();
        Iterator<Integer> iterator = DataStreamUtils.collect(cleansedStream);
        while(iterator.hasNext()){
            Integer integer = iterator.next();
            cleansedInteger.add(integer);
        }
        // check if remaining elements still have MIN.VALUE and fail if true
        int len = cleansedInteger.size();
        for (int i = 0; i < len; i++ ) {
            if (cleansedInteger.get(i) == Integer.MIN_VALUE) {
                System.out.println("Failed cause MIN VALUE exists");
                pass = false; 
            }
        }
        Assert.assertTrue(pass);
    }

}