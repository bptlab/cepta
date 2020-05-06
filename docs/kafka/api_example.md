# Introduction
We want to get periodical data from an external data source with an API and get that data into Kafka.

For that we build a simple API to Kafka example which makes calls to the Openweathermap(OWM)-API and transfers them to Kafka.

# Constants
We used self-explanatory names in this example.
## Kafka

```
 public interface KafkaConstants {
    String KAFKA_BROKERS = "localhost:9092";

    Integer MESSAGE_COUNT = 100;

    String CLIENT_ID = "client1";

    String TOPIC_NAME = "test";

    String GROUP_ID_CONFIG = "consumerGroup1";

    Integer MAX_NO_MESSAGE_FOUND_COUNT = 100;

    String OFFSET_RESET_LATEST = "latest";

    String OFFSET_RESET_EARLIER = "earliest";

    Integer MAX_POLL_RECORDS = 1;
 }
```

# Producer
In order to get data into kafka we need a producer(WeatherDataProducer.java). This is a generic one, that does the job by first creating a producer and then, surprise, running it.
```
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

```
This class also gets a list of cities we want to get the weather of and with that tries to make the API call for which we need the WeatherAPI.java class.

# WeatherAPI
This class provides different methods for API calls. They all have in common that they first need to create an object of the OWM class with your OWM-API-key. After that, you can use one of the many methods OWM provides to get whatever data you need from them. 

To make this work, you need to first download the owm-library [here](https://jar-download.com/?search_box=owm) and then do the following:
1. Move the folder into your project first
2. In intelliJ, go to File
3. Project structure
4. library
5. "+"
6. java
7. choose the wanted folder

Your code should look somewhat like this:
```
import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

import java.util.*;

public class WeatherAPI {

    // make an Open Weather API Call for a specific City
    public static WeatherData makeCityApiCall(String city) throws APIException {
        // declaring object of "OWM" class
        OWM owm = new OWM(*insert API key*);

        // getting current weather data for the "London" city
        CurrentWeather cwd = owm.currentWeatherByCityName(city);

        return new WeatherData(cwd.getCityName(), cwd.getMainData().getTempMax().intValue());
    }

    public static String makeRandomAPICall( List<String> cityList) {

        int size = cityList.size() - 1;
        int random = (int )(Math.random()* size + 0);

        String city = cityList.get(random);
        String cleanCity = city.replace("\"" , "");
        System.out.println(cleanCity);
        // declaring object of "OWM" class
        OWM owm = new OWM(*insert API key here*);

        // getting current weather data for the "London" city
       try {
           CurrentWeather cwd = owm.currentWeatherByCityName(cleanCity);
           return (" City: " + cwd.getCityName() +
                   "\n" + " Temperature: " + cwd.getMainData().getTempMax() + "/" + cwd.getMainData().getTempMin() + "\'K" +
                   "\n" + " Windspeed: " +cwd.getWindData().getSpeed());
       } catch (Exception e) {
           System.out.println("Api error by city:" + cleanCity);
           return "Error";
       }



    }

}
```
There's also the producer runner, that runs the producer:
```
public class ProducerRunner {

    public static void main(String[] args) throws Exception {
        runProducer();
    }

    private static void runProducer() throws Exception {
        WeatherDataProducer.runProducer();
    }
}
```
# Serializer

We also need a serializer that converts our data to a byte array. Here we use generated Messages frm protobuf

```
import org.apache.flink.api.common.serialization.SerializationSchema;
import com.google.protobuf.GeneratedMessageV3;

public class GenericBinaryProtoSerializer<T extends GeneratedMessageV3> implements SerializationSchema<T> {

  @Override
    public byte[] serialize(T value) {
        return value.toByteArray();
    }
}
```
# Consumer

The consumer subscribes to a Kafka topic and prints all the WeatherData it gets to our console.


```
public class WeatherDataConsumer {
    private static Consumer<Long, WeatherData> createTrainDataConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KafkaConstants.KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                KafkaConstants.GROUP_ID_CONFIG);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                WeatherDataDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, WeatherData> consumer =
                new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(KafkaConstants.TOPIC_NAME));
        return consumer;
    }

    static void runConsumer() {
        final Consumer<Long, WeatherData> consumer = createTrainDataConsumer();

        final int giveUp = 100;
        int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<Long, WeatherData> consumerRecords =
                    consumer.poll(1000);

            if (consumerRecords.count() == 0) {
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
```
We also have a consumer runner, that starts the consumer:
```
public class ConsumerRunner {
    public static void main(String[] args) throws InterruptedException {
        runConsumer();
    }

    private static void runConsumer() throws InterruptedException {
        WeatherDataConsumer.runConsumer();
    }
}
```

# Deserializer

We have a serializer and therefore need a deserializer to convert the data back to WeatherData.

```
import org.apache.flink.api.common.serialization.SerializationSchema;
import com.google.protobuf.GeneratedMessageV3;

public class GenericBinaryProtoSerializer<T extends GeneratedMessageV3> implements SerializationSchema<T> {

  @Override
    public byte[] serialize(T value) {
        return value.toByteArray();
    }
}
```
