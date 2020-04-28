package org.bptlab.cepta;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.serialization.GenericBinaryProtoDeserializer;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.providers.ReplayerProvider;
import org.bptlab.cepta.containers.ReplayerContainer;
import org.bptlab.cepta.containers.KafkaContainer;
import org.testcontainers.Testcontainers;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.ReplayedEvent;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.QueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.SourceQueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.ReplayOptions;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import java.sql.*;

public class PopulateStationMappingTests {

  private static final boolean debug = false;

  /*@Test
  public void testPopulateStationMappings() throws IOException, InterruptedException {
      KafkaContainer kafka = new KafkaContainer();
      kafka.start();
      ReplayerContainer replayer = ReplayerContainer.forKafka(kafka);
      replayer.start();

      // Start the replayer for the test
      ReplayOptions replayOptions = ReplayOptions.newBuilder().setLimit(100).build();
      SourceQueryOptions gps = SourceQueryOptions.newBuilder().setSource(Topic.GPS_TRIP_UPDATE_DATA).setOptions(replayOptions).build();
      QueryOptions queryOptions = QueryOptions.newBuilder().addAllSources(Arrays.asList(gps)).build();
      Iterator<ReplayedEvent> eventStream = new ReplayerProvider(replayer).query(queryOptions);

      final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

      eventStream.forEachRemaining(e -> System.out.println(e));
  }*/
}
