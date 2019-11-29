package org.bptlab.cepta;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
  public Object map(Object dataSet) throws Exception {
    insert(dataSet);
    return dataSet;
  }
  public void insert(Object dataSet)
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

    // stores strings of values and columns for sql query
    String[] colsAndVals = columnsAndValuesToString(dataSet);

    // Create query
    String query = "INSERT INTO " + table_name + colsAndVals[0]
        + " VALUES " + colsAndVals[1] + ";";
    System.out.println(query);

    // send query
    CompletableFuture<QueryResult> future = connection.sendPreparedStatement(query);

    // really execute query and get query result
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

  private String[] columnsAndValuesToString(Object dataSet)
      throws NoSuchFieldException, IllegalAccessException {
    // takes the values matching to columns and converts them to
    // a (val1, val2, ...) String
    // necessary for usage in the sql statement

    String valString = "(";
    Class c = dataSet.getClass();

    // get attribute fields of dataSet's class and store them in fields
    Field[] fields = c.getDeclaredFields();

    // stores names of attributes
    String[] columns = new String[fields.length];
/*
    String[] columns = new String[fields.length];
    for(int i = 0; i < fields.length; ++i){
      columns[i] = fields[i].getName();
    }
*/

    for(int i = 0; i < fields.length-1; i++){
      // get attribute-field of class for column-name
      Field f = fields[i];
      f.setAccessible(true);

      // add object's value of attribute/column to string
      // with , in between
      try{
        if(f.getType().equals(" ".getClass())){
          // add ' ' around value if it's a string
          valString += "'" + f.get(dataSet).toString() + "'" + ", ";
        }else{
          valString += f.get(dataSet).toString() + ", ";
        }
        columns[i] = f.getName();
      }catch (NullPointerException e){
        /*
         just go on with the for loop because there is no value (it's null)
         we want to add to the database and so we don't need he column and
         the value in our sql statement
        */
      }
    }

    // do the same for the last element but with ) instead of ,
    // for closing the brackets
    Field f = fields[fields.length-1];
    f.setAccessible(true);
    try{
      if(f.getType().equals(" ".getClass())){
        valString += "'" + f.get(dataSet).toString() + "'" + ")";
      }else{
        valString += f.get(dataSet).toString() + ")";
      }
    }catch (NullPointerException e){
      // crop last ', ' because we don't need it as we do not have another element
      valString = valString.substring(0, valString.length()-2) + ")";
    }

    // generate string for columns
    String colString = columnsToString(columns);
    String[] result = {colString, valString};
    return result;
  }

  private String columnsToString(String[] cols){
    // takes the columns and converts them to a "(col1, col2, ...)" String
    // necessary for usage in the sql statement

    /*
    * the null checks are necessary because we do not fill the columns array
    * when there is no value belonging to this attribute
    * */
    String colString = "(";
    for(int i = 0; i < cols.length-1; ++i){
      if (cols[i] != null){
        colString += cols[i] + ", ";
      }
    }
    if(cols[cols.length-1] != null){
      colString += cols[cols.length-1];
    }else {
      // crop the last ', ' because there is no next element
      colString = colString.substring(0, colString.length()-2);
    }

    return colString + ")";
  }
}
