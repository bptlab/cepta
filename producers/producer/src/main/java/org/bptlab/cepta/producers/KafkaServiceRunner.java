package org.bptlab.cepta.producers;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.serialization.AvroSerializer;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.bptlab.cepta.serialization.TrainDataRunningSerializer;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public abstract class KafkaServiceRunner implements Callable<Integer> {

  /*
  Kafka options
   */
  @Option(
      names = {"-b", "--broker"},
      description = "Specifies the Kafka Broker (ex. localhost:29092).")
  protected String kafkaBroker = KafkaConstants.KAFKA_BROKER;

  @Option(
      names = {"-cid", "--client-id"},
      description = "Specifies the Kafka client.")
  protected String kafkaClientID = KafkaConstants.CLIENT_ID;

  @Option(
      names = {"-t", "--topic"},
      description = "Specifies the Kafka topic.")
  protected String kafkaTopic = KafkaConstants.TOPIC;

  @Option(
      names = {"-gid", "--group-id"},
      description = "Specifies the Kafka group ID.")
  private String kafkaGroupID = KafkaConstants.GROUP_ID_CONFIG;

  protected Properties getDefaultProperties() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaClientID);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    // new AvroSerializer<LiveTrainData>(new Supplier<LiveTrainData>()).getClass().getName()
    // AvroSerializer<LiveTrainData> test = new AvroSerializer<LiveTrainData>(LiveTrainData::new).getClass();
    props.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new AvroSerializer<LiveTrainData>(LiveTrainData::new).getClass());
    return props;
  }
}
