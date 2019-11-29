package org.bptlab.cepta;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.flink.api.common.functions.MapFunction;

public class PlannedDataToDatabase<Actor> implements MapFunction<Actor, Actor> {
  static final String HOST = "localhost";
  static final String DATABASE = "dbs1_imdb";
  static final String USER = "tester";
  static final String PASSWORD = "password";
  static final Integer PORT = 5432;
  static final String TABLE = "actor";
  static final String[] COLUMNS = {"id", "name"};

  @Override
  public Actor map(Actor actor) throws Exception {
    insert(actor);
    return actor;
  }
  public void insert(Actor plannedDataSet)
      throws NoSuchFieldException, IllegalAccessException {
/*

    // Connection to PostgreSQL DB
    Connection connection = PostgreSQLConnectionBuilder.createConnectionPool(
        "jdbc:postgresql://" + HOST + ":" + PORT.toString() + "/" + DATABASE + "?user=" + USER + "&password=" + PASSWORD);
*/
    ConnectionPool<PostgreSQLConnection> connection;

    ConnectionPoolConfigurationBuilder config = new ConnectionPoolConfigurationBuilder();
    config.setUsername(USER);
    config.setPassword(PASSWORD);
    config.setHost(HOST);
    config.setPort(PORT);
    config.setDatabase(DATABASE);
    config.setMaxActiveConnections(100);
    connection = PostgreSQLConnectionBuilder.createConnectionPool(config);

    // Create query
    String query = "INSERT INTO " + TABLE + columnsToString(COLUMNS)
        + " VALUES " + valuesToString(plannedDataSet, COLUMNS) + ";";

    CompletableFuture<QueryResult> future = connection.sendPreparedStatement(query);

    // execute query and check for result
    try{
      QueryResult result = future.get();
    }catch (InterruptedException | ExecutionException e){
      System.out.println("Could not get result");
    }

    // Close the connection pool
    try{
      connection.disconnect().get();
    }catch (ExecutionException | InterruptedException e){
      System.out.println("Could not disconnect");
    }
  }

  private String columnsToString(String[] cols){
    String colString = "(";
    for(int i = 0; i < cols.length-1; ++i){
      colString += cols[i] + ", ";
    }
    colString += cols[cols.length-1] + ")";
    return colString;
  }

  private String valuesToString(Actor obj, String[] cols)
      throws NoSuchFieldException, IllegalAccessException {
    // get values of columns for object
    // maybe we can put this into the data class where we can do this in a prettier way

    String valString = "(";
    Class c = obj.getClass();
    for(int i = 0; i < cols.length-1; ++i){
      Field f = c.getField(cols[i]);
      f.setAccessible(true);
      if(f.getType().equals(" ".getClass())){
        valString += "'" + f.get(obj).toString() + "'" + ", ";
      }else{
        valString += f.get(obj).toString() + ", ";
      }
    }

    Field f = c.getField(cols[cols.length-1]);
    f.setAccessible(true);
    if(f.getType().equals(" ".getClass())){
      valString += "'" + f.get(obj).toString() + "'" + ")";
    }else{
      valString += f.get(obj).toString() + ")";
    }
    return valString;
  }


}
