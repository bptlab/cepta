package org.bptlab.cepta;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.bptlab.cepta.serialization.GenericBinaryProtoDeserializer;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.bptlab.cepta.containers.ReplayerContainer;
import org.bptlab.cepta.containers.KafkaContainer;
import org.testcontainers.Testcontainers;
import org.bptlab.cepta.constants.Topics;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import java.sql.*;

public class PopulateStationMappingTests {

  private static final boolean debug = false;

  @Test
  public void testPopulateStationMappings() throws IOException, InterruptedException {
      KafkaContainer kafka = new KafkaContainer();
      kafka.start();
      ReplayerContainer replayer = ReplayerContainer.forKafka(kafka);
      replayer.start();

      final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

      FlinkKafkaConsumer011<PlannedTrainData> plannedTrainDataConsumer =
        new FlinkKafkaConsumer011<>(
          Topics.PLANNED_TRAIN_DATA.getValueDescriptor().getName(),
            new GenericBinaryProtoDeserializer<PlannedTrainData>(PlannedTrainData.class),
            kafka.getConfig().withClientId("PlannedTrainDataMainConsumer").getProperties());

      DataStream<PlannedTrainData> inputStream = env.addSource(plannedTrainDataConsumer);
      Iterator<PlannedTrainData> inputIterator = DataStreamUtils.collect(inputStream);

      ArrayList<PlannedTrainData> received = new ArrayList<>();

      if (this.debug) {
        while (true) {
          TimeUnit.SECONDS.sleep(2);
          final String logs = replayer.getLogs();
          System.out.println(logs);
        }
      }

      // DataStreamUtils.collect(expectedStream).forEachRemaining(expected::add);
      while (inputIterator.hasNext() && received.size() < 1000) {
        received.add(inputIterator.next());
        System.out.println(received.get(received.size() - 1));
      }

      /*
      FlinkKafkaConsumer011<Person> personsKafkaConsumer = new FlinkKafkaConsumer011<Person>(
            topic, deserializer, getProps(kafka));
        personsKafkaConsumer.setStartFromEarliest();
        DataStream<Person> inputStream = expectedStream.getExecutionEnvironment().addSource(personsKafkaConsumer);
        Iterator<Person> inputIterator = DataStreamUtils.collect(inputStream);
        */

      /*
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());
      */

      /*
      DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.unmatchingLiveTrainDatas();
      DataStream<Tuple2<LiveTrainData, PlannedTrainData>> correlatedTrainStream = AsyncDataStream
          .unorderedWait(liveStream, new LivePlannedCorrelationFunction(postgresConfig),
              100000, TimeUnit.MILLISECONDS, 1);

      Iterator<Tuple2<LiveTrainData, PlannedTrainData>> iterator = DataStreamUtils.collect(correlatedTrainStream);
      ArrayList<Tuple2<Long, Long>> correlatedIds = new ArrayList<>();
      while(iterator.hasNext()){
        Tuple2<LiveTrainData, PlannedTrainData> tuple = iterator.next();
        if (tuple.f1 == null){
          correlatedIds.add(new Tuple2<>(tuple.f0.getTrainSectionId(), null));
        } else{
          correlatedIds.add(new Tuple2<>(tuple.f0.getTrainSectionId(), tuple.f1.getTrainSectionId()));
        }
      }
      Assert.assertTrue(correlatedIds.contains(new Tuple2<>(11111111L, null)));
      Assert.assertTrue(correlatedIds.contains(new Tuple2<>(22222222L, null)));
    }
      //Assert.assertTrue(true);
      */
  }
}
