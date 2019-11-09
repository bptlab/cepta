package wordcount;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.Properties;

public class WindowWordCount {
    private static String KAFKA_SERVER = "localhost:9092";
    private static String KAFKA_GROUP_ID = "myGroup";
    private static String KAFKA_TOPIC = "test";
    private static String ZOOKEEPER_SERVER = "localhost:2181";

    public static void main(String[] args) throws Exception{
        /*
        String[] testArgs = {"--topic", "test",
                "--bootstrap.servers", "localhost:9092",
                "--zookeeper.connect", "localhost:2181",
                "--group.id","myGroup"};
        System.out.println("HI");
        */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        /*
        ParameterTool parameterTool = ParameterTool.fromArgs(testArgs);

        DataStream<String> messageStream = env.addSource(new FlinkKafkaConsumer<>(parameterTool.getRequired("topic"), new SimpleStringSchema(), parameterTool.getProperties()));
        */
        /*
        DataStream<Tuple2<String, Integer>> dataStream = env
                .socketTextStream("localhost", 9999)
                .flatMap(new Splitter())
                .keyBy(0)
                .timeWindow(Time.seconds(5))
                .sum(1);

        dataStream.print();
        */
        DataStream<String> rawInputStream = env.addSource(createStringConsumerFromTopic(KAFKA_TOPIC, KAFKA_SERVER, KAFKA_GROUP_ID, ZOOKEEPER_SERVER));
        DataStream<Integer> integerInputStream = rawInputStream.map(new MapFunction<String, Integer>(){
           @Override
           public Integer map(String value) throws Exception {
               return Integer.valueOf(value);
           }
        });
        DataStream<Integer> filteredStream = integerInputStream.filter(new FilterFunction<Integer>() {
            @Override
            public boolean filter(Integer integer) throws Exception {
                return (integer > 3);
            }
        });
        filteredStream.print();

        env.execute("Window WordCount");
    }

    public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
            for (String word: sentence.split(" ")) {
                out.collect(new Tuple2<String, Integer>(word, 1));
            }
        }
    }

    // creates Flink Consumer from topic, kafka server address,
    // zookeeper server address and group of kafka
    public static FlinkKafkaConsumer<String> createStringConsumerFromTopic(String topic, String kafkaAddress, String kafkaGroup, String zookeeperAddress){
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkaAddress);
        props.setProperty("group.id", kafkaGroup);
        props.setProperty("zookeeper.server", zookeeperAddress);
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(topic, new SimpleStringSchema(), props);

        return consumer;
    }
}
