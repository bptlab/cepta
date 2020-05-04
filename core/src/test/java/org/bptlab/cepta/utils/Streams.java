package org.bptlab.cepta.utils;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Streams {

    /**
     * This method creates a new ArrayList of Type T which contains all
     * elements of the inputStream.
     * @param inputStream of Type T
     * @param <T>
     * @return an ArrayList with all objects in inputStream
     * @throws IOException
     */
    public static <T> ArrayList<T> collectStreamToArrayList(DataStream<T> inputStream) throws IOException {
        ArrayList<T> outputCollection = new ArrayList<>();
        Iterator<T> streamIterator = DataStreamUtils.collect(inputStream);
        while (streamIterator.hasNext()) {
            outputCollection.add(streamIterator.next());
        }
        return outputCollection;
    }
}
