package connectors.consumer_producer;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;

public class Runner {

    public static void main(String[] args) throws Exception {
        runProducer();
    }

    private static void runProducer() throws Exception {

        Producer<Long, TrainData> producer = TrainDataProducer.createProducer();
        TrainDataProducer.runProducer(10);
    }
}
