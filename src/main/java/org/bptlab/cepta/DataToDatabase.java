package org.bptlab.cepta;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
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
    String[] columnsAndValues = columnsAndValuesToString(dataSet);

    // Create query
    String query = "INSERT INTO " + table_name + columnsAndValues[0]
        + " VALUES " + columnsAndValues[1] + ";";
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
    // returns two strings
    // result[0] contains the query part string for columns
    // result[1] contains the query part string for the values
    // necessary for usage in the sql statement

    Class c = dataSet.getClass();

    // get attribute fields of dataSet's class and store them in fields
    // it only gets the fields of the class and not its superclasses, too
    Field[] fields = c.getDeclaredFields();

    // stores names of attributes
    String[] columns = new String[fields.length];
    String[] values = new String[fields.length];
    for(int i = 0; i < fields.length; i++){
      // get attribute-field of class for column-name
      Field f = fields[i];

      // add object's value of attribute/column to arrays
      try{
        java.lang.Object value = f.get(dataSet);
        if(value instanceof String){
          // add ' ' around value if it's a string
          values[i] = String.format("'%s'", value.toString());
        }else{
          values[i] = value.toString();
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

    // generate strings for columns and values
    String valuesString = arrayToString(values);
    String columnsString = arrayToString(columns);
    String[] result = {columnsString, valuesString};
    return result;
  }

  private String arrayToString(String[] array){
    // takes the array's elements and converts them to a "(val1, val2, ...)" String
    // necessary for usage in the sql statement
    String string;

    // remove null values
    ArrayList<String> elements = new ArrayList<>(Arrays.asList(array));;
    elements.removeIf(Objects::isNull);

    // add , between elements
    string = String.join(",", elements);

    // surround it with brackets
    string = String.format("(%s)", string);
    return string;
  }
}
