package connectors.consumer_producer;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;

public class ProducerRunner {

    public static void main(String[] args) throws Exception {
        runProducer();
    }

    private static void runProducer() throws Exception {
        TrainDataProducer.runProducer(5);
    }
}
