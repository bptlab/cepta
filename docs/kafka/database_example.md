# Introduction
We want to read data from a database table to Kafka. Because we want to see that it works we also build a Consumer which subscribes to Kafka and sends the data to the console.

But if you want to continue using the data in Flink you can stop after the Serializer. (And continue in "Get Data from Kafka to Flink")

To make it easier to understand we will use a simple example. The data we use is TrainData which has an id (int) and a name (string).

# How to start the pipeline
So if you already have all the classes and just want to run it all, do the following:
1. write and generate all the stuff you need following the novel I wrote above
1. set up a database (fill it with test data or you won't be happy because you won't see magic)
2. run a kafka server
3. set all constants for the database and Kafka to what you need
4. write main methods that run the consumer and the producer (they need to run parallel). This means you need a method that calls _runProducer()_ and one that calls _runConsumer()_ you can run.
5. Run those methods and be in awe!


# Dependencies
**Deprecated - we use bazel instead of maven and protobuf instead of avro**
You need the following maven dependencies in your pom.xml
```
<!-- gson for converting json to object and vice versa -->
<dependency>
	<groupId>com.google.code.gson</groupId>
	<artifactId>gson</artifactId>
	<version>2.3.1</version>
</dependency>
<!-- jdbc stuff so we can talk with our postgresql database -->
<dependency>
	<groupId>org.postgresql</groupId>
	<artifactId>postgresql</artifactId>
	<version>42.2.0</version>
</dependency>
<!-- avro deserialization schema-->
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-avro</artifactId>
	<version>${flink.version}</version>
</dependency>
```

# Constants
The names are self explaining, so we let them speak ;-)
## Database
```
public interface DatabaseConstants {
    String SERVER = "localhost";

    int PORT = 5432;

    String DATABASE_NAME = "bachelor_projekt";

    String USER = "testuser";

    String PASSWORD = "password";

    String CONNECTOR = "jdbc";

    String DATABASE_SYSTEM = "postgresql";
}
```
## Kafka
```
public interface KafkaConstants {
   // adress of your kafka server
    String KAFKA_BROKERS = "localhost:9092";

    String CLIENT_ID = "client1";

    String TOPIC_NAME = "test";

    String GROUP_ID_CONFIG = "consumerGroup1";
}
```

# Database 
We need a database with the database name (same as in DatabaseKonstants). The user specified in DatabaseKonstants should have the necessary rights to perform the needed operations.

# Avro Schema and Code Generation
**Deprecated - use protobuf instead**
## Create schema
To create a schema for your data you build a .avsc file named like your data. There you specify the namespace, attributes and so on. You do that in JSON format.
```
{
 "namespace": "connectors.consumer_producer",
 "type": "record",
 "name": "TrainData",
 "fields": [
    {"name": "id", "type": "int"},
    {"name": "name", "type": "string"}
    ]
}
```
See more to avro schemas at http://avro.apache.org/docs/1.8.1/spec.html#schema_record
## Generate Java class
With the  avro plugin you can generate a TrainData.java (or other data class ;-) ) file from a TrainData.avsc file.
The code belongs to your pom.xml under the plugins-tag.

generate with "mvn clean avro:schema"

generated classes land in cepta -> src -> target -> generated-sources -> avro
```
<!-- for Avro code generation -->
<plugin>
	<groupId>org.apache.avro</groupId>
	<artifactId>avro-maven-plugin</artifactId>
	<version>1.8.2</version>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>schema</goal>
			</goals>
			<configuration>
				<sourceDirectory>${project.basedir}/src/main/avro/</sourceDirectory>
				<outputDirectory>${project.basedir}/src/main/java/</outputDirectory>
			</configuration>
		</execution>
	</executions>
</plugin>
```

### Beware!!! 
If you want to use your own consumer (as described further down):
If you have attributes with the type String you have to replace "charsequence" in the data class (TrainData.java) with "String".
Don't do that if you want to continue with "Get Data from Kafka to Flink".

# Producer
To send data into the Kafka queue we need a producer. It can look like the following.

_producer.flush()_ sends the currently stored data to Kafka.
```
public class TrainDataProducer {

    private static final String TABLE_NAME = "test_train_data";
    private static int last_id = 0;
    private static Connection connection;
    private static long PAUSE = 1000; // ime between sending records in milliseconds


    public static Producer<Long, TrainData> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KafkaConstants.KAFKA_BROKERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConstants.CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                TrainDataSerializer.class.getName());
        return new KafkaProducer<Long, TrainData>(props);
    }

    public static void runProducer(final int sendMessageCount) throws Exception {
        final Producer<Long, TrainData> producer = createProducer();
        long time = System.currentTimeMillis();
        connect();
        try {
            for (long index = time; index < time + sendMessageCount; index++) {
                TrainData trainData = null;
                try {
                    trainData = fetchNextData();
                } catch (NullPointerException e) {
                    System.out.println("There is no new Train Data left in the database.");
                }
                if (trainData != null) {
                    ProducerRecord<Long, TrainData> record
                            = new ProducerRecord<Long, TrainData>(
                            KafkaConstants.TOPIC_NAME,
                            trainData);

                    RecordMetadata metadata = producer.send(record).get();

                    long elapsedTime = System.currentTimeMillis() - time;
                    System.out.printf("sent record(key=%s value=%s) " +
                                    "meta(partition=%d, offset=%d) time=%d\n",
                            record.key(), record.value(), metadata.partition(),
                            metadata.offset(), elapsedTime);
                    producer.flush();
                    Thread.sleep(PAUSE);
                } else {
                    break;
                }

            }
        } finally {

            producer.close();
        }
    }
}
```
_connect()_ is a function you can implement by yourself. It connects to the database.
```
private static void connect() {
    String url = DatabaseConstants.CONNECTOR + ":" 
            + DatabaseConstants.DATABASE_SYSTEM + "://"
            + DatabaseConstants.SERVER + ":"
            + DatabaseConstants.PORT + "/"
            + DatabaseConstants.DATABASE_NAME;

    try {
        connection = DriverManager.getConnection(url, DatabaseConstants.USER, DatabaseConstants.PASSWORD);
    } catch (SQLException e) {
        System.out.println("SQLException: Connection could not be established.");
        e.printStackTrace();
    }
}
```
 _fetchNextData()_ is another method you can implement for yourself. It fetches the next entry from a table you specify in the constant TABLE_NAME from the database you connected to.
```
private static TrainData fetchNextData() throws SQLException {
    String getNextEntry =
            "SELECT * " +
                    "FROM " + TABLE_NAME + " " +
                    "WHERE id > " + last_id + " " +
                    "ORDER BY id ASC " +
                    "FETCH FIRST 1 ROWS ONLY; ";
    last_id++;
    Statement getNextEntryStatement = connection.createStatement();
    ResultSet result = getNextEntryStatement.executeQuery(getNextEntry);
    if (result.next()) {
        return new TrainData(result.getInt("id"), result.getString("name"));
    } else {
        // There is no new Train Data left in the database.
        // maybe we could use our own exception here
        throw new NullPointerException();
    }
}
```
## Serializer
**Deprecated - we have a different Serializer now and don't rely on avro anymore**

Sadly it is not enough to just throw the data as it comes from the database directly to Kafka. Therefor we need a serializer that converst our data to a byte array.
We use the avro format for that. That's why we need to reference the schema from our data class (row 10: `Schema schema = TrainData.SCHEMA$;`).
```
public class TrainDataSerializer implements Serializer<TrainData> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String arg0, TrainData data) {
        // serializes TrainData to byte[]
        try {
            Schema schema = TrainData.SCHEMA$;
            DatumReader<Object> reader = new GenericDatumReader<>(schema);
            GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, data.toString());
            Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);
            Object datum = reader.read(null, decoder);
            writer.write(datum, encoder);

            // send record
            encoder.flush();
            return output.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public void close() {
    }
}
```

# Consumer
The consumer subscribes to a Kafka topic and prints all the TrainData it gets to our console.
```

public class TrainDataConsumer {
    private static Consumer<Long, TrainData> createTrainDataConsumer() {
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

    static void runConsumer() {
        final Consumer<Long, TrainData> consumer = createTrainDataConsumer();

        final int giveUp = 100;
        int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<Long, TrainData> consumerRecords =
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

## Deserializer
**Deprecated - we have a different Deserializer now and don't rely on avro anymore**

But Kafka is stupid and can not send our data in the right format, but only byte array. That's why we need a deserializer to convert it back to TrainData.
```
public class TrainDataDeserializer implements Deserializer<TrainData> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public TrainData deserialize(String topic, byte[] data) {
        try {
            boolean pretty = false;
            Schema schema = TrainData.SCHEMA$;
            GenericDatumReader<Object> reader = new GenericDatumReader<>(schema);
            DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, output, pretty);
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            Object datum = reader.read(null, decoder);
            writer.write(datum, encoder);
            encoder.flush();
            output.flush();
            String stringData = new String(output.toByteArray(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            TrainData trainData = gson.fromJson(stringData, TrainData.class);
            return trainData;
        } catch (IOException e) {
        }
        return null;
    }
    @Override
    public void close() {
    }
}
```


