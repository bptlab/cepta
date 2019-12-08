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

import java.util.concurrent.Callable;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.constants.KafkaConstants.Topics;
import org.bptlab.cepta.producers.replayer.Success;
import org.bptlab.cepta.schemas.grpc.ReplayerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@SuppressWarnings("FieldCanBeLocal")

@Command(
    name = "cepta core",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Captures the train events coming from the Kafka queue.")
public class Main implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

  @Mixin
  KafkaConfig kafkaConfig = new KafkaConfig();

  @Override
  public Integer call() throws Exception {
    logger.info("Staring cepta core...");

    // Start the replayer
    ReplayerClient test = new ReplayerClient("localhost", 9005);
    Success success = test.start();

    // Setup the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create consumer that reads avro data as TrainData objects from topic "test"
    FlinkKafkaConsumer011<PlannedTrainData> consumer =
        new FlinkKafkaConsumer011<PlannedTrainData>(
            Topics.PLANNED_TRAIN_DATA, AvroDeserializationSchema.forSpecific(PlannedTrainData.class),
            kafkaConfig.withClientId("PlannedTrainDataMainConsumer").getProperties());

    // Add consumer as source for data stream
    DataStream<PlannedTrainData> inputStream = env.addSource(consumer);
    DataStream<String> trainIDStream = inputStream.map(new MapFunction<PlannedTrainData, String>() {
      @Override
      public String map(PlannedTrainData value) throws Exception {
        return String.format("Train with ID: %d", value.getId());
      }
    });

    /* Add consumer for our train id messages
    FlinkKafkaProducer011<String> myProducer = new FlinkKafkaProducer011<>(
        "mytestTopic", new SimpleStringSchema(), kafkaConfig.withClientId("myProducer").getProperties());

    myProducer.setWriteTimestampToKafka(true);
    trainIDStream.addSink(myProducer);*/

    // Print stream to console
    inputStream.print();

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
