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
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.formats.avro.AvroRowSerializationSchema;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.bptlab.cepta.config.constants.KafkaConstants.Topics;
import org.bptlab.cepta.operators.PlannedLiveCorrelationFunction;
import org.bptlab.cepta.serialization.AvroBinarySerializer;
import org.bptlab.cepta.serialization.AvroBinaryFlinkSerializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "cepta core",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Captures the train events coming from the Kafka queue.")
public class Main implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

  @Mixin
  KafkaConfig kafkaConfig = new KafkaConfig();

  @Mixin
  PostgresConfig postgresConfig = new PostgresConfig();

  @Override
  public Integer call() throws Exception {
    logger.info("Starting cepta core...");

    // Setup the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    FlinkKafkaConsumer011<LiveTrainData> liveTrainDataConsumer =
        new FlinkKafkaConsumer011<>(
            Topics.LIVE_TRAIN_DATA, AvroDeserializationSchema.forSpecific(LiveTrainData.class),
            new KafkaConfig().withClientId("LiveTrainDataMainConsumer").getProperties());

    /*
    FlinkKafkaConsumer011<PlannedTrainData> plannedTrainDataConsumer =
        new FlinkKafkaConsumer011<>(
            Topics.PLANNED_TRAIN_DATA,
            AvroDeserializationSchema.forSpecific(PlannedTrainData.class),
            new KafkaConfig().getProperties());
    */

    // .withClientId("PlannedTrainDataMainConsumer")
    // System.out.print(liveTrainDataConsumer.client.id);

    // Add consumer as source for data stream
    // DataStream<PlannedTrainData> plannedTrainDataStream = env.addSource(plannedTrainDataConsumer);
    DataStream<LiveTrainData> liveTrainDataStream = env.addSource(liveTrainDataConsumer);

    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream =
        AsyncDataStream
            .unorderedWait(liveTrainDataStream, new PlannedLiveCorrelationFunction(postgresConfig),
                100000, TimeUnit.MILLISECONDS, 12);

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

                long delay = observed.getActualTime() - expected.getPlannedTime();
                // Only send a delay notification if some threshold is exceeded
                if (Math.abs(delay) > 10) {
                  collector.collect(TrainDelayNotification.newBuilder().setDelay(delay)
                      .setTrainId(observed.getTrainId()).setLocationId(observed.getLocationId())
                      .build());
                }
              }
            });

    // Produce delay notifications into new queue
    KafkaConfig delaySenderConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer")
        .withKeySerializer(Optional.of(LongSerializer::new));
    FlinkKafkaProducer011<TrainDelayNotification> trainDelayNotificationProducer = new FlinkKafkaProducer011<>(
        KafkaConstants.Topics.DELAY_NOTIFICATIONS, new AvroBinaryFlinkSerializationSchema<>(),
        delaySenderConfig.getProperties());

    trainDelayNotificationProducer.setWriteTimestampToKafka(true);
    trainDelayNotificationDataStream.addSink(trainDelayNotificationProducer);

    // Print stream to console
    trainDelayNotificationDataStream.print();

    // insert every event into database table with name actor
    // DataStream<PlannedTrainData> plannedTrainDataStream = inputStream.map(new DataToDatabase<PlannedTrainData>("plannedTrainData"));

    env.execute("Flink Streaming Java API Skeleton");
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}
