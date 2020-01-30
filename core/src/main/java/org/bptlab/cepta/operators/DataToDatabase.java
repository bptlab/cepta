package org.bptlab.cepta.operators;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Descriptors.FieldDescriptor;
// import org.apache.avro.Schema;
// import org.apache.avro.specific.SpecificRecord;
import org.apache.flink.api.common.functions.MapFunction;

public class DataToDatabase<T extends GeneratedMessage> implements MapFunction<T, T> {
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
  public T map(T dataSet) throws Exception {
    insert(dataSet);
    return dataSet;
  }

  public void insert(T dataSet)
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

    /* get column names from avro schema of data class
    Schema avroSchema = dataSet.getSchema();
    List<Schema.Field> fields = avroSchema.getFields();
    String[] columnNames = new String[fields.size()];
    for(int i = 0; i < fields.size(); i++){
      columnNames[i] = fields.get(i).name();
    }
    */
    /*
    for(int i = 0; i < dataSet.field_count(); i++) {
      columnNames[i] = descriptor->field(i).name();
    }
    */
    List<String> columnNames = new ArrayList<String>();
    for (Map.Entry<FieldDescriptor,java.lang.Object> entry : dataSet.getAllFields().entrySet()) {
      columnNames.add(entry.getKey().getName());
    }

    // store strings of values and columns for sql query
    // String valuesString = valuesToQueryString(dataSet, columnNames);
    String insertionValues = arrayToQueryString(getValuesOfProtoMessage(dataSet));
    String columnsString = arrayToQueryString(columnNames);

    // Create query
    String query = "INSERT INTO " + table_name + columnsString
        + " VALUES " + insertionValues + ";";
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

  private List<String> getValuesOfProtoMessage(T dataSet) throws NoSuchFieldException, IllegalAccessException {
    List<String> values = new ArrayList<String>();
    // returns the query part string for the values
    // necessary for usage in the sql statement
    /*
    Class c = dataSet.getClass();

    // get attribute fields of dataSet's class and store them in fields
    // it only gets the fields of the class and not its superclasses, too

    // stores names of attributes
    String[] values = new String[columns.length];
    for(int i = 0; i < columns.length; i++){
      String column = columns[i];

      // get attribute-field of class for column-name
      Field f = c.getDeclaredField(column);
      // set accessible so we can access private attributes
      f.setAccessible(true);

      // add object's value of attribute/column to arrays
      try{
        Object value = f.get(dataSet);
        if(value instanceof String){
          // add ' ' around value if it's a string
          values[i] = String.format("'%s'", value.toString());
        }else{
          values[i] = value.toString();
        }
      }catch (NullPointerException e){
        // set null so we do not have this column in the query because there is no value to add there
        columns[i] = null;
      }
    }
    */
    // String[] values = new String[columns.length];
    for (Map.Entry<FieldDescriptor,java.lang.Object> entry : dataSet.getAllFields().entrySet()) {
      System.out.println(entry.getKey() + "/" + entry.getValue());
      if(entry.getValue() instanceof String){
        // add ' ' around value if it's a string
        values.add(String.format("'%s'", entry.getValue().toString()));
      }else{
        values.add(entry.getValue().toString());
      }
    }
    return values;
  }

  private String arrayToQueryString(List<String> elements){
    // takes the array's elements and converts them to a "(val1, val2, ...)" String
    // necessary for usage in the sql statement
    String string;

    // remove null values
    // ArrayList<String> elements = new ArrayList<>(Arrays.asList(array));;
    elements.removeIf(Objects::isNull);

    // add , between elements
    string = String.join(",", elements);

    // surround it with brackets
    string = String.format("(%s)", string);
    return string;
  }
}
