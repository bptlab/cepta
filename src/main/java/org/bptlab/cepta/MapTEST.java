package org.bptlab.cepta;

import com.github.jasync.sql.db.*;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import org.apache.flink.api.common.functions.MapFunction;
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

class MapTEST extends RichAsyncFunction<String, Tuple2<String, Integer>> {
  //this must be set to transient, as flink will otherwise try to serialize it which it is not
  private transient ConnectionPool<PostgreSQLConnection> connection;

  @Override
  public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
    //the Configuration class must be from flink, it will give errors when jasync's Configuration is taken
    //open should be called before methods like map() or join() are executed
    super.open(parameters);
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
    System.out.println(connection.toString());
  }

  /*
  Close gets called after the last thing went through this processing node.
  I would expect doing some cleanup like closing the connectionPools is a good idea, but
  apparently this leads to errors, since Flink still sends requests after close was called.<
  (atleast I think so)
   */
  @Override
  public void close() throws Exception {
    //connection.disconnect().get();
    super.close();
  }

  @Override
  public void asyncInvoke(String key, final ResultFuture<Tuple2<String, Integer>> resultFuture) throws Exception {
        /*
        asyncInvoke will be called for each incoming element
        the resultFuture is where the outgoing element will be
         */
    final String cleanedKey = key.replace("'", "''");
    final CompletableFuture<QueryResult> future = connection.sendPreparedStatement(
        "SELECT count(id) FROM movie INNER JOIN actor ON actor.movie_id = movie.mid WHERE actor.name LIKE '" + cleanedKey + "';");

        /*
        We create a new CompletableFuture which will be automatically and asynchronly done with the value
        from the given supplier.
         */
    CompletableFuture.supplyAsync(new Supplier<Long>() {
      @Override
      public Long get() {
        try {
          QueryResult queryResult = future.get();
          return queryResult.getRows().get(0).getLong(0);
        } catch (NullPointerException | InterruptedException | ExecutionException e) {
          System.err.println(e.getMessage());
          return null;
        }
      }

    }).thenAccept( (Long dbResult) -> {
            /*
            After the CompletableFuture is completed, the .thenAccept call will be made with the value from the CompletableFuture.
            We can use this value to set our return value into the function return (returnFuture).
             */
      resultFuture.complete(Collections.singleton(new Tuple2<>(cleanedKey, dbResult.intValue())));
    });
  }

}

class flinkWithJasyncTest {
  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    streamExecutionEnvironment.setParallelism(6);
//set the path
    final TextInputFormat format = new TextInputFormat(new Path("./actors_id.txt"));

    //set the path
    DataStream<String> actorNamesStream = streamExecutionEnvironment.readFile(format, "./actors_id.txt");
    /*DataStream<Integer> actorIdStream = actorNamesStream.map(new MapFunction<String, Integer>() {
      @Override
      public Integer map(String s) throws Exception {
        return Integer.parseInt(s);
      }
    });*/
        /*
        This is the same as the following line:
            DataStream<String> actorNamesStream = streamExecutionEnvironment.readTextFile("/home/vincent/Documents/WiSe1920/BP/all_actors.txt");
        However, the readTextFile method is deprecated (but still works fine)
         */

    DataStream<Tuple2<String, Integer>> resultStream =
        AsyncDataStream.unorderedWait(actorNamesStream,
            new MapTEST(),
            10000,
            TimeUnit.MILLISECONDS,
            5);

    resultStream.print();
    streamExecutionEnvironment.execute();
  }
}
