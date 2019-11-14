package connectors.consumer_producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class WeatherDataProducer {

    public static Producer<Long, WeatherData> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.KAFKA_BROKERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConstants.CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WeatherDataSerializer.class.getName());
        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
        return new KafkaProducer<>(props);
    }

    public static void runProducer() throws Exception {
        final Producer<Long, WeatherData> producer = WeatherDataProducer.createProducer();

        // hold Cities Data
        BufferedReader reader = new BufferedReader(new FileReader("cities.csv"));
        List<String> cityList = new ArrayList<>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            cityList.add(line);
        }

        WeatherData weatherData = null;
        weatherData = WeatherAPI.makeCityApiCall("London");


        for (int index = 0; index < KafkaConstants.MESSAGE_COUNT; index++) {
            ProducerRecord<Long, WeatherData> record = new ProducerRecord<Long, WeatherData>(KafkaConstants.TOPIC_NAME,
                    weatherData);

            try {
                RecordMetadata metadata = producer.send(record).get();
                System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
                        + " with offset " + metadata.offset());
            }
            catch (ExecutionException | InterruptedException e) {
                System.out.println("Error in sending record");
                System.out.println(e);
            }
            Thread.sleep(2000);
        }
    }


}

