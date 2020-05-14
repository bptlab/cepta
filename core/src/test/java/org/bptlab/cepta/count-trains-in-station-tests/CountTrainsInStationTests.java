package org.bptlab.cepta;

import com.google.protobuf.util.Timestamps;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.events.correlatedEvents.CountOfTrainsAtStationEventOuterClass.*;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.operators.CountOfTrainsAtStationFunction;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class CountTrainsInStationTests {

    @Test
    public void testCorrectAmountOfTriggers() throws IOException, ParseException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 1;
        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setEventTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(2)
                        .setEventTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData end =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setEventTime(Timestamps.parse("2042-01-01T10:00:31.023-05:00")).build();

        DataStream<LiveTrainDataOuterClass.LiveTrainData> liveTrainStream =
                env.fromElements(first, second, end)
                .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        DataStream<CountOfTrainsAtStationEvent> countOfStationStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainStream);
        ArrayList<CountOfTrainsAtStationEvent> resultCollection = StreamUtils.collectStreamToArrayList(countOfStationStream);

        /*Expect 4 times cause the function provides a sliding window of one hour every 15 minutes*/
        Assert.assertEquals(4, resultCollection.size());
    }

    @Test
    public void testCorrectCountAtStation() throws IOException, ParseException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 1;
        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setEventTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(2)
                        .setEventTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData end =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setEventTime(Timestamps.parse("1972-01-02T10:00:21.022-05:00")).build();

        DataStream<LiveTrainDataOuterClass.LiveTrainData> liveTrainStream =
                env.fromElements(first, second, end)
                .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());


        DataStream<CountOfTrainsAtStationEvent> countOfStationStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainStream);
        ArrayList<CountOfTrainsAtStationEvent> resultCollection = StreamUtils.collectStreamToArrayList(countOfStationStream);

        for (CountOfTrainsAtStationEvent in : resultCollection) {
                Assert.assertEquals(1, in.getStationId());
                Assert.assertEquals(2, in.getCount());
        }
    }

    @Test
    public void testCountEachStationIndependently() throws IOException, ParseException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 1;
        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setEventTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation + 1)
                        .setTrainId(2)
                        .setEventTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData end =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setEventTime(Timestamps.parse("2042-01-01T10:00:31.023-05:00")).build();

        DataStream<LiveTrainDataOuterClass.LiveTrainData> liveTrainStream =
                env.fromElements(first, second, end)
                        .assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

        DataStream<CountOfTrainsAtStationEvent> countOfStationStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainStream);
        ArrayList<CountOfTrainsAtStationEvent> resultCollection = StreamUtils.collectStreamToArrayList(countOfStationStream);

        boolean onlyOneTrainPerStation = true;
        for (CountOfTrainsAtStationEvent in : resultCollection) {
            if (in.getCount()!= 1) {
                onlyOneTrainPerStation = false;
            }
        }

        Assert.assertTrue(onlyOneTrainPerStation);

    }
}