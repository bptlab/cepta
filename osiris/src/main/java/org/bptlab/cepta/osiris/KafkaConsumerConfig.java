package org.bptlab.cepta.osiris;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.TrainDelayNotification;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.bptlab.cepta.serialization.AvroBinaryDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;


@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Bean
  public ConsumerFactory<Long, TrainDelayNotification> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        "localhost:29092");
    props.put(
        ConsumerConfig.GROUP_ID_CONFIG,
        KafkaConstants.GROUP_ID_CONFIG);
    props.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        LongDeserializer.class);
    props.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        AvroBinaryDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(
        props, new LongDeserializer(), new AvroBinaryDeserializer<>(TrainDelayNotification::new));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<Long, TrainDelayNotification>
  kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<Long, TrainDelayNotification> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setMissingTopicsFatal(false);
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }
}