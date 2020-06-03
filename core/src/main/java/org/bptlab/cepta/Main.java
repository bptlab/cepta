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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
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
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.operators.*;
import org.bptlab.cepta.models.events.correlatedEvents.CountOfTrainsAtStationEventOuterClass.*;
import org.bptlab.cepta.models.events.correlatedEvents.NoMatchingPlannedTrainDataEventOuterClass.NoMatchingPlannedTrainDataEvent;
import org.bptlab.cepta.models.events.info.LocationDataOuterClass.LocationData;
import org.bptlab.cepta.operators.DetectStationArrivalDelay;
import org.bptlab.cepta.patterns.NoMatchingPlannedTrainDataPattern;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.bptlab.cepta.serialization.GenericBinaryProtoDeserializer;
import org.bptlab.cepta.serialization.GenericBinaryProtoSerializer;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.Mongo.IndexContainer;
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


@Command(
    name = "cepta core",
    mixinStandardHelpOptions = true,
    version = "0.5.0",
    description = "Captures the train events coming from the Kafka queue.")
public class Main implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

  // Consumers
  private FlinkKafkaConsumer011<EventOuterClass.Event> liveTrainDataConsumer;
  private FlinkKafkaConsumer011<EventOuterClass.Event> plannedTrainDataConsumer;
  private FlinkKafkaConsumer011<EventOuterClass.Event> weatherDataConsumer;
  private FlinkKafkaConsumer011<EventOuterClass.Event> locationDataConsumer;

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

    this.locationDataConsumer =
        new FlinkKafkaConsumer011<>(
            Topic.LOCATION_DATA.getValueDescriptor().getName(),
            new GenericBinaryProtoDeserializer<EventOuterClass.Event>(EventOuterClass.Event.class),
            new KafkaConfig().withClientId("LocationDataMainConsumer").getProperties());
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
    DataStream<EventOuterClass.Event> locationDataEvents = env.addSource(locationDataConsumer);

    DataStream<PlannedTrainData> plannedTrainDataStream = plannedTrainDataEvents.map(new MapFunction<EventOuterClass.Event, PlannedTrainData>(){
      @Override
      public PlannedTrainData map(Event event) throws Exception{
        return event.getPlannedTrain();
      }
    }).assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

    DataStream<LiveTrainData> liveTrainDataStream = liveTrainDataEvents.map(new MapFunction<EventOuterClass.Event, LiveTrainData>(){
      @Override
      public LiveTrainData map(Event event) throws Exception{
        return event.getLiveTrain();
      }
    }).assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

    DataStream<WeatherData> weatherDataStream = weatherDataEvents.map(new MapFunction<EventOuterClass.Event, WeatherData>(){
      @Override
      public WeatherData map(Event event) throws Exception{
        return event.getWeather();
      }
    }).assignTimestampsAndWatermarks(StreamUtils.eventTimeExtractor());

    DataStream<LocationData> locationDataStream = locationDataEvents.map(new MapFunction<EventOuterClass.Event, LocationData>(){
      @Override
      public LocationData map(Event event) throws Exception{
        return event.getLocation();
      }
    });

    /*-------------------------
     * End - InputStream Setup
     * ++++++++++++++++++++++++
     * Begin - Output/Consumer Setup
     * ------------------------*/

    ////// TrainDelayNotification Consumer

    // Produce delay notifications into new Kafka queue
    KafkaConfig delaySenderConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer")
            .withKeySerializer(Optional.of(LongSerializer::new));

    FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer = new FlinkKafkaProducer011<>(
            Topic.DELAY_NOTIFICATIONS.getValueDescriptor().getName(),
            new GenericBinaryProtoSerializer<NotificationOuterClass.Notification>(),
            delaySenderConfig.getProperties());

    trainDelayNotificationProducer.setWriteTimestampToKafka(true);

    ////// StaysInStation Consumer

    KafkaConfig staysInStationKafkaConfig = new KafkaConfig().withClientId("StaysInStationProducer")
            .withKeySerializer(Optional.of(LongSerializer::new));

    FlinkKafkaProducer011<StaysInStationEvent> staysInStationProducer = new FlinkKafkaProducer011<>(
            Topic.STAYS_IN_STATION.getValueDescriptor().getName(),
            new GenericBinaryProtoSerializer<>(),
            staysInStationKafkaConfig.getProperties());

    staysInStationProducer.setWriteTimestampToKafka(true);

    /*-------------------------
     * End - Output/Consumer Setup
     * ++++++++++++++++++++++++
     * Begin - Weather/Locations
     * ------------------------*/
    //Instruct to create an Index to increase performance of queries after Insertion
    List<IndexContainer> locationIndex = Mongo.makeIndexContainerList(Arrays.asList("latitude","longitude"));

    //The Stream is not necessary it passes through all events independent from a successful upload
    DataStream<LocationData> uploadedLocationStream = AsyncDataStream
            .unorderedWait(locationDataStream, new DataToMongoDB<LocationData>("location",locationIndex,mongoConfig),
                    100000, TimeUnit.MILLISECONDS, 1);

    DataStream<Tuple2<WeatherData, Long>> weatherLocationStream = AsyncDataStream
            .unorderedWait(weatherDataStream, new WeatherLocationCorrelationMongoFunction("location",mongoConfig),
                    100000, TimeUnit.MILLISECONDS, 1);

    //this is a bit weird compared to the other operators
    DataStream<NotificationOuterClass.Notification> delayFromWeatherStream = WeatherLiveTrainJoinFunction.delayFromWeather(weatherLocationStream,liveTrainDataStream);

    delayFromWeatherStream.addSink(trainDelayNotificationProducer);
//    delayFromWeatherStream.print();
    /*-------------------------
     * End - Weather
     * ++++++++++++++++++++++++
     * Begin - MongoDelayShift
     * ------------------------*/
    List<IndexContainer> plannedTrainDataIndex = Mongo.makeIndexContainerList(Arrays.asList("trainSectionId","endStationId","plannedArrivalTimeEndStation","plannedEventTime"));
    //The Stream is not necessary it passes through all events independent from a successful upload
    DataStream<PlannedTrainData> plannedTrainDataStreamUploaded = AsyncDataStream
      .unorderedWait(plannedTrainDataStream, new DataToMongoDB("plannedTrainData",plannedTrainDataIndex, mongoConfig),
        100000, TimeUnit.MILLISECONDS, 1);

    DataStream<Notification> notificationFromDelayShift = AsyncDataStream
            .unorderedWait(liveTrainDataStream, new DelayShiftFunctionMongo(mongoConfig),
                    100000, TimeUnit.MILLISECONDS, 1);

//    notificationFromDelayShift.print();
//    notificationFromDelayShift.addSink(trainDelayNotificationProducer);

    DataStream<Notification> notificationFromDelayShiftenriched = notificationFromDelayShift.flatMap(new EnrichDelayWithCoordinatesFunction(mongoConfig));
    //notificationFromDelayShiftenriched.print();
    notificationFromDelayShiftenriched.addSink(trainDelayNotificationProducer);
    /*-------------------------
     * End - MongoDelayShift
     * ++++++++++++++++++++++++
     * Begin - StaysInStation
     * ------------------------*/

    DataStream<StaysInStationEvent> staysInStationEventDataStream =
            CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern)
            .process(StaysInStationPattern.staysInStationProcessFunction());

    staysInStationEventDataStream.addSink(staysInStationProducer);

    /*-------------------------
     * End - StaysInStation
     * ++++++++++++++++++++++++
     * Begin - CountOfTrainsAtStation
     * ------------------------*/

    DataStream<CountOfTrainsAtStationEvent> countOfTrainsAtStationDataStream = CountOfTrainsAtStationFunction.countOfTrainsAtStation(liveTrainDataStream);

//    countOfTrainsAtStationDataStream.print();

//    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream =
//        AsyncDataStream
//            .unorderedWait(liveTrainDataStream, new LivePlannedCorrelationFunction(postgresConfig),
//                100000, TimeUnit.MILLISECONDS, 1);

    // LivePlannedCorrelationFunction Mongo
    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream = AsyncDataStream
            .unorderedWait(liveTrainDataStream, new LivePlannedCorrelationFunctionMongo( mongoConfig),
                    100000, TimeUnit.MILLISECONDS, 1);

    // DetectStationArrivalDelay
    DataStream<NotificationOuterClass.Notification> trainDelayNotificationDataStream = matchedLivePlannedStream
        .process(new DetectStationArrivalDelay()).name("train-delays");


//    trainDelayNotificationDataStream.addSink(trainDelayNotificationProducer);
    DataStream<NotificationOuterClass.Notification> trainDelayNotificationsWithCoordinates = trainDelayNotificationDataStream.flatMap(new EnrichDelayWithCoordinatesFunction(mongoConfig) );
    //trainDelayNotificationDataStream.print();
    trainDelayNotificationsWithCoordinates.addSink(trainDelayNotificationProducer);
    trainDelayNotificationsWithCoordinates.print();
    // NoMatchingPlannedTrainDataPattern

    PatternStream<Tuple2<LiveTrainData, PlannedTrainData>> patternStream = CEP.pattern(
            matchedLivePlannedStream, NoMatchingPlannedTrainDataPattern.noMatchingPlannedTrainDataPattern());

    //TODO Decide about the other 3 specialised patterns?

    DataStream<NoMatchingPlannedTrainDataEvent> noMatchingPlannedTrainDataEventDataStream =
            patternStream.process(NoMatchingPlannedTrainDataPattern.generateNMPTDEventsFunc());

    //TODO add consumer for these Events
    /*-------------------------
     * End - matchedLivePlanned
     * ++++++++++++++++++++++++
     * Begin - SumOfDelaysAtStation
     * ------------------------*/
    //TODO Decided about input (Stream and events Notification VS DelayNotification) and Window
    int sumOfDelayWindow = 4;
    DataStream<Tuple2<Long, Long>> sumOfDelayAtStationStream = SumOfDelayAtStationFunction.sumOfDelayAtStation(trainDelayNotificationDataStream, sumOfDelayWindow );

    //TODO Make Sink/Producer
//    sumOfDelayAtStationStream.print();
    /*-------------------------
     * End - SumOfDelaysAtStation
     * ++++++++++++++++++++++++
     * Begin - Execution
     * ------------------------*/

    env.execute("CEPTA CORE");
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}
