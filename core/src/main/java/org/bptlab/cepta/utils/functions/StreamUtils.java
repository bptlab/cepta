package org.bptlab.cepta.utils.functions;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class StreamUtils {

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

    /**
     * This method creates a new Vector of Type T which contains all
     * elements of the inputStream.
     * @param inputStream of Type T
     * @param <T>
     * @return a Vector with all objects in inputStream
     * @throws IOException
     */
    public static <T> Vector<T> collectStreamToVector(DataStream<T> inputStream) throws IOException {
        //Apparently, the difference between an ArrayList and a Vector is that the Vector is thread-safe but slower.
        Vector<T> outputCollection = new Vector<>();
        Iterator<T> streamIterator = DataStreamUtils.collect(inputStream);
        while (streamIterator.hasNext()) {
            outputCollection.add(streamIterator.next());
        }
        return outputCollection;
    }

    /**
     * This method will return the count of Objects in a stream.
     * @param inputStream
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> int countOfEventsInStream(DataStream<T> inputStream) throws IOException {
        return collectStreamToVector(inputStream).size();
    }

    /**
     * This method will return the count of matched patterns on the inputStream processed with a processFunction
     * @param inputStream the stream to be processed
     * @param inputPattern the pattern to be applied
     * @param <T>
     * @return
     */
    public static <T> int countOfMatchesInStream(DataStream<T> inputStream, Pattern<T, ?> inputPattern) {
        PatternStream<T> patternStream = CEP.pattern(inputStream, inputPattern);
        //TODO: create an interface for ProcessFunctions so we can add them as a parameter
        return 0;
    }

    public static AscendingTimestampExtractor<LiveTrainDataOuterClass.LiveTrainData> eventTimeExtractor() {
        return new AscendingTimestampExtractor<LiveTrainDataOuterClass.LiveTrainData>() {
            @Override
            public long extractAscendingTimestamp(LiveTrainDataOuterClass.LiveTrainData liveTrainData) {
                // flink timestamps are specified as millisecons since java epoch on 1970-01-01T00:00:00Z.
                return liveTrainData.getEventTime().getSeconds() * 1000;
            }
        };
    }
}
