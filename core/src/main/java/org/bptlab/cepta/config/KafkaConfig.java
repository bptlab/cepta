package org.bptlab.cepta.config;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import picocli.CommandLine.Option;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class KafkaConfig implements Serializable, Cloneable {

  @Option(
      names = {"-b", "--broker"},
      description = "Specifies the Kafka Broker (ex. localhost:29092).")
  static String kafkaBroker = "localhost:29092";

  @Option(
      names = {"-cid", "--client-id"},
      description = "Specifies the Kafka client.")
  static String kafkaClientID = "client1";

  @Option(
      names = {"-t", "--topic"},
      description = "Specifies the Kafka topic.")
  static String kafkaTopic = "test";

  @Option(
      names = {"-gid", "--group-id"},
      description = "Specifies the Kafka group ID.")
  static String kafkaGroupID = "consumerGroup1";

  String broker;
  String clientId;

  static Optional<Supplier<? extends Serializer<?>>> keySerializer = Optional.empty();
  static Optional<Supplier<? extends Serializer<?>>> valueSerializer = Optional.empty();

  public KafkaConfig() {}

  public KafkaConfig(String broker, String clientId) {
    this.broker = broker;
    this.clientId = clientId;
  }

  public KafkaConfig build() {
    return new KafkaConfig(this.broker, this.clientId);
  }

  public KafkaConfig withBroker(String broker) {
    KafkaConfig.kafkaBroker = broker;
    return this;
  }

  public KafkaConfig withClientId(String clientId) {
    KafkaConfig.kafkaClientID = clientId;
    return this;
  }

  public KafkaConfig withTopic(String topicName) {
    KafkaConfig.kafkaTopic = topicName;
    return this;
  }

  public KafkaConfig withGroupID(String groupID) {
    KafkaConfig.kafkaGroupID = groupID;
    return this;
  }

  public KafkaConfig withKeySerializer(Optional<Supplier<? extends Serializer<?>>> serializer) {
    KafkaConfig.keySerializer = serializer;
    return this;
  }

  public KafkaConfig withValueSerializer(Optional<Supplier<? extends Serializer<?>>> serializer) {
    KafkaConfig.valueSerializer = serializer;
    return this;
  }

  public String getBroker() {
    return kafkaBroker;
  }

  public String getClientId() {
    return kafkaClientID;
  }

  public String getTopic() {
    return kafkaTopic;
  }

  public Optional<Supplier<? extends Serializer<?>>> getKeySerializer() {
    return keySerializer;
  }

  public Optional<Supplier<? extends Serializer<?>>> getValueSerializer() {
    return valueSerializer;
  }

  public String getGroupID() {
    return kafkaGroupID;
  }

  public Properties getProperties() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBroker());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, getClientId());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupID());
    getKeySerializer().map(s -> props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, s.get().getClass().getName()));
    getValueSerializer().map(s -> props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, s.get().getClass().getName()));
    return props;
  }
}
