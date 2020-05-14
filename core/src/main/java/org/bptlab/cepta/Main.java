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
import org.apache.flink.cep.CEP;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.events.correlatedEvents.CountOfTrainsAtStationEventOuterClass.*;
import org.bptlab.cepta.operators.DelayShiftFunction;
import org.bptlab.cepta.operators.DetectStationArrivalDelay;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.operators.*;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.bptlab.cepta.serialization.GenericBinaryProtoDeserializer;
import org.bptlab.cepta.serialization.GenericBinaryProtoSerializer;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import org.bptlab.cepta.operators.CountOfTrainsAtStationFunction;
import org.bptlab.cepta.models.events.event.EventOuterClass;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass.Notification;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventOuterClass.StaysInStationEvent;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Command(
    name = "cepta core",
    mixinStandardHelpOptions = true,
    version = "0.4.0",
    description = "Captures the train events coming from the Kafka queue.")
public class Main implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

  // Consumers
  private FlinkKafkaConsumer011<EventOuterClass.Event> liveTrainDataConsumer;
  private FlinkKafkaConsumer011<EventOuterClass.Event> plannedTrainDataConsumer;
  private FlinkKafkaConsumer011<EventOuterClass.Event> weatherDataConsumer;

  // Producer 
  private FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer;

  private void setupConsumers() {
    this.liveTrainDataConsumer =
        new FlinkKafkaConsumer011<>(
          Topic.LIVE_TRAIN_DATA.getValueDescriptor().getName(),
          new GenericBinaryProtoDeserializer<EventOuterClass.Event>(EventOuterClass.Event.class),
          new KafkaConfig().withClientId("LiveTrainDataMainConsumer").getProperties());
    this.plannedTrainDataConsumer =
        new FlinkKafkaConsumer011<>(
          Topic.PLANNED_TRAIN_DATA.getValueDescriptor().getName(),
            new GenericBinaryProtoDeserializer<EventOuterClass.Event>(EventOuterClass.Event.class),
            new KafkaConfig().withClientId("PlannedTrainDataMainConsumer").getProperties());

    this.weatherDataConsumer =
        new FlinkKafkaConsumer011<>(
            Topic.WEATHER_DATA.getValueDescriptor().getName(),
            new GenericBinaryProtoDeserializer<EventOuterClass.Event>(EventOuterClass.Event.class),
            new KafkaConfig().withClientId("WeatherDataMainConsumer").withGroupID("Group").getProperties());
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

  @Mixin
  MongoConfig mongoConfig = new MongoConfig();

  @Override
  public Integer call() throws Exception {
    logger.info("Starting CEPTA core...");

    // Setup the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    this.setupConsumers();
    this.setupProducers();

    /*-------------------------
     * End - StreamExecution Environment Setup
     * ++++++++++++++++++++++++
     * Begin - InputStream Setup
     * ------------------------*/

    DataStream<EventOuterClass.Event> plannedTrainDataEvents = env.addSource(plannedTrainDataConsumer);
    DataStream<EventOuterClass.Event> liveTrainDataEvents = env.addSource(liveTrainDataConsumer);
    DataStream<EventOuterClass.Event> weatherDataEvents = env.addSource(weatherDataConsumer);

    DataStream<PlannedTrainData> plannedTrainDataStream = plannedTrainDataEvents.map(new MapFunction<EventOuterClass.Event, PlannedTrainData>(){
      @Override
      public PlannedTrainData map(Event event) throws Exception{
        return event.getPlannedTrain();
      }
    });
    DataStream<LiveTrainData> liveTrainDataStream = liveTrainDataEvents.map(new MapFunction<EventOuterClass.Event, LiveTrainData>(){
      @Override
      public LiveTrainData map(Event event) throws Exception{
        return event.getLiveTrain();
      }
    });

    DataStream<LiveTrainData> liveTrainDataStream2 = liveTrainDataStream.assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());
    // liveTrainDataStream.print();

    /*-------------------------
     * End - InputStream Setup
     * ++++++++++++++++++++++++
     * Begin - Weather
     * ------------------------*/

    DataStream<WeatherData> weatherDataStream = weatherDataEvents.map(new MapFunction<EventOuterClass.Event, WeatherData>(){
      @Override
      public WeatherData map(Event event) throws Exception{
        return event.getWeather();
      }
    });

    /*-------------------------
     * End - Weather
     * ++++++++++++++++++++++++
     * Begin - StaysInStation
     * ------------------------*/

    DataStream<Notification> notificationFromDelayShift = AsyncDataStream
    .unorderedWait(liveTrainDataStream, new DelayShiftFunctionMongo(mongoConfig),
      100000, TimeUnit.MILLISECONDS, 1);

    //The Stream is not necessary it passes through all events independent from a successfull upload
    DataStream<PlannedTrainData> plannedTrainDataStreamUploaded = AsyncDataStream
      .unorderedWait(plannedTrainDataStream, new DataToMongoDB("plannedTrainData", mongoConfig),
        100000, TimeUnit.MILLISECONDS, 1);

    /*-------------------------
     * End - MongoDelayShift
     * ++++++++++++++++++++++++
     * Begin - StaysInStation
     * ------------------------*/

    DataStream<StaysInStationEvent> staysInStationEventDataStream =
            CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern)
            .process(StaysInStationPattern.staysInStationProcessFunction());

    KafkaConfig staysInStationKafkaConfig = new KafkaConfig().withClientId("StaysInStationProducer")
            .withKeySerializer(Optional.of(LongSerializer::new));

    FlinkKafkaProducer011<StaysInStationEvent> staysInStationProducer = new FlinkKafkaProducer011<>(
            Topic.STAYS_IN_STATION.getValueDescriptor().getName(),
            new GenericBinaryProtoSerializer<>(),
            staysInStationKafkaConfig.getProperties());

    staysInStationEventDataStream.addSink(staysInStationProducer);

    /*-------------------------
     * End - StaysInStation
     * ++++++++++++++++++++++++
     * Begin - CountOfTrainsAtStation
     * ------------------------*/

    DataStream<CountOfTrainsAtStationEvent> countOfTrainsAtStationDataStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainDataStream2);

    countOfTrainsAtStationDataStream.print();

    /*-------------------------
     * End - CountOfTrainsAtStation
     * ++++++++++++++++++++++++
     * Begin - matchedLivePlanned
     * ------------------------*/

    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream =
        AsyncDataStream
            .unorderedWait(liveTrainDataStream, new LivePlannedCorrelationFunction(postgresConfig),
                100000, TimeUnit.MILLISECONDS, 1);

    DataStream<NotificationOuterClass.Notification> trainDelayNotificationDataStream = matchedLivePlannedStream
        .process(new DetectStationArrivalDelay()).name("train-delays");

    // Produce delay notifications into new queue
     KafkaConfig delaySenderConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer")
        .withKeySerializer(Optional.of(LongSerializer::new));

    FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer = new FlinkKafkaProducer011<>(
        Topic.DELAY_NOTIFICATIONS.getValueDescriptor().getName(),
        new GenericBinaryProtoSerializer<NotificationOuterClass.Notification>(),
        delaySenderConfig.getProperties());

    trainDelayNotificationProducer.setWriteTimestampToKafka(true);
    trainDelayNotificationDataStream.addSink(trainDelayNotificationProducer);
    trainDelayNotificationDataStream.print();

    /*-------------------------
     * End - matchedLivePlanned
     * ++++++++++++++++++++++++
     * Begin - !
     * ------------------------*/

    /*delayShiftNotifications.addSink(trainDelayNotificationProducer);
    delayShiftNotifications.print();

     trainDelayNotificationDataStream.print();
    delayShiftNotifications.addSink(trainDelayNotificationProducer);
     delayShiftNotifications.print();*/



    env.execute("CEPTA CORE");
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}
