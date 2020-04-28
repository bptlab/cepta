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
import org.bptlab.cepta.operators.DetectStationArrivalDelay;
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
import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass.TrainDelayNotification;
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

  private FlinkKafkaConsumer011<LiveTrainData> liveTrainDataConsumer;
  private FlinkKafkaConsumer011<PlannedTrainData> plannedTrainDataConsumer;
  private FlinkKafkaConsumer011<WeatherData> weatherDataConsumer;

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

    // Add consumer as source for data stream
    DataStream<PlannedTrainData> plannedTrainDataStream = env.addSource(plannedTrainDataConsumer);
    DataStream<LiveTrainData> liveTrainDataStream = env.addSource(liveTrainDataConsumer);
    DataStream<WeatherData> weatherDataStream = env.addSource(weatherDataConsumer);

    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream =
        AsyncDataStream
            .unorderedWait(liveTrainDataStream, new LivePlannedCorrelationFunction(postgresConfig),
                100000, TimeUnit.MILLISECONDS, 1);

    DataStream<TrainDelayNotification> trainDelayNotificationDataStream = matchedLivePlannedStream
        .process(new DetectStationArrivalDelay()).name("train-delays");

    // Produce delay notifications into new queue
    KafkaConfig delaySenderConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer")
        .withKeySerializer(Optional.of(LongSerializer::new));


    FlinkKafkaProducer011<TrainDelayNotification> trainDelayNotificationProducer = new FlinkKafkaProducer011<>(
        Topic.DELAY_NOTIFICATIONS.getValueDescriptor().getName(),
        new GenericBinaryProtoSerializer<TrainDelayNotification>(),
        delaySenderConfig.getProperties());

    trainDelayNotificationProducer.setWriteTimestampToKafka(true);
    trainDelayNotificationDataStream.addSink(trainDelayNotificationProducer);

    // Print stream to console
    // liveTrainDataStream.print();
    trainDelayNotificationDataStream.print();

    //DataStream<PlannedTrainData> plannedTrainDataStream = inputStream.map(new DataToDatabase<PlannedTrainData>("plannedTrainData"));
    weatherDataStream.print();
    plannedTrainDataStream.print();
    env.execute("Flink Streaming Java API Skeleton");
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}
