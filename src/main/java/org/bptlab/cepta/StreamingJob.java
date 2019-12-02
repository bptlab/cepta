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

import java.util.Properties;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the tutorials and examples on
 * the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run 'mvn clean package' on the
 * command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingJob {

  public static void main(String[] args) throws Exception {
    // set up the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // set properties of the consumer
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", KafkaConstants.KAFKA_BROKERS);
    properties.setProperty("group.id", KafkaConstants.GROUP_ID_CONFIG);

    // create consumer that reads avro data as TrainData objects from topic "test"
    FlinkKafkaConsumer011<PlannedTrainData> consumer = new FlinkKafkaConsumer011<PlannedTrainData>(KafkaConstants.TOPIC_NAME, AvroDeserializationSchema
        .forSpecific(PlannedTrainData.class), properties);

    // add consumer as source for data stream
    DataStream<PlannedTrainData> inputStream = env.addSource(consumer);

    // print stream to console
    inputStream.print();

    env.execute("Flink Streaming Java API Skeleton");
    // insert every event into database table with name actor
    DataStream<PlannedTrainData> plannedTrainDataStream = inputStream.map(new DataToDatabase<PlannedTrainData>("plannedTrainData"));

    env.execute("Flink Streaming Java API Skeleton");
  }
}
