package org.bptlab.cepta.osiris;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.bptlab.cepta.serialization.AvroDeserializer;
import org.bptlab.cepta.serialization.AvroSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/*
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

  @Value(value = "${kafka.broker}")
  private String kafkaBroker = KafkaConstants.KAFKA_BROKER;

  @Value(value = "${kafka.clientId}")
  private String clientId = KafkaConstants.CLIENT_ID;

  @Bean
  KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>>
  kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setMissingTopicsFatal(false);
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(3);
    factory.getContainerProperties().setPollTimeout(3000);
    return factory;
  }

  @Bean
  public ConsumerFactory<Integer, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs());
  }

  @Bean
  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "client1");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);
    props.put("key.deserializer", LongDeserializer.class.getName());
    props.put("value.deserializer", AvroDeserializer.class);
    return props;
  }
}
*/