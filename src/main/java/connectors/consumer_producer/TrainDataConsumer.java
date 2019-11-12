package connectors.consumer_producer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.LongDeserializer;

import java.util.Collections;
import java.util.Properties;

public class TrainDataConsumer {
    private static Consumer<Long, TrainData> createTrainDataConsumer(){
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KafkaConstants.KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                KafkaConstants.GROUP_ID_CONFIG);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                TrainDataDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, TrainData> consumer =
                new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(KafkaConstants.TOPIC_NAME));
        return consumer;
    }
    static void runConsumer() throws InterruptedException {
        final Consumer<Long, TrainData> consumer = createTrainDataConsumer();

        final int giveUp = 100;   int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<Long, TrainData> consumerRecords =
                    consumer.poll(1000);

            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }

            consumerRecords.forEach(record -> {
                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                        record.key(), record.value(),
                        record.partition(), record.offset());
            });

            consumer.commitAsync();
        }
        consumer.close();
        System.out.println("DONE");
    }
}
