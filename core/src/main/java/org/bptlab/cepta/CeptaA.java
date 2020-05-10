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

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cep.CEP;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventOuterClass.StaysInStationEvent;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.operators.DetectStationArrivalDelay;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.bptlab.cepta.serialization.GenericBinaryProtoSerializer;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CeptaA extends Cepta {

    // Producers
    private FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer;

    protected CeptaA(Builder builder) {
      super(builder);
    }

    public static class Builder extends Cepta.Builder<Builder> {
        public CeptaA build() { return new CeptaA(this); }
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

    @Override
    public void start() throws Exception {
        logger.info("Starting CEPTA core... [A]");

        // Setup the streaming execution environment
        this.setupProducers();

        DataStream<Event> plannedTrainDataEvents = env.addSource(this.sources.get(Topic.PLANNED_TRAIN_DATA));
        DataStream<Event> liveTrainDataEvents = env.addSource(this.sources.get(Topic.LIVE_TRAIN_DATA));
        DataStream<Event> weatherDataEvents = env.addSource(this.sources.get(Topic.WEATHER_DATA));

        DataStream<PlannedTrainData> plannedTrainDataStream = plannedTrainDataEvents.map((MapFunction<Event, PlannedTrainData>) Event::getPlannedTrain);
        DataStream<LiveTrainData> liveTrainDataStream = liveTrainDataEvents.map((MapFunction<Event, LiveTrainData>) Event::getLiveTrain);
        DataStream<WeatherData> weatherDataStream = weatherDataEvents.map((MapFunction<Event, WeatherData>) Event::getWeather);

        DataStream<StaysInStationEvent> staysInStationEventDataStream =
                CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern)
                        .process(StaysInStationPattern.staysInStationProcessFunction());

       /*
       DataStream<TrainDelayNotification> delayShiftNotifications = AsyncDataStream
          .unorderedWait(liveTrainDataStream, new DelayShiftFunction(postgresConfig),
            100000, TimeUnit.MILLISECONDS, 1);
      */
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
                new GenericBinaryProtoSerializer<>(),
                delaySenderConfig.getProperties());

        trainDelayNotificationProducer.setWriteTimestampToKafka(true);
        trainDelayNotificationDataStream.addSink(trainDelayNotificationProducer);
        trainDelayNotificationDataStream.print();

        /*
          delayShiftNotifications.addSink(trainDelayNotificationProducer);
          delayShiftNotifications.print();
        */

        KafkaConfig staysInStationKafkaConfig = new KafkaConfig().withClientId("StaysInStationProducer")
                .withKeySerializer(Optional.of(LongSerializer::new));

        FlinkKafkaProducer011<StaysInStationEvent> staysInStationProducer = new FlinkKafkaProducer011<>(
                Topic.STAYS_IN_STATION.getValueDescriptor().getName(),
                new GenericBinaryProtoSerializer<>(),
                staysInStationKafkaConfig.getProperties());

        staysInStationEventDataStream.addSink(staysInStationProducer);

        env.execute("CEPTA CORE");
    }
}
