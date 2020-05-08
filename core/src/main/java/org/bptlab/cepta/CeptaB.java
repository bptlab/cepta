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
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.serialization.GenericBinaryProtoSerializer;
import java.util.HashMap;
import java.util.Optional;

public class CeptaB extends Cepta {

  // Producers
  private FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer;

  protected CeptaB(Cepta.Builder builder) {
    super(builder);
  }

  public static class Builder extends Cepta.Builder<CeptaA.Builder> {
    public CeptaB build() { return new CeptaB(this); }
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
    logger.info("Starting CEPTA core... [B]");

    env.setParallelism(1);
    this.setupProducers();

    DataStream<Event> plannedTrainDataEvents = env.addSource(this.sources.get(Topic.PLANNED_TRAIN_DATA));
    DataStream<Event> liveTrainDataEvents = env.addSource(this.sources.get(Topic.LIVE_TRAIN_DATA));
    DataStream<Event> weatherDataEvents = env.addSource(this.sources.get(Topic.WEATHER_DATA));

    DataStream<PlannedTrainData> plannedTrainDataStream = plannedTrainDataEvents.map((MapFunction<Event, PlannedTrainData>) Event::getPlannedTrain);
    DataStream<LiveTrainData> liveTrainDataStream = liveTrainDataEvents.map((MapFunction<Event, LiveTrainData>) Event::getLiveTrain);
    DataStream<WeatherData> weatherDataStream = weatherDataEvents.map((MapFunction<Event, WeatherData>) Event::getWeather);

    env.execute("CEPTA CORE");
  }
}
