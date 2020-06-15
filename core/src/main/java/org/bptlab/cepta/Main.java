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

import com.google.protobuf.Timestamp;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.serialization.GenericBinaryProtoDeserializer;
import org.bptlab.cepta.serialization.GenericBinaryProtoSerializer;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import org.bptlab.cepta.models.events.event.EventOuterClass;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;

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

    /*-------------------------
     * End - InputStream Setup
     * ++++++++++++++++++++++++
     * Begin - Gritta BA Kram
     * ------------------------*/

    //liveTrainDataStream.print();
    //plannedTrainDataStream.print();

    DataStream<NotificationOuterClass.DelayNotification> delays = liveTrainDataStream
        .connect(plannedTrainDataStream)
        .keyBy(new LiveTrainIdKeySelector(), new PlannedTrainIdKeySelector(), TypeInformation.of(Long.class))
        .process(new KeyedCoProcessFunction<Long, LiveTrainData, PlannedTrainData, NotificationOuterClass.DelayNotification>(){

          public transient MapState<Long, Timestamp> stationsWithTimestampsState;
          public transient ValueState<TreeSet<StationWithTimestamp>> stationsOrderState; // ordered list representing the stations' order
          public transient ValueState<Long> delayState;
          public transient ValueState<Long> currentStationState;

          @Override
          public void open(Configuration config){
            // set up the states

            ValueStateDescriptor<TreeSet<StationWithTimestamp>> stationsOrderStateDesc = new ValueStateDescriptor<TreeSet<StationWithTimestamp>>(
                "StationsOrderState",
                TypeInformation.of(new TypeHint<TreeSet<StationWithTimestamp>>() { }));
            stationsOrderState = getRuntimeContext().getState(stationsOrderStateDesc);

            ValueStateDescriptor<Long> delayStateDesc = new ValueStateDescriptor<Long>(
                "DelayState",
                TypeInformation.of(Long.class));
            delayState = getRuntimeContext().getState(delayStateDesc);

            ValueStateDescriptor<Long> currentStationStateDesc = new ValueStateDescriptor<Long>(
                "CurrentStationState",
                TypeInformation.of(Long.class));
            currentStationState = getRuntimeContext().getState(currentStationStateDesc);

            MapStateDescriptor<Long, Timestamp> stationsWithTimestampsStateDesc = new MapStateDescriptor<Long, Timestamp>(
                "StationsWithTimestampsState",
                TypeInformation.of(Long.class), TypeInformation.of(Timestamp.class));
            stationsWithTimestampsState = getRuntimeContext().getMapState(stationsWithTimestampsStateDesc);
          }

          @Override
          public void processElement1(LiveTrainData liveTrainData, Context context, Collector<NotificationOuterClass.DelayNotification> collector) throws Exception {
            /*
             * retrieve stuff from state
             * update state
             */
            Long delay = 0L;
            try{
              System.out.println(stationsWithTimestampsState.get(liveTrainData.getStationId()));
              delay = stationsWithTimestampsState.get(liveTrainData.getStationId()).getSeconds() - liveTrainData.getEventTime().getSeconds();
            }catch (NullPointerException e){
              // there is no corresponding planned data available
            }
            delayState.update(delay);
            currentStationState.update(liveTrainData.getStationId());
          }
          @Override
          public void processElement2(PlannedTrainData plannedTrainData,
                                      Context context,
                                      Collector<NotificationOuterClass.DelayNotification> collector)
              throws Exception {

            // add entry to state or update earlier value
            stationsWithTimestampsState.put(plannedTrainData.getStationId(), plannedTrainData.getPlannedEventTime());
            try{
              stationsOrderState.value().add(new StationWithTimestamp(plannedTrainData.getStationId(), plannedTrainData.getPlannedEventTime()));}
            catch (NullPointerException e){
              //e.printStackTrace();
              stationsOrderState.update(new TreeSet<StationWithTimestamp>());
            }
          }
        });

    /*-------------------------
     * End - Gritta BA Kram
     * ++++++++++++++++++++++++
     * Begin - Output/Consumer Setup
     * ------------------------*/

    ////// TrainDelayNotification Consumer

    // Produce delay notifications into new Kafka queue
/*    KafkaConfig delaySenderConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer")
            .withKeySerializer(Optional.of(LongSerializer::new));

    FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer = new FlinkKafkaProducer011<>(
            Topic.DELAY_NOTIFICATIONS.getValueDescriptor().getName(),
            new GenericBinaryProtoSerializer<NotificationOuterClass.Notification>(),
            delaySenderConfig.getProperties());

    trainDelayNotificationProducer.setWriteTimestampToKafka(true);*/

    /*-------------------------
     * End - Output/Consumer Setup
     * ++++++++++++++++++++++++
     * Begin - MongoDelayShift
     * ------------------------*/

    //The Stream is not necessary it passes through all events independent from a successful upload

/*    DataStream<PlannedTrainData> plannedTrainDataStreamUploaded = AsyncDataStream
      .unorderedWait(plannedTrainDataStream, new DataToMongoDB("plannedTrainData", mongoConfig),
        100000, TimeUnit.MILLISECONDS, 1);

    DataStream<Notification> notificationFromDelayShift = AsyncDataStream
            .unorderedWait(liveTrainDataStream, new DelayShiftFunctionMongo(mongoConfig),
                    100000, TimeUnit.MILLISECONDS, 1);

    notificationFromDelayShift.addSink(trainDelayNotificationProducer);*/

//    notificationFromDelayShift.print();
    /*-------------------------
     * End - MongoDelayShift
     * ++++++++++++++++++++++++
     * Begin - matchedLivePlanned
     * ------------------------*/

    // LivePlannedCorrelationFunction Mongo

    /*DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream = AsyncDataStream
            .unorderedWait(liveTrainDataStream, new LivePlannedCorrelationFunctionMongo( mongoConfig),
                    100000, TimeUnit.MILLISECONDS, 1);*/

    // LivePlannedCorrelationFunction Postgre
    //TODO!!
    //This might be Very Slow, maybe Too slow for  LivePlannedCorrelationFunction!!
//    plannedTrainDataStream.map(new DataToPostgresDatabase<PlannedTrainData>("planned",postgresConfig));

//    DataStream<Tuple2<LiveTrainData, PlannedTrainData>> matchedLivePlannedStream =
//        AsyncDataStream
//            .unorderedWait(liveTrainDataStream, new LivePlannedCorrelationFunction(postgresConfig),
//                100000, TimeUnit.MILLISECONDS, 1);

    // DetectStationArrivalDelay
/*
    DataStream<NotificationOuterClass.Notification> trainDelayNotificationDataStream = matchedLivePlannedStream
        .process(new DetectStationArrivalDelay()).name("train-delays");


    trainDelayNotificationDataStream.addSink(trainDelayNotificationProducer);
    trainDelayNotificationDataStream.print();
*/

    //TODO add consumer for these Events

    /*-------------------------
     * End - matchedLivePlanned
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

  public static class StationWithTimestamp implements Comparable<StationWithTimestamp>{
    public Long stationId;
    public com.google.protobuf.Timestamp plannedArrival;
    public StationWithTimestamp(Long stationId, com.google.protobuf.Timestamp plannedArrival){
      this.stationId = stationId;
      this.plannedArrival = plannedArrival;
    }

    @Override
    public int compareTo(StationWithTimestamp o) {
      return compareProtoTimestamps(this.plannedArrival, o.plannedArrival);
    }

    @Override
    public String toString() {
      return "StationWithTimestamp {" +
          "stationId: " + stationId.toString() +
          ", plannedArrival: " + plannedArrival.toString() + "}";
    }
  }

  public static class TrainState{
    public TreeSet<StationWithTimestamp> stationsWithTimestamp = new TreeSet<StationWithTimestamp>();
    public Integer delay = 0;
    public TrainState(){}
    public TrainState(TreeSet<StationWithTimestamp> stationsWithTimestamp, Integer delay){
      this.stationsWithTimestamp = stationsWithTimestamp;
      this.delay = delay;
    }

    @Override
    public String toString() {
      ArrayList<String> stations = new ArrayList<String>();
      for (StationWithTimestamp stat : stationsWithTimestamp){
        stations.add(stat.toString());
      }
      return "TrainState {" +
          "stationsWithTimestamp: " + stations.toString() +
          ", delay: " + delay.toString() +
          '}';
    }
  }

  public static class PlannedTrainIdKeySelector implements KeySelector<PlannedTrainData, Long> {
    @Override
    public Long getKey(PlannedTrainData planned){
      return planned.getTrainId();
    }
  }
  public static class LiveTrainIdKeySelector implements KeySelector<LiveTrainData, Long> {
    @Override
    public Long getKey(LiveTrainData live){
      return live.getTrainId();
    }
  }

  private static int compareProtoTimestamps(com.google.protobuf.Timestamp t1, com.google.protobuf.Timestamp t2){
    //  1 ; t1 > t2 : t1 is after t2
    // -1 ; t1 < t2 : t1 is before t2
    //  0 ; t1 = t2 : t1 and t2 are at the same time
    Long l = t1.getSeconds();
    return l.compareTo(t2.getSeconds());
  }
}

