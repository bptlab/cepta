package org.bptlab.cepta;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

import org.bptlab.cepta.utils.functions.StreamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.bptlab.cepta.operators.RemoveDuplicatesFunction;
import org.bptlab.cepta.providers.JavaDataProvider;
import org.bptlab.cepta.providers.LiveTrainDataProvider;

import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;

public class RemoveDuplicatesTests {

    @Test
    public void TestRemoveIntegerDuplicate() throws IOException {
        boolean pass = true;
        DataStream<Integer> integerStream = JavaDataProvider.integerDataStreamWithElement(1);
        RemoveDuplicatesFunction removeDuplicatesFunction = new RemoveDuplicatesFunction<Integer>();
        DataStream<Integer> eliminationStream = removeDuplicatesFunction.removeDuplicates(integerStream,4);

        ArrayList<Integer> removedInteger = StreamUtils.collectStreamToArrayList(eliminationStream);

        // check if remaining elements still have MIN.VALUE and fail if true
        int len = removedInteger.size();
        boolean flag = false;
       
        for (int i = 0; i < len; i++ ) {
            System.out.println(removedInteger.get(i));
            if (removedInteger.get(i) == 1 && !flag) {
                flag = true; 
            } else if (removedInteger.get(i) == 1 && flag) {
                pass = false;
            }
        }
        Assert.assertEquals(3, len);
        Assert.assertTrue(pass);
    }

    @Test
    public void TestRemoveLiveTrainDataDuplicate() throws Exception {
        boolean pass = true;

        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.liveTrainDatStreamWithDuplicates();  
        RemoveDuplicatesFunction removeDuplicatesFunction = new RemoveDuplicatesFunction<LiveTrainData>(); 
    
        DataStream<LiveTrainData> duplicateFreeStream = removeDuplicatesFunction.removeDuplicates(liveTrainDataStream, 3);

        ArrayList<LiveTrainData> duplicateFreeData = StreamUtils.collectStreamToArrayList(duplicateFreeStream);

        // check if duplicates were removed
        int len = duplicateFreeData.size();
        boolean flag = false;
       
        for (int i = 0; i < len; i++ ) {
            if (duplicateFreeData.get(i).equals(LiveTrainDataProvider.trainEventWithTrainSectionId(2)) && !flag) {
                flag = true; 
            } else if (duplicateFreeData.get(i).equals(LiveTrainDataProvider.trainEventWithTrainSectionId(2)) && flag) {
                pass = false;
            }
        }

        Assert.assertEquals(2, len);
        Assert.assertTrue(pass);
    }
}