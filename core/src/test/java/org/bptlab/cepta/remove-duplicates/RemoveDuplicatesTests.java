package org.bptlab.cepta;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.bptlab.cepta.operators.RemoveDuplicatesFunction;
import org.bptlab.cepta.providers.JavaDataProvider;
import org.bptlab.cepta.providers.LiveTrainDataProvider;

import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public class RemoveDuplicatesTests {

    @Test
<<<<<<< HEAD
    public void IntegerTest() throws IOException {
        boolean noDuplicate = true;
=======
    public void TestRemoveIntegerDuplicate() throws IOException {
        boolean pass = true;
>>>>>>> origin/dev
        DataStream<Integer> integerStream = JavaDataProvider.integerDataStreamWithElement(1);
        RemoveDuplicatesFunction removeDuplicatesFunction = new RemoveDuplicatesFunction<Integer>();
        DataStream<Integer> remainingStream = removeDuplicatesFunction.removeDuplicates(integerStream,4);  
       
        ArrayList<Integer> remainingIntegers = new ArrayList<>();
        Iterator<Integer> iterator = DataStreamUtils.collect(remainingStream);
        while(iterator.hasNext()){
            Integer integer = iterator.next();
            remainingIntegers.add(integer);
        }
        // check if remaining elements still have MIN.VALUE and fail if true
        int len = remainingIntegers.size();
        boolean flag = false;
       
        for (int i = 0; i < len; i++ ) {
            System.out.println(remainingIntegers.get(i));
            if (remainingIntegers.get(i) == 1 && !flag) {
                flag = true; 
            } else if (remainingIntegers.get(i) == 1 && flag) {
                noDuplicate = false;
            }
        }

        ArrayList<Integer> expectedArray = new ArrayList<Integer>(3){{add(1); add(2); add(3);}};
        Assert.assertEquals(expectedArray, remainingIntegers);
        Assert.assertEquals(3, len);
        Assert.assertTrue(noDuplicate);
    }

    @Test
    public void TestRemoveLiveTrainDataDuplicate() throws Exception {
        boolean pass = true;

        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.liveTrainDatStreamWithDuplicates();  
        RemoveDuplicatesFunction removeDuplicatesFunction = new RemoveDuplicatesFunction<LiveTrainData>(); 
    
        DataStream<LiveTrainData> remainingStream = removeDuplicatesFunction.removeDuplicates(liveTrainDataStream, 3);

        ArrayList<LiveTrainData> remainingData = new ArrayList<>();
        Iterator<LiveTrainData> iterator = DataStreamUtils.collect(remainingStream);
        while(iterator.hasNext()){
            LiveTrainData liveTrainDataValue = iterator.next();
            remainingData.add(liveTrainDataValue);
        }
        // check if duplicates were removed
        int len = remainingData.size();
        boolean flag = false;
       
        for (int i = 0; i < len; i++ ) {
            if (remainingData.get(i).equals(LiveTrainDataProvider.trainEventWithTrainID(2)) && !flag) {
                flag = true; 
            } else if (remainingData.get(i).equals(LiveTrainDataProvider.trainEventWithTrainID(2)) && flag) {
                pass = false;
            }
        }

        Assert.assertEquals(2, len);
        Assert.assertTrue(pass);
    }


    
    
}