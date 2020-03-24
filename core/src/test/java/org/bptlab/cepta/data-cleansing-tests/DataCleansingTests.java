package org.bptlab.cepta;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.operators.DataCleansingFunction;
import org.bptlab.cepta.providers.JavaDataProvider;
import sun.security.util.Length;

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
        // get Stream of String with an element with the value of "Test"
        DataStream<String> stringStream = JavaDataProvider.stringDataStreamWithElement("Test");  
        // cleanse all "Test" elements from our Stream
        DataStream<String> cleansedStream = DataCleansingFunction.cleanseStream(stringStream, "Test");
        // add all remaining elements of the Stream in an ArrayList
        ArrayList<String> cleansedStrings = new ArrayList<>();
        Iterator<String> iterator = DataStreamUtils.collect(cleansedStream);
        while(iterator.hasNext()){
            String integer = iterator.next();
            cleansedStrings.add(integer);
        }
        // check if remaining elements still have "Test" and fail if true
        int len = cleansedStrings.size();
        for (int i = 0; i < len; i++ ) {
            if (cleansedStrings.get(i).equals("Test")) {
                System.out.println("Failed cause MIN VALUE exists");
                pass = false; 
            }
        }
        Assert.assertTrue(pass);
    }

    @Test
    public void TestDoubleCleansing() throws IOException {
        boolean pass = true;
        // get Stream of Doubles with an element with the value of Double.MAX_VALUE
        DataStream<Double> stringStream = JavaDataProvider.doubleDataStreamWithElement(Double.MAX_VALUE);  
        // cleanse all Double.MAX_VALUE elements from our Stream
        DataStream<Double> cleansedStream = DataCleansingFunction.cleanseStream(stringStream, Double.MAX_VALUE);
        // add all remaining elements of the Stream in an ArrayList
        ArrayList<Double> cleansedDoubles = new ArrayList<>();
        Iterator<Double> iterator = DataStreamUtils.collect(cleansedStream);
        while(iterator.hasNext()){
            Double doubleValue = iterator.next();
            cleansedDoubles.add(doubleValue);
        }
        // check if remaining elements still have Double.MAX_VALUE and fail if true
        int len = cleansedDoubles.size();
        for (int i = 0; i < len; i++ ) {
            if (cleansedDoubles.get(i).equals(Double.MAX_VALUE)) {
                System.out.println("Failed cause MIN VALUE exists");
                pass = false; 
            }
        }
        Assert.assertTrue(pass);
    }

    @Test
    public void TestLongCleansing() throws IOException {
        boolean pass = true;
        // get Stream of Longs with an element with the value of Long.MAX_VALUE
        DataStream<Long> longStream = JavaDataProvider.longDataStreamWithElement(Long.MAX_VALUE);  
        // cleanse all Long.MAX_VALUE elements from our Stream
        DataStream<Long> cleansedStream = DataCleansingFunction.cleanseStream(longStream, Long.MAX_VALUE);
        // add all remaining elements of the Stream in an ArrayList
        ArrayList<Long> cleansedLongs = new ArrayList<>();
        Iterator<Long> iterator = DataStreamUtils.collect(cleansedStream);
        while(iterator.hasNext()){
            Long longValue = iterator.next();
            cleansedLongs.add(longValue);
        }
        // check if remaining elements still have Long.MAX_VALUE and fail if true
        int len = cleansedLongs.size();
        for (int i = 0; i < len; i++ ) {
            if (cleansedLongs.get(i).equals(Long.MAX_VALUE)) {
                System.out.println("Failed cause MIN VALUE exists");
                pass = false; 
            }
        }
        Assert.assertTrue(pass);
    }

    @Test
    public void TestBooleanCleansing() throws IOException {
        boolean pass = true;
        // get Stream of Bools 
        DataStream<Boolean> booleanStream = JavaDataProvider.booleanDataStream();  
        // cleanse all false elements from our Stream
        DataStream<Boolean> cleansedStream = DataCleansingFunction.cleanseStream(booleanStream, false);
        // add all remaining elements of the Stream in an ArrayList
        ArrayList<Boolean> cleansedBooleans = new ArrayList<>();
        Iterator<Boolean> iterator = DataStreamUtils.collect(cleansedStream);
        while(iterator.hasNext()){
            Boolean booleanValue = iterator.next();
            cleansedBooleans.add(booleanValue);
        }
        // check if remaining elements still have false and fail if true
        int len = cleansedBooleans.size();
        for (int i = 0; i < len; i++ ) {
            if (cleansedBooleans.get(i).equals(false)) {
                System.out.println("Failed cause false exists");
                pass = false; 
            }
        }
        Assert.assertTrue(pass);
    }

}