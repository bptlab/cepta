package org.bptlab.cepta.operators;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.RowData;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.Locale;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.config.PostgresConfig;

public class WeatherLocationCorrelationFunction extends
    RichAsyncFunction<WeatherData, Tuple2<WeatherData, Integer> > {

  private PostgresConfig postgresConfig = new PostgresConfig();


  private transient ConnectionPool<PostgreSQLConnection> connection;

  public WeatherLocationCorrelationFunction(PostgresConfig postgresConfig) {
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
    config.setMaxActiveConnections(1);
    connection = PostgreSQLConnectionBuilder.createConnectionPool(config);
  }

  @Override
  public void close() throws Exception {
    connection.disconnect();
  }

  @Override
  public void asyncInvoke(WeatherData weatherEvent,
      final ResultFuture<Tuple2<WeatherData, Integer>> resultFuture) throws Exception {
    /*
       0.02 is about 2 kilometers
    */
    String query = String
        .format(Locale.US,"select id from public.location where "
                + "%f - 0.02 < CAST(lat AS float)  and CAST(lat AS float) < %f + 0.02 and "
                + "%f - 0.02 < CAST(lon AS float)  and CAST(lon AS float) < %f + 0.02;",
            weatherEvent.getLatitude(),  weatherEvent.getLatitude(),
            weatherEvent.getLongitude(), weatherEvent.getLongitude());
    System.out.println("Query: " + query);
    final CompletableFuture<QueryResult> future = connection.sendPreparedStatement(query);

    CompletableFuture.supplyAsync(new Supplier<ArrayList<Tuple2<WeatherData, Integer>>>() {
      @Override
      public ArrayList<Tuple2<WeatherData, Integer>> get() {
        try {
          QueryResult queryResult = future.get();
          ArrayList<Tuple2<WeatherData, Integer>> notifications = new ArrayList<>();
          
          for (RowData row : queryResult.getRows()){
            notifications.add(new Tuple2<>(weatherEvent, row.getInt("id")));
          }
          System.out.println(notifications);
          return notifications;
        } catch (NullPointerException | InterruptedException | ExecutionException e) {
          System.err.println(e.getMessage());
          return null;
        }
      }
    }).thenAccept(resultFuture::complete);
  }
}
