package org.bptlab.cepta.producers.trainreplayer;

import io.grpc.stub.StreamObserver;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.producers.PostgresReplayer;
import org.bptlab.cepta.producers.exceptions.NoDatabaseConnectionException;
import org.bptlab.cepta.producers.replayer.Success;
import org.bptlab.cepta.utils.grpc.GrpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "FieldCanBeLocal"})

public class TrainDataReplayerServer
    extends GrpcServer<org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerImplBase> {
  private static final Logger logger =
      LoggerFactory.getLogger(TrainDataReplayerServer.class.getName());

  public static final class Builder {
    private int servicePort;
    private Properties kafkaConfig = new Properties();
    private PostgresConfig databaseConfig = new PostgresConfig();
    private long frequency = 5000;
    private Optional<Timestamp> startTimestamp;
    private Optional<Timestamp> endTimestamp;

    private Builder() {}

    public Builder withDatabaseConfig(PostgresConfig config) {
      databaseConfig = config;
      return this;
    }

    public Builder withKafkaConfig(Properties config) {
      kafkaConfig = config;
      return this;
    }

    public Builder withStartTime(Optional<Timestamp> startTime) {
      startTimestamp = startTime;
      return this;
    }

    public Builder withEndTime(Optional<Timestamp> endTime) {
      endTimestamp = endTime;
      return this;
    }

    public Builder withFrequency(long frequency) {
      this.frequency = frequency;
      return this;
    }

    public TrainDataReplayerServer build(int servicePort) {
      LiveTrainDataReplayer liveTrainDataReplayer = new LiveTrainDataReplayer(kafkaConfig, "Test");
      PlannedTrainDataReplayer plannedTrainDataReplayer = new PlannedTrainDataReplayer(kafkaConfig, "Test");

      PostgresReplayer[] replayers = new PostgresReplayer[]{
          liveTrainDataReplayer,
          plannedTrainDataReplayer
      };

      for (PostgresReplayer replayer : replayers) {
        replayer.connect(databaseConfig);
        replayer.setFrequency(frequency);
        startTimestamp.ifPresent(replayer::setStartTime);
        endTimestamp.ifPresent(replayer::setEndTime);
      }
      return new TrainDataReplayerServer(replayers, servicePort);
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public TrainDataReplayerServer(PostgresReplayer replayer, int servicePort) {
    super(new TrainDataReplayerService(new PostgresReplayer[]{replayer}), servicePort);
  }

  public TrainDataReplayerServer(PostgresReplayer[] replayers, int servicePort) {
    super(new TrainDataReplayerService(replayers), servicePort);
  }

  /*
  public static TrainDataReplayerServer build(Properties properties, int servicePort) {
    LiveTrainDataReplayer liveTrainDataReplayer = new LiveTrainDataReplayer(properties);
    liveTrainDataReplayer.setTopic("Test");
    PlannedTrainDataReplayer plannedTrainDataReplayer = new PlannedTrainDataReplayer(properties);
    plannedTrainDataReplayer.setTopic("Test");

    PostgresReplayer[] replayers = new PostgresReplayer[]{
        liveTrainDataReplayer,
        plannedTrainDataReplayer
    };

    for (PostgresReplayer replayer : replayers) {
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
    }

    return new TrainDataReplayerServer(replayers, servicePort);
  }*/

  private static class TrainDataReplayerService
      extends org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerImplBase {
    private PostgresReplayer[] replayers;

    @FunctionalInterface
    public interface UnsafeFunction<T, R> {
      R apply(T t) throws Exception;
    }

    public TrainDataReplayerService(PostgresReplayer[] replayers) {
      this.replayers = replayers;
    }

    private Success forEachReplayer(UnsafeFunction<PostgresReplayer, Optional<? extends Success>> routine) {
      List<Success> received = new ArrayList<>();
      for (PostgresReplayer replayer : replayers) {
        try {
          routine.apply(replayer).map(received::add);
        } catch (Exception exception) {
          logger.error(exception.toString());
          received.add(Success.newBuilder().setSuccess(false).build());
        }
      }
      return Success.newBuilder().setSuccess(received.stream().allMatch(Success::getSuccess)).build();
    }

    @Override
    public void seekTo(
        org.bptlab.cepta.producers.replayer.Timestamp timestamp,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      logger.info(String.format("Seeking train data replayer to %s", timestamp.toString()));
      Success successful = forEachReplayer(replayer -> {
        replayer.reset(); return Optional.of(Success.newBuilder().setSuccess(true).build());});
      responseObserver.onNext(successful);
      responseObserver.onCompleted();
    }

    @Override
    public void reset(
        org.bptlab.cepta.producers.replayer.Empty request,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      logger.info("Resetting train data replayer server");
      Success successful = forEachReplayer(replayer -> {
        replayer.reset(); return Optional.of(Success.newBuilder().setSuccess(true).build());});
      responseObserver.onNext(successful);
      responseObserver.onCompleted();
    }

    @Override
    public void start(
        org.bptlab.cepta.producers.replayer.Empty request,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      logger.info("Starting train data replayer server");
      Success successful = forEachReplayer(replayer -> {
        try {
          replayer.start();
        } catch (NoDatabaseConnectionException exception) {
          logger.error("Could not connect to the database");
          throw exception;
        }
        return Optional.of(Success.newBuilder().setSuccess(true).build());
      });
      responseObserver.onNext(successful);
      responseObserver.onCompleted();
    }

    @Override
    public void stop(
        org.bptlab.cepta.producers.replayer.Empty request,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      logger.info("Stopping train data replayer server");
      Success successful = forEachReplayer(replayer -> {
        replayer.stop(); return Optional.of(Success.newBuilder().setSuccess(true).build());});
      responseObserver.onNext(successful);
      responseObserver.onCompleted();
    }
  }
}
