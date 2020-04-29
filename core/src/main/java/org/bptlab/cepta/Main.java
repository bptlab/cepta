/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bptlab.cepta;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.util.Collector;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.DataCleansingFunction;
import org.bptlab.cepta.serialization.GenericBinaryProtoDeserializer;
import org.bptlab.cepta.serialization.GenericBinaryProtoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Command(
    name = "cepta core",
    mixinStandardHelpOptions = true,
    version = "0.3.1",
    description = "Captures the train events coming from the Kafka queue.")
public class Main implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

  // Consumers
  private FlinkKafkaConsumer011<LiveTrainData> liveTrainDataConsumer;
  private FlinkKafkaConsumer011<PlannedTrainData> plannedTrainDataConsumer;
  private FlinkKafkaConsumer011<WeatherData> weatherDataConsumer;

  // Producer 
  private FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer;


  private void setupConsumers() {
    this.liveTrainDataConsumer =
        new FlinkKafkaConsumer011<LiveTrainData>(
          Topic.LIVE_TRAIN_DATA.getValueDescriptor().getName(),
          new GenericBinaryProtoDeserializer<LiveTrainData>(LiveTrainData.class),
          new KafkaConfig().withClientId("LiveTrainDataMainConsumer").getProperties());

    this.plannedTrainDataConsumer =
        new FlinkKafkaConsumer011<>(
          Topic.PLANNED_TRAIN_DATA.getValueDescriptor().getName(),
            new GenericBinaryProtoDeserializer<PlannedTrainData>(PlannedTrainData.class),
            new KafkaConfig().withClientId("PlannedTrainDataMainConsumer").getProperties());

    this.weatherDataConsumer =
        new FlinkKafkaConsumer011<>(
            Topic.WEATHER_DATA.getValueDescriptor().getName(),
            new GenericBinaryProtoDeserializer<WeatherData>(WeatherData.class),
            new KafkaConfig().withClientId("WeatherDataMainConsumer").getProperties());
  }

  private void setupProducers() {
    KafkaConfig delaySenderConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer")
            .withKeySerializer(Optional.of(LongSerializer::new));
      this.trainDelayNotificationProducer = new FlinkKafkaProducer011<>(
        Topic.DELAY_NOTIFICATIONS.getValueDescriptor().getName(),
        new GenericBinaryProtoSerializer<>(),
        delaySenderConfig.getProperties());
      this.trainDelayNotificationProducer.setWriteTimestampToKafka(true);
  }

  @Mixin
  KafkaConfig kafkaConfig = new KafkaConfig();

  @Mixin
  PostgresConfig postgresConfig = new PostgresConfig();

  @Override
  public Integer call() throws Exception {
    logger.info("Starting CEPTA core...");

    // Setup the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    this.setupConsumers();
    this.setupProducers();

    // Add consumer as source for data stream
    DataStream<PlannedTrainData> plannedTrainDataStream = env.addSource(plannedTrainDataConsumer);
    DataStream<LiveTrainData> liveTrainDataStream = env.addSource(liveTrainDataConsumer);
    DataStream<WeatherData> weatherDataStream = env.addSource(weatherDataConsumer);

    // 1. Transform to one singular stream with hight level event

    /*
    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream =
        AsyncDataStream
            .unorderedWait(liveTrainDataStream, new LivePlannedCorrelationFunction(postgresConfig),
                100000, TimeUnit.MILLISECONDS, 1);

    DataStream<TrainDelayNotification> trainDelayNotificationDataStream = matchedLivePlannedStream
        .flatMap(
            new FlatMapFunction<Tuple2<LiveTrainData, PlannedTrainData>, TrainDelayNotification>() {
              @Override
              public void flatMap(
                  Tuple2<LiveTrainData, PlannedTrainData> liveTrainDataPlannedTrainDataTuple2,
                  Collector<TrainDelayNotification> collector) throws Exception {
                LiveTrainData observed = liveTrainDataPlannedTrainDataTuple2.f0;
                PlannedTrainData expected = liveTrainDataPlannedTrainDataTuple2.f1;

         
          // Delay is defined as the difference between the observed time of a train id at a location id.
          // delay > 0 is bad, the train might arrive later than planned
          // delay < 0 is good, the train might arrive earlier than planned
                try {
                  double delay = observed.getEventTime().getSeconds() - expected.getPlannedEventTime().getSeconds();

                  // Only send a delay notification if some threshold is exceeded
                  if (Math.abs(delay) > 10) {
                    collector.collect(TrainDelayNotification.newBuilder().setDelay(delay)
                        .setTransportId(Transports.TransportID.newBuilder().setId(String.valueOf(observed.getTrainSectionId())))
                        .setLocationId(observed.getStationId())
                        .build());
                  }
                } catch ( NullPointerException e ) {
                  // Do not send a delay event
                }

              }
            });
    */

    // trainDelayNotificationDataStream.addSink(this.trainDelayNotificationProducer);

    env.execute("CEPTA CORE");
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}
