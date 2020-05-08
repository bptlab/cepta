package org.bptlab.cepta;

import java.util.HashMap;
import java.util.concurrent.Callable;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.serialization.GenericBinaryProtoDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import org.bptlab.cepta.models.events.event.EventOuterClass;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;

@Command(
        name = "CEPTA Core",
        mixinStandardHelpOptions = true,
        version = "0.4.0",
        description = "Processes source event streams")
public class Main implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

    // Consumers
    private FlinkKafkaConsumer011<EventOuterClass.Event> liveTrainDataConsumer;
    private FlinkKafkaConsumer011<EventOuterClass.Event> plannedTrainDataConsumer;
    private FlinkKafkaConsumer011<EventOuterClass.Event> weatherDataConsumer;

    private void setupConsumers() {
        this.liveTrainDataConsumer =
                new FlinkKafkaConsumer011<>(
                        Topic.LIVE_TRAIN_DATA.getValueDescriptor().getName(),
                        new GenericBinaryProtoDeserializer<>(EventOuterClass.Event.class),
                        new KafkaConfig().withClientId("LiveTrainDataMainConsumer").getProperties());
        this.plannedTrainDataConsumer =
                new FlinkKafkaConsumer011<>(
                        Topic.PLANNED_TRAIN_DATA.getValueDescriptor().getName(),
                        new GenericBinaryProtoDeserializer<>(EventOuterClass.Event.class),
                        new KafkaConfig().withClientId("PlannedTrainDataMainConsumer").getProperties());

        this.weatherDataConsumer =
                new FlinkKafkaConsumer011<>(
                        Topic.WEATHER_DATA.getValueDescriptor().getName(),
                        new GenericBinaryProtoDeserializer<>(EventOuterClass.Event.class),
                        new KafkaConfig().withClientId("WeatherDataMainConsumer").withGroupID("Group").getProperties());
    }

    @Mixin
    KafkaConfig kafkaConfig = new KafkaConfig();

    @Mixin
    PostgresConfig postgresConfig = new PostgresConfig();

    @Option(names = {"-i", "--implementation"}, description = "A, B, ...")
    private String implementation = "A";

    // Set the builder here explicitly if you do not want to use command line arguments
    Cepta.Builder builder;

    @Override
    public Integer call() throws Exception {
        logger.info("Starting CEPTA core...");

        // Setup the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        this.setupConsumers();

        // Collect input sources
        HashMap<Topic, FlinkKafkaConsumer011<Event>> sources = new HashMap<>();
        sources.put(Topic.PLANNED_TRAIN_DATA, plannedTrainDataConsumer);
        sources.put(Topic.LIVE_TRAIN_DATA, liveTrainDataConsumer);
        sources.put(Topic.WEATHER_DATA, weatherDataConsumer);

        // Start
        if (builder == null) {
            switch (this.implementation.toLowerCase().trim()) {
                case "b":
                    builder = new CeptaB.Builder();
                    break;
                default:
                    builder = new CeptaA.Builder();
            }
        }
        builder.setEnv(env).setSources(sources).setKafkaConfig(kafkaConfig).setPostgresConfig(postgresConfig).build().start();
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
