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

public class DuplicateEliminationTests {

    @Test
    public void Test() throws IOException {
        boolean pass = true;
        DataStream<Integer> integerStream = JavaDataProvider.integerDataStreamWithElement(1);
        RemoveDuplicatesFunction removeDuplicatesFunction = new RemoveDuplicatesFunction<Integer>();
        DataStream<Integer> eliminationStream = removeDuplicatesFunction.removeDuplicates(integerStream,10);  
       
        ArrayList<Integer> removedInteger = new ArrayList<>();
        Iterator<Integer> iterator = DataStreamUtils.collect(eliminationStream);
        while(iterator.hasNext()){
            Integer integer = iterator.next();
            removedInteger.add(integer);
        }
        // check if remaining elements still have MIN.VALUE and fail if true
        int len = removedInteger.size();
        boolean flag = false;
       
        for (int i = 0; i < len; i++ ) {
            if (removedInteger.get(i) == 1 && !flag) {
                flag = true; 
            } else if (removedInteger.get(i) == 1 && flag) {
                pass = false;
            }
        }
        Assert.assertEquals(3, len);
        Assert.assertTrue(false);
    }

    
    
}