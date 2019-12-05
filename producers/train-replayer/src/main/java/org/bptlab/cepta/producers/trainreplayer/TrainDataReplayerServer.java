package org.bptlab.cepta.producers.trainreplayer;

import io.grpc.stub.StreamObserver;
import org.bptlab.cepta.producers.PostgresReplayer;
import org.bptlab.cepta.utils.grpc.GrpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainDataReplayerServer
    extends GrpcServer<org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerImplBase> {
  private static final Logger logger =
      LoggerFactory.getLogger(TrainDataReplayerServer.class.getName());

  /*
  public ReplayerServer(
      Properties props,
      int servicePort,
      String connector,
      String protocol,
      String host,
      int port,
      String database,
      String user,
      String password)
      throws IOException {
    super(
        props,
        ReplayerService::new,
        servicePort,
        connector,
        protocol,
        host,
        port,
        database,
        user,
        password);
  }

  public ReplayerServer(Properties props, int servicePort) throws IOException {
    super(props, ReplayerService::new, servicePort);
  }
   */
  public TrainDataReplayerServer(PostgresReplayer replayer, int servicePort) {
    super(new TrainDataReplayerService(replayer), servicePort);
  }

  private static class TrainDataReplayerService
      extends org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerImplBase {
    private PostgresReplayer replayer;

    public TrainDataReplayerService(PostgresReplayer replayer) {
      this.replayer = replayer;
    }

    @Override
    public void seekTo(
        org.bptlab.cepta.producers.replayer.Timestamp timestamp,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      // timestamp
      responseObserver.onNext(org.bptlab.cepta.producers.replayer.Success.newBuilder().build());
      responseObserver.onCompleted();
    }

    @Override
    public void reset(
        org.bptlab.cepta.producers.replayer.Empty request,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      try {
        replayer.reset();
      } catch (Exception exception) {
        logger.error("");
      }
      responseObserver.onNext(org.bptlab.cepta.producers.replayer.Success.newBuilder().build());
      responseObserver.onCompleted();
    }

    @Override
    public void start(
        org.bptlab.cepta.producers.replayer.Empty request,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      try {
        logger.info("Starting replayer server");
        replayer.start();
      } catch (NoDatabaseConnectionException exception) {
        logger.error("Could not connect to the database");
      } catch (Exception exception) {
        logger.error("Could not connect to the database");
      }
      responseObserver.onNext(org.bptlab.cepta.producers.replayer.Success.newBuilder().build());
      responseObserver.onCompleted();
    }

    @Override
    public void stop(
        org.bptlab.cepta.producers.replayer.Empty request,
        StreamObserver<org.bptlab.cepta.producers.replayer.Success> responseObserver) {
      responseObserver.onNext(org.bptlab.cepta.producers.replayer.Success.newBuilder().build());
      replayer.stop();
      responseObserver.onCompleted();
    }
  }

  /*
  TrainDataRunningProducer(
      Properties props,
      String connector,
      String protocol,
      String host,
      int port,
      String database,
      String user,
      String password) {
    super(props);
    this.connect(connector, protocol, host, port, database, user, password);
  }

  public TrainDataRunningProducer(Properties props) {
    super(props);
  }

  public void start() throws NoDatabaseConnectionException {
    if (!connected) {
      throw new NoDatabaseConnectionException("Not connected to database");
    }
    this.running = true;
    try {
      String query = buildReplayQuery();
      Statement getNextEntryStatement = connection.createStatement();
      ResultSet result = getNextEntryStatement.executeQuery(query);

      while (result.next()) {
        System.out.println(result.getString("pointtime"));
        TrainDataRunning event = getTrainDataRunning(result);
        ProducerRecord<Long, TrainDataRunning> record =
            new ProducerRecord<Long, TrainDataRunning>(topic, event);

        try {
          RecordMetadata metadata = this.send(record).get();
          System.out.printf(
              "sent record(key=%s value=%s) " + "meta(partition=%d, offset=%d)\n",
              record.key(), record.value(), metadata.partition(), metadata.offset());
          this.flush();
          Thread.sleep(this.frequency);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
      System.out.println("There is no new Train Data left in the database. Exiting.");
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      this.close();
    }
  }

  public void stop() {
    this.running = false;
  }

  public void connect(
      String connector,
      String protocol,
      String host,
      int port,
      String database,
      String user,
      String password) {
    String url = String.format("%s:%s://%s:%d/%s", connector, protocol, host, port, database);
    try {
      connection = DriverManager.getConnection(url, user, password);
      connected = true;
    } catch (SQLException e) {
      System.out.println("SQLException: Connection could not be established.");
      e.printStackTrace();
    }
  }

  private static TrainDataRunning getTrainDataRunning(ResultSet result) {
    TrainDataRunning trainDataRunning = new TrainDataRunning();

    try {
      trainDataRunning.setId(result.getInt("id"));
      trainDataRunning.setTrainid(result.getInt("trainid"));
      trainDataRunning.setPointid(result.getInt("pointid"));
      trainDataRunning.setPointtime(result.getLong("pointtime"));
      trainDataRunning.setPointstatus(result.getInt("pointstatus"));
      trainDataRunning.setServicenb(result.getInt("servicenb"));
      trainDataRunning.setServiceidtime(result.getLong("serviceidtime"));
      trainDataRunning.setReferencenb(result.getInt("referencenb"));
      trainDataRunning.setReferenceidtime(result.getLong("referenceidtime"));
      trainDataRunning.setDelta(result.getInt("delta"));
      trainDataRunning.setTransferpointid(result.getInt("transferpointid"));
      trainDataRunning.setReportingim(result.getInt("reportingim"));
      trainDataRunning.setNextim(result.getInt("nextim"));
      trainDataRunning.setStatus(result.getInt("status"));
      trainDataRunning.setPosted(result.getInt("posted"));
      trainDataRunning.setTimestamp(result.getInt("timestamp"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return trainDataRunning;
  }
  */
}
