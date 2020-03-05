package org.bptlab.cepta.operators;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
// import com.github.jasync.sql.db.postgresql.RowData;
import com.github.jasync.sql.db.RowData;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.utils.converters.PlannedTrainDataDatabaseConverter;

public class LivePlannedCorrelationFunction extends
    RichAsyncFunction<LiveTrainData, Tuple2<LiveTrainData, PlannedTrainData>> {

  private PostgresConfig postgresConfig = new PostgresConfig();
  private transient ConnectionPool<PostgreSQLConnection> connection;

  public LivePlannedCorrelationFunction(PostgresConfig postgresConfig) {
    this.postgresConfig = postgresConfig;
  }

  public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
    /*
      the Configuration class must be from flink, it will give errors when jasync's Configuration is taken
      open should be called before methods like map() or join() are executed
      this must be set to transient, as flink will otherwise try to serialize it which it is not
     */
    super.open(parameters);
    ConnectionPoolConfigurationBuilder config = new ConnectionPoolConfigurationBuilder();
    config.setUsername(postgresConfig.getUser());
    config.setPassword(postgresConfig.getPassword());
    config.setHost(postgresConfig.getHost());
    config.setPort(postgresConfig.getPort());
    config.setDatabase(postgresConfig.getName());
    // Having the same maximum amount of connections as concurrent asynchronous requests seems to work
    config.setMaxActiveConnections(12);
    connection = PostgreSQLConnectionBuilder.createConnectionPool(config);
  }

  @Override
  public void close() throws Exception {
    connection.disconnect();
  }

  @Override
  public void asyncInvoke(LiveTrainData liveEvent,
      final ResultFuture<Tuple2<LiveTrainData, PlannedTrainData>> resultFuture) throws Exception {

    /*
       asyncInvoke will be called for each incoming element
       the resultFuture is where the outgoing element will be
    */
    String query = String
        .format("select * from public.planned where train_id = %d and location_id = %d;",
            liveEvent.getTrainId(), liveEvent.getLocationId());
    final CompletableFuture<QueryResult> future = connection.sendPreparedStatement(query);

    /*
      We create a new CompletableFuture which will be automatically and asynchronously done with the value
      from the given supplier.
    */
    CompletableFuture.supplyAsync(new Supplier<PlannedTrainData>() {
      @Override
      public PlannedTrainData get() {
        try {
          QueryResult queryResult = future.get();
          RowData firstMatch = queryResult.getRows().get(0);
          return new PlannedTrainDataDatabaseConverter().fromRowData(firstMatch);
        } catch (NullPointerException | InterruptedException | ExecutionException e) {
          System.err.println(e.getMessage());
          return null;
        }
      }
    }).thenAccept((PlannedTrainData dbResult) -> {
      /*
        After the CompletableFuture is completed, the .thenAccept call will be made with the value from the CompletableFuture.
        We can use this value to set our return value into the function return (returnFuture).
      */
      resultFuture.complete(Collections.singleton(new Tuple2<>(liveEvent, dbResult)));
    });
  }
}
