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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.connectors.kafka.internal.FlinkKafkaProducer;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.bptlab.cepta.producers.replayer.Empty;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerBlockingStub;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerStub;
import org.bptlab.cepta.producers.replayer.Success;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SuppressWarnings("FieldCanBeLocal")

@Command(
    name = "cepta core",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Captures the train events coming from the Kafka queue.")
public class Main implements Callable<Integer> {

  @Option(
      names = {"-b", "--broker"},
      description = "Specifies the Kafka Broker (ex. localhost:29092).")
  private String kafkaBroker = KafkaConstants.KAFKA_BROKER;

  @Option(
      names = {"-gid", "--group-id"},
      description = "Specifies the Kafka group ID")
  private String kafkaGroupId = KafkaConstants.GROUP_ID_CONFIG;

  @Option(
      names = {"-t", "--topic"},
      description = "Specifies the Kafka topic.")
  private String kafkaTopic = KafkaConstants.TOPIC_NAME;

  @Override
  public Integer call() throws Exception {

    // Setup the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Setup the properties of the consumer
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", kafkaBroker);
    properties.setProperty("group.id", kafkaGroupId);

    // Create consumer that reads avro data as TrainData objects from topic "test"
    FlinkKafkaConsumer011<PlannedTrainData> consumer =
        new FlinkKafkaConsumer011<PlannedTrainData>(
            kafkaTopic, AvroDeserializationSchema.forSpecific(PlannedTrainData.class), properties);

    // Add consumer as source for data stream
    DataStream<PlannedTrainData> inputStream = env.addSource(consumer);
    DataStream<String> trainIDStream = inputStream.map(new MapFunction<PlannedTrainData, String>() {
      @Override
      public String map(PlannedTrainData value) throws Exception {
        return String.format("Train with ID: %d", value.getId());
      }
    });

    // Add consumer for our train id messages
    FlinkKafkaProducer011<String> myProducer = new FlinkKafkaProducer011<>(
        "testTopic", new SimpleStringSchema(), properties);

    myProducer.setWriteTimestampToKafka(true);
    trainIDStream.addSink(myProducer);

    // Print stream to console
    inputStream.print();

    // insert every event into database table with name actor
    DataStream<PlannedTrainData> plannedTrainDataStream = inputStream.map(new DataToDatabase<PlannedTrainData>("plannedTrainData"));

    env.execute("Flink Streaming Java API Skeleton");
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}
