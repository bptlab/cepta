package connectors.consumer_producer;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;

public class Runner {

    public static void main(String[] args) {
        runProducer();
    }

    private static void runProducer() {

        Producer<Long, TrainData> producer = ProducerCreator.createProducer();

        for (int index = 0; index < KafkaConstants.MESSAGE_COUNT; index++) {
            TrainData trainData = new TrainData(2, "Tommy");
            ProducerRecord<Long, TrainData> record
                    = new ProducerRecord<Long, TrainData>(
                            KafkaConstants.TOPIC_NAME,
                            trainData);

            try {

                RecordMetadata metadata = producer.send(record).get();

                System.out.println("Record sent with key "
                        + index
                        + " to partition "
                        + metadata.partition()
                        + " with offset "
                        + metadata.offset());
            }

            catch (ExecutionException | InterruptedException e) {
                System.out.println("Error in sending record");
                System.out.println(e);
            }
        }
    }
}
