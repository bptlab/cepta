package org.bptlab.cepta.operators;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.protobuf.Message;
import org.apache.flink.api.common.functions.MapFunction;
import org.bptlab.cepta.config.PostgresConfig;

import org.bptlab.cepta.utils.database.Util;
import org.bptlab.cepta.utils.database.Util.ProtoInfoStrings;

/*Depricated: Uploads received Events into a PostgresDatabase
  with Custom prototimestamp Conversion*/
public class DataToPostgresDatabase<T extends Message> implements MapFunction<T, T> {

  private String table_name;
  private PostgresConfig postgresConfig = new PostgresConfig();

  @Deprecated
  public DataToPostgresDatabase(String table, PostgresConfig postgresConfig){
    this.table_name = table;
    this.postgresConfig = postgresConfig;
  }

  @Override
  public T map(T dataSet) throws Exception {
    insert(dataSet);
    return dataSet;
  }


  private String getCreatePlannedDatabaseQuery(ArrayList<String> columnNames, ArrayList<String> types){
    String query = "CREATE TABLE public."+table_name+" ( ";

    for (int index = 0; index < columnNames.size(); index++) {
      try {
        query = query.concat(columnNames.get(index)+" "+types.get(index));
        if (index != columnNames.size()-1){
          query = query.concat(", ");
        }
      } catch (Exception e) {
        System.out.println("Types is out of Bounds mismatch between columnNames.size() and types.size()");
      }
    }
    query = query.concat(")");
    return query;
  }

  public boolean createSchema(ConnectionPool<PostgreSQLConnection> connection, ArrayList<String> columnNames, ArrayList<String> types/*, T message*/) {
    String query;
//    if (message instanceof PlannedTrainDataOuterClass.PlannedTrainData) {
      query = getCreatePlannedDatabaseQuery(columnNames, types);
//    } else if (message instanceof LocationDataOuterClass.LocationData) {
//      query = getCreateLocationDatabaseQuery(columnNames, types);
//    }
    // send query
    CompletableFuture<QueryResult> future = connection.sendPreparedStatement(query);
    boolean success =true;
    // really execute query and get query result
    try{
      QueryResult result = future.get();
    }catch (InterruptedException | ExecutionException  e){
      System.out.println("Could not create Schema: public."+table_name+" may already exist.");
      //Async call may take longer so the next element wants to create the schema too and fails cause it already exists
      //to prevent event loss try again even if schema creation failed.
      //success = false;
    }
    return success;
  }

  //TODO: change void to boolean that reflects whether the collection was changed
  public void insert(T dataSet)
      throws NoSuchFieldException, IllegalAccessException {

    // Connection to PostgreSQL DB <- this might me better on server startup?
    ConnectionPool<PostgreSQLConnection> connection;

    ConnectionPoolConfigurationBuilder config = new ConnectionPoolConfigurationBuilder();

    config.setUsername(postgresConfig.getUser());
    config.setPassword(postgresConfig.getPassword());
    config.setHost(postgresConfig.getHost());
    config.setPort(postgresConfig.getPort());
    config.setDatabase(postgresConfig.getName());
    config.setMaxActiveConnections(100);
    connection = PostgreSQLConnectionBuilder.createConnectionPool(config);

    // store strings of values, columns and types for sql query
    // Triplet<>(columnNames,values,types)
    ProtoInfoStrings protoInfo = Util.getInfosOfProtoMessageAsStrings(dataSet);
    String columnsString = arrayToQueryString(protoInfo.getColumnNames());
    String insertionValues = arrayToQueryString(protoInfo.getValues());

    // Create query
    String insertion_query = "INSERT INTO " + "public."+table_name + columnsString
        + " VALUES " + insertionValues + ";";
    System.out.println(insertion_query);

    // send query
    CompletableFuture<QueryResult> future = connection.sendPreparedStatement(insertion_query);

    // really execute query and get query result
    try{
      QueryResult result = future.get();
    }catch (InterruptedException | ExecutionException e){
      System.out.println("Could not get result, check for missing schema");
        // check if schema was missing -> create and retry
      if(createSchema(connection,protoInfo.getColumnNames(), protoInfo.getTypes()/*, dataSet*/)){
        future = connection.sendPreparedStatement(insertion_query);
        try {
          QueryResult result = future.get();
        }catch (InterruptedException | ExecutionException e1){
          System.out.println("Finally could not get result");
        }
      }
    }

    // Close the connection pool <- this should happen on server shutdown not each time
    try{
      connection.disconnect().get();
    }catch (ExecutionException | InterruptedException e){
      System.out.println("Could not disconnect");
    }
  }

  private String arrayToQueryString(List<String> elements){
    // takes the array's elements and converts them to a "(val1, val2, ...)" String
    // necessary for usage in the sql statement
    String string;

    // remove null values
    elements.removeIf(Objects::isNull);

    // add , between elements
    string = String.join(",", elements);

    // surround it with brackets
    string = String.format("(%s)", string);
    return string;
  }
}
