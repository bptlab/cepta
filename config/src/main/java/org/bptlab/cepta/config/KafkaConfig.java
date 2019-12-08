package org.bptlab.cepta.config;

import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.bptlab.cepta.serialization.AvroBinarySerializer;
import picocli.CommandLine.Option;

public class KafkaConfig {

  @Option(
      names = {"-b", "--broker"},
      description = "Specifies the Kafka Broker (ex. localhost:29092).")
  static String kafkaBroker = KafkaConstants.KAFKA_BROKER;

  @Option(
      names = {"-cid", "--client-id"},
      description = "Specifies the Kafka client.")
  static String kafkaClientID = KafkaConstants.CLIENT_ID;

  @Option(
      names = {"-t", "--topic"},
      description = "Specifies the Kafka topic.")
  static String kafkaTopic = KafkaConstants.TOPIC_NAME;

  @Option(
      names = {"-gid", "--group-id"},
      description = "Specifies the Kafka group ID.")
  static String kafkaGroupID = KafkaConstants.GROUP_ID_CONFIG;

  public KafkaConfig() {
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

  public String getBroker() {
    return kafkaBroker;
  }

  public String getClientId() {
    return kafkaClientID;
  }

  public String getTopic() {
    return kafkaTopic;
  }

  public String getGroupID() {
    return kafkaGroupID;
  }

  public Properties getProperties() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBroker());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, getClientId());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroBinarySerializer.class.getName());
    return props;
  }
}
