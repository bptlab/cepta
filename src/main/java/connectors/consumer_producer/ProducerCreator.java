package connectors.consumer_producer;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;

import org.apache.kafka.clients.producer.Producer;

import org.apache.kafka.clients.producer.ProducerConfig;

import org.apache.kafka.common.serialization.LongSerializer;

import org.apache.kafka.common.serialization.StringSerializer;


public class ProducerCreator {
    // this class can create a producer with the config stuff from the KafkaConstants file
    // more at https://dzone.com/articles/kafka-producer-and-consumer-example
    public static Producer<Long, TrainData> createProducer() {

        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.KAFKA_BROKERS);

        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConstants.CLIENT_ID);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());

        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TrainDataSerializer.class.getName());

        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());

        return new KafkaProducer<Long, TrainData>(props);

    }

}