package org.bptlab.cepta;

import com.google.protobuf.Timestamp;
import static com.google.protobuf.util.Timestamps.*;

import com.google.protobuf.util.Timestamps;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.operators.CountOfTrainsAtStationFunction;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

public class CountTrainsInStationTests {

    @Test
    public void TestCorrectAmountOfTriggers() throws IOException, ParseException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 1;
        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(2)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData end =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setIngestionTime(Timestamps.parse("2042-01-01T10:00:31.023-05:00")).build();

        DataStream<LiveTrainDataOuterClass.LiveTrainData> liveTrainStream =
                env.fromElements(first, second, end)
                .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        DataStream<Tuple2<Long, Integer>> countOfStationStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainStream);
        ArrayList<Tuple2<Long,Integer>> resultCollection = StreamUtils.collectStreamToArrayList(countOfStationStream);

        /*Expect 4 times cause the function provides a sliding window of one hour every 15 minutes*/
        Assert.assertEquals(4, resultCollection.size());
    }

    @Test
    public void TestCorrectCountAtStation() throws IOException, ParseException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 1;
        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(2)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData end =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setIngestionTime(Timestamps.parse("2042-01-01T10:00:31.023-05:00")).build();

        DataStream<LiveTrainDataOuterClass.LiveTrainData> liveTrainStream =
                env.fromElements(first, second, end)
                .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());


        DataStream<Tuple2<Long, Integer>> countOfStationStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainStream);
        ArrayList<Tuple2<Long,Integer>> resultCollection = StreamUtils.collectStreamToArrayList(countOfStationStream);

        for (Tuple2<Long,Integer> in : resultCollection) {
                Assert.assertEquals(Long.valueOf(1L), in.f0);
                Assert.assertEquals(Integer.valueOf(2), in.f1);
        }
    }

    @Test
    public void TestCountEachStationIndependently() throws IOException, ParseException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 1;
        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation + 1)
                        .setTrainId(2)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData end =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setIngestionTime(Timestamps.parse("2042-01-01T10:00:31.023-05:00")).build();

        DataStream<LiveTrainDataOuterClass.LiveTrainData> liveTrainStream =
                env.fromElements(first, second, end)
                        .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        DataStream<Tuple2<Long, Integer>> countOfStationStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainStream);
        ArrayList<Tuple2<Long,Integer>> resultCollection = StreamUtils.collectStreamToArrayList(countOfStationStream);

        boolean onlyOneTrainPerStation = true;
        for (Tuple2<Long,Integer> in : resultCollection) {
            /*
            check that each window only had one train per station
            with .f1 being the count of the window
           */
            if (in.f1 != 1) {
                onlyOneTrainPerStation = false;
            }
        }

        Assert.assertTrue(onlyOneTrainPerStation);

    }
}