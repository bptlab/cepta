package org.bptlab.cepta.producers.trainreplayer;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import org.bptlab.cepta.config.constants.DatabaseConstants;
import org.bptlab.cepta.producers.KafkaServiceRunner;
import org.bptlab.cepta.producers.PostgresReplayer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

class TimestampTypeConverter implements ITypeConverter<Optional<Timestamp>> {
  public Optional<Timestamp> convert(String value) throws Exception {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Date parsedDate = dateFormat.parse(value);
    return Optional.of(new Timestamp(parsedDate.getTime()));
  }
}

@Command(
    name = "Train Data Replayer",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Replays the train events saved in our database.")
public class TrainDataReplayerRunner extends KafkaServiceRunner {

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
      names = {"--host"},
      description = "Specifies the Database Host (ex. postgres).")
  private String databaseHost = DatabaseConstants.HOST;

  @Option(
      names = {"--db-port"},
      description = "Specifies the port where the Database is running.")
  private int databasePort = DatabaseConstants.PORT;

  @Option(
      names = {"--grpc-port"},
      description = "Specifies the port for serving the gRPC service.")
  private int grpcPort = 8080;

  @Option(
      names = {"-db", "--database"},
      description = "Specifies the name of the Database.")
  private String databaseName = DatabaseConstants.DATABASE_NAME;

  @Option(
      names = {"-u", "--user"},
      description = "Specifies the user of the Database.")
  private String databaseUser = DatabaseConstants.USER;

  @Option(
      names = {"-pw", "--password"},
      description = "Specifies the password of the Database.")
  private String databasePassword = DatabaseConstants.PASSWORD;

  @Option(
      names = {"-c", "--connector"},
      description = "Specifies the connector to the Database (ex. jdbc).")
  private String databaseConnector = DatabaseConstants.CONNECTOR;

  @Option(
      names = {"-proto", "--protocol"},
      description = "Specifies the protocol of the Database (ex. postgresql).")
  private String databaseProtocol = DatabaseConstants.PROTOCOL;

  void initializeReplayer(PostgresReplayer replayer){
    replayer.connect(
        databaseConnector,
        databaseProtocol,
        databaseHost,
        databasePort,
        databaseName,
        databaseUser,
        databasePassword);
    startTimestamp.ifPresent(replayer::setStartTime);
    endTimestamp.ifPresent(replayer::setEndTime);
    replayer.setFrequency(frequency);
    replayer.setTopic(kafkaTopic);
  }

  @Override
  public Integer call() throws Exception {
    // Setup the Replayers in order for them to be run by the Server
    LiveTrainDataReplayer liveTrainDataReplayer = new LiveTrainDataReplayer(getDefaultProperties());
    PlannedTrainDataReplayer plannedTrainDataReplayer = new PlannedTrainDataReplayer(getDefaultProperties());
    initializeReplayer(liveTrainDataReplayer);
    initializeReplayer(plannedTrainDataReplayer);

    // Run server until being shut down
    TrainDataReplayerServer liveTrainDataReplayerServer = new TrainDataReplayerServer(liveTrainDataReplayer, grpcPort);
    liveTrainDataReplayerServer.start();
    TrainDataReplayerServer plannedTrainDataReplayerServer = new TrainDataReplayerServer(plannedTrainDataReplayer, grpcPort + 1);
    plannedTrainDataReplayerServer.start();

    plannedTrainDataReplayerServer.blockUntilShutdown();
    liveTrainDataReplayerServer.blockUntilShutdown();
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new TrainDataReplayerRunner()).execute(args);
    System.exit(exitCode);
  }
}
