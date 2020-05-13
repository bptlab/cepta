# Introduction
This is following the entry database_example.md.

With the following instructions you will be able to receive data from a Kafka topic in Flink, so you can print it to the console.

We use the same example and constants.

You should already have the trainData.avsc, TrainDataProducer.java (and a way to run its _runProducer()_ method), TrainDataSerializer and TrainData.java files.

# How to run everything
If you have all the files, kafka topics, databases, tables and entries from database_example.md and this page, you have to perform the following steps to see the sent events from the producer in your console:
1. set all constants to your specific configs
1. start kafka and zookeeper
1. start your consumer
1. start your producer
1. see your sent data in your console :-)


# Dependencies
**Deprecated - we don't use Maven or Avro anymore**

You need the following additional maven dependencies in your pom.xml.
```
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-connector-kafka-0.11_2.11</artifactId>
	<scope>compile</scope>
	<version>${flink.version}</version>
</dependency>
<dependency>
	<groupId>org.apache.avro</groupId>
	<artifactId>avro</artifactId>
	<version>${flink.version}</version>
</dependency>
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-core</artifactId>
	<version>${flink.version}</version>
</dependency>
<dependency>
	<groupId>commons-io</groupId>
	<artifactId>commons-io</artifactId>
	<version>2.6</version>
</dependency>
```
Be sure to use a flink version over 1.4.1. and the shown connector version.

It is possible you need to remove the <scope>provided</scope> tag from some Flink dependencies because some classes can not be found.


# Get Kafka topic as data source
Let's take a look at the main method doing the trick. If you have another data class instead of TrainData, then replace every TrainData with your own class.
```
public static void main(String[] args) throws Exception {

	// set up the streaming execution environment
	final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // set properties of the consumer
	Properties properties = new Properties();
	properties.setProperty("bootstrap.servers", KafkaConstants.KAFKA_BROKERS);
	properties.setProperty("group.id", KafkaConstants.GROUP_ID_CONFIG);

        // create consumer that reads avro data as TrainData objects from topic "test"
	FlinkKafkaConsumer011<TrainData> consumer = new FlinkKafkaConsumer011<TrainData>(KafkaConstants.TOPIC_NAME, AvroDeserializationSchema.forSpecific(TrainData.class), properties);

        // add consumer as source for data stream
	DataStream<TrainData> inputStream = env.addSource(consumer);

        // print stream to console
	inputStream.print();

	env.execute("Flink Streaming Java API Skeleton");
}
```
For more information on the configs of the consumer see https://kafka.apache.org/10/javadoc/org/apache/kafka/clients/consumer/ConsumerConfig.html
