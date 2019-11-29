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

public class PlannedDataToDatabase<PlannedTrainData> implements MapFunction<PlannedTrainData, PlannedTrainData> {
  static final String HOST = "localhost";
  static final String DATABASE = "dbs1_imdb";
  static final String USER = "tester";
  static final String PASSWORD = "password";
  static final Integer PORT = 5432;
  static final String TABLE = "actor";
  static final String[] COLUMNS = {"id", "name"};

  @Override
  public PlannedTrainData map(PlannedTrainData plannedTrainDataSet) throws Exception {
    insert(plannedTrainDataSet);
    return plannedTrainDataSet;
  }
  public void insert(PlannedTrainData plannedTrainDataSet)
      throws NoSuchFieldException, IllegalAccessException {

    // Connection to PostgreSQL DB
    ConnectionPool<PostgreSQLConnection> connection;

    ConnectionPoolConfigurationBuilder config = new ConnectionPoolConfigurationBuilder();
    config.setUsername(USER);
    config.setPassword(PASSWORD);
    config.setHost(HOST);
    config.setPort(PORT);
    config.setDatabase(DATABASE);
    config.setMaxActiveConnections(100);
    connection = PostgreSQLConnectionBuilder.createConnectionPool(config);

    String[] colsAndVals = columnsAndValuesToString(plannedTrainDataSet, COLUMNS);

    // Create query
    String query = "INSERT INTO " + TABLE + colsAndVals[0]
        + " VALUES " + colsAndVals[1] + ";";
    System.out.println(query);
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
    // takes the columns and converts them to a (col1, col2, ...) String
    // necessary for usage in the sql statement
    String colString = "(";
    for(int i = 0; i < cols.length-1; ++i){
      if(!cols[i].equals("NONE")){
        colString += cols[i] + ", ";
      }
    }
    if (!cols[cols.length-1].equals("NONE")){
      colString += cols[cols.length-1];
    }else {
      // crop last ' ' and ,
      colString = colString.substring(0, colString.length()-2);
    }
    colString += ")";
    return colString;
  }

  private String[] columnsAndValuesToString(PlannedTrainData plannedTrainDataSet, String[] cols)
      throws NoSuchFieldException, IllegalAccessException {
    // takes the values matching to columns and converts them to
    // a (val1, val2, ...) String
    // necessary for usage in the sql statement
    // maybe we can put this into the data class where we can do this in a prettier way

    String valString = "(";
    Class c = plannedTrainDataSet.getClass();
    for(int i = 0; i < cols.length-1; i++){
      // get attribute-field of class for column-name
      Field f = c.getField(cols[i]);
      f.setAccessible(true);

      // add object's value of attribute/column to string
      // with , in between
      try{
        if(f.getType().equals(" ".getClass())){
          // add ' ' around value if it's a string
          valString += "'" + f.get(plannedTrainDataSet).toString() + "'" + ", ";
        }else{
          valString += f.get(plannedTrainDataSet).toString() + ", ";
        }
      }catch (NullPointerException e){
        cols[i] = "NONE";
        System.out.println("Col " + String.valueOf(i) + " is empty");
      }
    }

    // do the same for the last element but with ) instead of ,
    // for closing the brackets
    System.out.println("Col " + String.valueOf(cols.length-1) + " is the last");
    Field f;
    try {
      f = c.getField(cols[cols.length-1]);
    }catch (NoSuchFieldException e){
      throw e;
    }

    f.setAccessible(true);
    try{
      if(f.getType().equals(" ".getClass())){
        valString += "'" + f.get(plannedTrainDataSet).toString() + "'" + ")";
      }else{
        valString += f.get(plannedTrainDataSet).toString() + ")";
      }
    }catch (NullPointerException e){
      cols[cols.length-1] = "NONE";
      System.out.println("Col " + String.valueOf(cols.length-1) + " is empty");
      valString = valString.substring(0, valString.length()-2) + ")";
      System.out.println(valString);
    }
    String colString = columnsToString(cols);
    String[] result = {colString, valString};
    return result;
  }


}
