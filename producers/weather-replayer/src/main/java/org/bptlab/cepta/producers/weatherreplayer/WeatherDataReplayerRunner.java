package org.bptlab.cepta.producers.weatherreplayer;

import java.sql.Timestamp;
import java.util.Optional;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.producers.KafkaServiceRunner;
import org.bptlab.cepta.utils.converters.OptionalTypeConverter;
import org.bptlab.cepta.utils.converters.TimestampTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "FieldCanBeLocal"})

@Command(
    name = "Weather Data Replayer",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Replays the weather events saved in our database.")
public class WeatherDataReplayerRunner extends KafkaServiceRunner {

  private static final Logger logger =
      LoggerFactory.getLogger(WeatherDataReplayerRunner.class.getName());

  @Mixin
  PostgresConfig databaseConfig = new PostgresConfig();

  @Option(
      names = {"--log-level"},
      description = "Sets the output log level.")
  private long logLevel = 2000;

  @Option(
      names = {"--must-match"},
      description = "Will be included in database query to narrow down events",
      converter = OptionalTypeConverter.class)
  private Optional<String> mustMatch = Optional.empty();

  @Option(
      names = {"-f", "--frequency"},
      description = "Sets the frequency in which an event should be replayed after the other.")
  private long frequency = 2000;

  @Option(
      names = {"-from", "--start-timestamp"},
      description = "Sets the start time. Earlier events are not supposed to be replayed.",
      converter = TimestampTypeConverter.class)
  private Optional<Timestamp> startTimestamp = Optional.empty();

  @Option(
      names = {"-to", "--end-timestamp"},
      description = "Sets the end time. Later events are not supposed to be replayed.",
      converter = TimestampTypeConverter.class)
  private Optional<Timestamp> endTimestamp = Optional.empty();

  @Option(
      names = {"--grpc-port"},
      description = "Specifies the port for serving the gRPC service.")
  private int grpcPort = 8080;

  @Override
  public Integer call() throws Exception {
    logger.info("Starting weather data replayer gRPC server");
    WeatherDataReplayerServer weatherDataReplayerServer = WeatherDataReplayerServer.newBuilder()
        .withKafkaConfig(kafkaConfig).withDatabaseConfig(databaseConfig)
        .withStartTime(startTimestamp).withEndTime(endTimestamp).withFrequency(frequency)
        .mustMatch(mustMatch)
        .build(grpcPort);
    weatherDataReplayerServer.startGrpcServer();
    weatherDataReplayerServer.blockUntilShutdown();
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new WeatherDataReplayerRunner()).execute(args);
    System.exit(exitCode);
  }
}
