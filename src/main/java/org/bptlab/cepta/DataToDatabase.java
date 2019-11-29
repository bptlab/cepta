package org.bptlab.cepta;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.flink.api.common.functions.MapFunction;

public class DataToDatabase<Object> implements MapFunction<Object, Object> {
  static final String HOST = "localhost";
  static final String DATABASE = "dbs1_imdb";
  static final String USER = "tester";
  static final String PASSWORD = "password";
  static final Integer PORT = 5432;
  private String table_name;
  public DataToDatabase(String table){
    this.table_name = table;
  }
  @Override
  public Object map(Object ObjectSet) throws Exception {
    insert(ObjectSet);
    return ObjectSet;
  }
  public void insert(Object ObjectSet)
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

    String[] colsAndVals = columnsAndValuesToString(ObjectSet);

    // Create query
    String query = "INSERT INTO " + table_name + colsAndVals[0]
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

  private String[] columnsAndValuesToString(Object ObjectSet)
      throws NoSuchFieldException, IllegalAccessException {
    // takes the values matching to columns and converts them to
    // a (val1, val2, ...) String
    // necessary for usage in the sql statement
    // maybe we can put this into the data class where we can do this in a prettier way

    String valString = "(";
    Class c = ObjectSet.getClass();
    Field[] fields = c.getDeclaredFields();
    String[] columns = new String[fields.length];
    for(int i = 0; i < fields.length; ++i){
      columns[i] = fields[i].getName();
    }
    System.out.println("COLUMNS");
    for (String col : columns){
      System.out.println(col);
    }

    for(int i = 0; i < columns.length-1; i++){
      // get attribute-field of class for column-name
      Field f = c.getField(columns[i]);
      f.setAccessible(true);

      // add object's value of attribute/column to string
      // with , in between
      try{
        if(f.getType().equals(" ".getClass())){
          // add ' ' around value if it's a string
          valString += "'" + f.get(ObjectSet).toString() + "'" + ", ";
        }else{
          valString += f.get(ObjectSet).toString() + ", ";
        }
      }catch (NullPointerException e){
        columns[i] = "NONE";
      }
    }

    // do the same for the last element but with ) instead of ,
    // for closing the brackets
    Field f;
    try {
      f = c.getField(columns[columns.length-1]);
    }catch (NoSuchFieldException e){
      throw e;
    }

    f.setAccessible(true);
    try{
      if(f.getType().equals(" ".getClass())){
        valString += "'" + f.get(ObjectSet).toString() + "'" + ")";
      }else{
        valString += f.get(ObjectSet).toString() + ")";
      }
    }catch (NullPointerException e){
      columns[columns.length-1] = "NONE";
      valString = valString.substring(0, valString.length()-2) + ")";
    }
    String colString = columnsToString(columns);
    String[] result = {colString, valString};
    return result;
  }


}
