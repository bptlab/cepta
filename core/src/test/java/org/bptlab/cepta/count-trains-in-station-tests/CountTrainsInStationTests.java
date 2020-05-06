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
    public void TestTriggersOncePerWindow() throws IOException, ParseException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 2;
        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(2)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build()
                ;
        LiveTrainDataOuterClass.LiveTrainData third =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setIngestionTime(Timestamps.parse("1975-01-01T10:00:22.022-05:00")).build()
                ;

        LiveTrainDataOuterClass.LiveTrainData furf =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(3)
                        .setIngestionTime(Timestamps.parse("2042-01-01T10:00:31.023-05:00")).build()
                ;

        DataStream<LiveTrainDataOuterClass.LiveTrainData> liveTrainStream =
                env.fromElements(first, second, third, furf)
                .assignTimestampsAndWatermarks(
                new AscendingTimestampExtractor<LiveTrainDataOuterClass.LiveTrainData>() {
                    @Override
                    public long extractAscendingTimestamp(LiveTrainDataOuterClass.LiveTrainData liveTrainData) {
                        return liveTrainData.getIngestionTime().getSeconds();
                    }
                });


        DataStream<Tuple2<Long, Integer>> countOfStationStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainStream);

      //  countOfStationStream.print();

        ArrayList<Tuple2<Long,Integer>> resultCollection = StreamUtils.collectStreamToArrayList(countOfStationStream);



        Iterator<Tuple2<Long, Integer>> resultIterator = DataStreamUtils.collect(countOfStationStream);

        for (Iterator<Tuple2<Long, Integer>> it = resultIterator; it.hasNext(); ) {
            Tuple2<Long, Integer> e = it.next();
            System.out.println(e);
        }
        Assert.assertEquals(1, resultCollection.size());
    }

    @Test
    public void TestCorrectCountPerStation{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        int commonStation = 2;

        LiveTrainDataOuterClass.LiveTrainData first =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(1)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:20.021-05:00")).build();
        LiveTrainDataOuterClass.LiveTrainData second =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(2)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build()
                ;
        LiveTrainDataOuterClass.LiveTrainData w =
                LiveTrainDataProvider.getDefaultLiveTrainDataEvent().toBuilder()
                        .setStationId(commonStation)
                        .setTrainId(2)
                        .setIngestionTime(Timestamps.parse("1972-01-01T10:00:21.022-05:00")).build()
                ;

    }

}