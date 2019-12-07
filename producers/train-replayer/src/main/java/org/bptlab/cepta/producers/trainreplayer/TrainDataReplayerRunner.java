package org.bptlab.cepta.producers.trainreplayer;

import java.sql.Timestamp;
import java.util.Optional;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.config.constants.DatabaseConstants;
import org.bptlab.cepta.producers.KafkaServiceRunner;
import org.bptlab.cepta.utils.converters.TimestampTypeConverter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "FieldCanBeLocal"})

@Command(
    name = "Train Data Replayer",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Replays the train events saved in our database.")
public class TrainDataReplayerRunner extends KafkaServiceRunner {

  @Mixin
  PostgresConfig databaseConfig = new PostgresConfig();

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
    TrainDataReplayerServer trainDataReplayerServer = TrainDataReplayerServer.newBuilder().withKafkaConfig(getDefaultProperties()).withDatabaseConfig(databaseConfig).withStartTime(startTimestamp).withEndTime(endTimestamp).withFrequency(frequency).build(grpcPort);
    trainDataReplayerServer.start();
    trainDataReplayerServer.blockUntilShutdown();
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new TrainDataReplayerRunner()).execute(args);
    System.exit(exitCode);
  }
}
