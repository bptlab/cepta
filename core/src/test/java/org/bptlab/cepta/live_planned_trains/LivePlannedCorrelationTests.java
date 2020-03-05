package org.bptlab.cepta.live_planned_trains;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.PlannedTrainData;
import org.bptlab.cepta.WeatherData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore ("Integration tests") public class LivePlannedCorrelationTests {
  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test
  public void testIdMatch() throws IOException {
    DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.matchingLiveTrainDatas();
    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
        .unorderedWait(liveStream, new LivePlannedCorrelationFunction(postgresConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    correlatedTrainStream.print();
    Iterator<Tuple2<LiveTrainData, PlannedTrainData>> iterator = DataStreamUtils.collect(correlatedTrainStream);
    ArrayList<Tuple2<Integer, Integer>> correlatedIds = new ArrayList<>();
    while(iterator.hasNext()){
      tuple = iterator.next()
      correlatedIds.add();
    }
    Assert.assertTrue(correlatedIds.contains(new Tuple2<>(42382923, 42382923)));
    Assert.assertTrue(correlatedIds.contains(new Tuple2<>(42093766, 42093766)));// 42093766
  }

  @Test
  public void testIdUnmatch() throws IOException {
    DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.unmatchingLiveTrainDatas();
    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
        .unorderedWait(liveStream, new LivePlannedCorrelationFunction(postgresConfig),
            100000, TimeUnit.MILLISECONDS, 1);

    correlatedTrainStream.print();
    Iterator<Tuple2<LiveTrainData, PlannedTrainData>> iterator = DataStreamUtils.collect(correlatedTrainStream);
    ArrayList<Tuple2<Integer, Integer>> correlatedIds = new ArrayList<>();
    while(iterator.hasNext()){
      Tuple2<LiveTrainData, PlannedTrainData> tuple = iterator.next();
      if (tuple.f1 == null){
        correlatedIds.add(new Tuple2<>(tuple.f0.getTrainId(), null));
      } else{
        correlatedIds.add(new Tuple2<>(tuple.f0.getTrainId(), tuple.f1.getTrainId()));
      }
    }
    Assert.assertTrue(correlatedIds.contains(new Tuple2<>(11111111, null)));
    Assert.assertTrue(correlatedIds.contains(new Tuple2<>(22222222, null)));// 42093766
  }

}
