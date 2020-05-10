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
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.bptlab.cepta.models.internal.event.Event.NormalizedEvent;
import org.bptlab.cepta.models.events.station.StationDataOuterClass.StationData;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.station.StationOuterClass;
import org.bptlab.cepta.models.internal.transport.TransportOuterClass;
import org.bptlab.cepta.models.internal.updates.plan.PlanUpdateOuterClass;
import org.bptlab.cepta.normalization.LiveTrainDataNormalizationFunction;
import org.bptlab.cepta.normalization.PlannedTrainDataNormalizationFunction;
import org.bptlab.cepta.normalization.StationDataNormalizationFunction;
import org.bptlab.cepta.operators.aggregators.AggregateTransportsFunction;
import org.bptlab.cepta.operators.aggregators.FlinkStateAggregateTransportsFunction;
import org.bptlab.cepta.serialization.GenericBinaryProtoSerializer;

import java.util.*;

/*
    NOTE: Do not alter this implementation. Instead, extend this class or the Cepta class and replace the class in Main!
*/

public class CeptaB extends Cepta {

    // Producers
    protected FlinkKafkaProducer011<NotificationOuterClass.Notification> trainDelayNotificationProducer;
    protected FlinkKafkaProducer011<PlanUpdateOuterClass.PlanUpdate> transportPlanUpdateProducer;

    protected CeptaB(Cepta.Builder builder) {
        super(builder);
    }

    public static class Builder extends Cepta.Builder<CeptaA.Builder> {
        public CeptaB build() {
            return new CeptaB(this);
        }
    }

    private void setupProducers() {
        KafkaConfig trainDelayNotificationProducerConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer").withKeySerializer(Optional.of(LongSerializer::new));
        this.trainDelayNotificationProducer = new FlinkKafkaProducer011<>(
                Topic.DELAY_NOTIFICATIONS.getValueDescriptor().getName(),
                new GenericBinaryProtoSerializer<>(),
                trainDelayNotificationProducerConfig.getProperties());
        this.trainDelayNotificationProducer.setWriteTimestampToKafka(true);

        KafkaConfig transportPlanUpdateProducerConfig = new KafkaConfig().withClientId("TrainDelayNotificationProducer").withKeySerializer(Optional.of(LongSerializer::new));
        this.transportPlanUpdateProducer = new FlinkKafkaProducer011<>(
                Topic.TRANSPORT_PLAN_UPDATES.getValueDescriptor().getName(),
                new GenericBinaryProtoSerializer<>(),
                transportPlanUpdateProducerConfig.getProperties());
        this.transportPlanUpdateProducer.setWriteTimestampToKafka(true);
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting CEPTA core... [B]");

        this.setupProducers();

        DataStream<Event> plannedTrainDataEvents = env.addSource(this.sources.get(Topic.PLANNED_TRAIN_DATA));
        DataStream<Event> stationDataEvents = env.addSource(this.sources.get(Topic.STATION_DATA));
        DataStream<Event> liveTrainDataEvents = env.addSource(this.sources.get(Topic.LIVE_TRAIN_DATA));
        DataStream<Event> weatherDataEvents = env.addSource(this.sources.get(Topic.WEATHER_DATA));

        DataStream<NormalizedEvent> normalizedPlanUpdateStream = plannedTrainDataEvents.flatMap(new PlannedTrainDataNormalizationFunction());
        DataStream<NormalizedEvent> normalizedStationStream = stationDataEvents.flatMap(new StationDataNormalizationFunction());
        DataStream<NormalizedEvent> normalizedLiveUpdateStream = liveTrainDataEvents.flatMap(new LiveTrainDataNormalizationFunction());

        // TODO:  Aggregate normalized events
        // TODO:  Choose a good middle ground for window size, so that most connected transports fall into this window and longer transports
        //        will be fine to asynchronously call out to the query grpc service on a slow extra stream.
        //        TODO: We need a way to differentiate between a "first transport event" and a "late transport event" that is part of a long running transport
        //        One idea would be to have unfinished transport metadata kept in memory at the end of a stream that is passed to the next window
        //        Or immediately dispatch unfinished transports to another stream when the window closes?

        DataStream<TransportOuterClass.Transport> aggregatedStateStream = FlinkStateAggregateTransportsFunction.newBuilder()
                .setEnv(env)
                .setPlanUpdates(normalizedPlanUpdateStream)
                .setLiveUpdates(normalizedLiveUpdateStream)
                .setGlobalData(normalizedStationStream)
                .build()
                .aggregate();

        // TODO: Look for patterns in any normalized data stream or the aggregated data stream

        // TODO: Join aggregated stream with the singular pattern matches and reason about sending delay notifications

        // TODO

        // Send normalized plan data to archival
        // normalizedPlanUpdateStream.addSink(transportPlanUpdateProducer);

        env.execute("CEPTA CORE");
    }
}
