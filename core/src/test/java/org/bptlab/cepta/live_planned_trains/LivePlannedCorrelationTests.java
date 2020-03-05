package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LivePlannedCorrelationTests {
  //private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");


  @Test
  public void testIdMatch() throws IOException {
    try(PostgreSQLContainer postgres = new PostgreSQLContainer<>()) {
      postgres.start();

      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword("");

      DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.matchingLiveTrainDatas();
      DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
          .unorderedWait(liveStream, new LivePlannedCorrelationFunction(postgresConfig),
              100000, TimeUnit.MILLISECONDS, 1);

      correlatedTrainStream.print();
      Iterator<Tuple2<LiveTrainData, PlannedTrainData>> iterator = DataStreamUtils.collect(correlatedTrainStream);
      ArrayList<Tuple2<Long, Long>> correlatedIds = new ArrayList<>();
      while(iterator.hasNext()){
        Tuple2<LiveTrainData, PlannedTrainData> tuple = iterator.next();
        if (tuple.f1 == null){
          correlatedIds.add(new Tuple2<>(tuple.f0.getTrainId(), null));
        } else{
          correlatedIds.add(new Tuple2<>(tuple.f0.getTrainId(), tuple.f1.getTrainId()));
        }
      }
      Assert.assertTrue(correlatedIds.contains(new Tuple2<>(42382923L, 42382923L)));
      Assert.assertTrue(correlatedIds.contains(new Tuple2<>(42093766L, 42093766L)));// 42093766
    }
  }
  @Test
  public void testIdUnmatch() throws IOException {
    try(PostgreSQLContainer postgres = new PostgreSQLContainer<>()) {
      postgres.start();
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword("");

      DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.unmatchingLiveTrainDatas();
      DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
          .unorderedWait(liveStream, new LivePlannedCorrelationFunction(postgresConfig),
              100000, TimeUnit.MILLISECONDS, 1);

      correlatedTrainStream.print();
      Iterator<Tuple2<LiveTrainData, PlannedTrainData>> iterator = DataStreamUtils.collect(correlatedTrainStream);
      ArrayList<Tuple2<Long, Long>> correlatedIds = new ArrayList<>();
      while(iterator.hasNext()){
        Tuple2<LiveTrainData, PlannedTrainData> tuple = iterator.next();
        if (tuple.f1 == null){
          correlatedIds.add(new Tuple2<>(tuple.f0.getTrainId(), null));
        } else{
          correlatedIds.add(new Tuple2<>(tuple.f0.getTrainId(), tuple.f1.getTrainId()));
        }
      }
      Assert.assertTrue(correlatedIds.contains(new Tuple2<>(11111111L, null)));
      Assert.assertTrue(correlatedIds.contains(new Tuple2<>(22222222L, null)));// 42093766
    }
  }

}
