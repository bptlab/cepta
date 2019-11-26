package org.bptlab.cepta;

/*import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;*/

import com.github.jasync.sql.db.*;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MapEventToActor extends RichAsyncFunction<Integer, String> {

  private transient ConnectionPool<PostgreSQLConnection> connection;

  public void open(org.apache.flink.configuration.Configuration parameters) {
    //the Configuration class must be from flink, it will give errors when jasync's Configuration is taken
    //open should be called before methods like map() or join() are executed
    try {
      super.open(parameters);
    }catch (Exception e){
      System.out.println("Connection Error");
    }
    ConnectionPoolConfigurationBuilder config = new ConnectionPoolConfigurationBuilder();
    config.setUsername("tester");
    config.setPassword("password");
    config.setHost("localhost");
    config.setPort(5432);
    config.setDatabase("dbs1_imdb");
  
          /*
          Having the same maximum amount of connections as concurrent asynchronous requests seems to work
           */

    config.setMaxActiveConnections(100);
    connection = PostgreSQLConnectionBuilder.createConnectionPool(config);
  }

  @Override
  public void close() throws Exception {
    //connection.disconnect().get();
    super.close();
  }

  @Override
  public void asyncInvoke(Integer key, final ResultFuture<String> resultFuture){
  
          /*
          asyncInvoke will be called for each incoming element
          the resultFuture is where the outgoing element will be
           */
    final CompletableFuture<QueryResult> future = connection.sendPreparedStatement(
        "SELECT name FROM actor WHERE actor.id = " + key + ";");

          /*
          We create a new CompletableFuture which will be automatically and asynchronly done with the value
          from the given supplier.
           */
    CompletableFuture.supplyAsync(new Supplier<String>() {
      @Override
      public String get() {
        try {
          System.out.println("hier");
          QueryResult queryResult = future.get();
          System.out.println("Result: " + queryResult.toString());
          return queryResult.getRows().get(0).getString(0);
        } catch (NullPointerException | InterruptedException | ExecutionException e) {
          System.err.println(e.getMessage());
          return null;
        }
      }

    }).thenAccept( (String dbResult) -> {
              /*
              After the CompletableFuture is completed, the .thenAccept call will be made with the value from the CompletableFuture.
              We can use this value to set our return value into the function return (returnFuture).
               */
              System.out.println("ERGEBNIS: " + dbResult.toString());
      resultFuture.complete(Collections.singleton(dbResult.toString()));
    });
  }

}