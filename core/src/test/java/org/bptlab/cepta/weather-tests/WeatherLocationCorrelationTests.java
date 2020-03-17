package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.bptlab.cepta.models.events.weather.WeatherDataProtos.WeatherData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.testcontainers.containers.PostgreSQLContainer;
import java.sql.*;
import java.lang.*;

public class WeatherLocationCorrelationTests {
  private PostgresConfig postgresConfig = new PostgresConfig().withHost("localhost");

  @Test
  public void testDirectLocationMatch() throws IOException {
      System.out.println("Start testDirectLocationMatch");
    try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
      postgres.start();
      initDatabaseStuff(postgres);
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());
      
      DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherAtDirectLocationData();
      DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = AsyncDataStream
          .unorderedWait(weatherStream, new WeatherLocationCorrelationFunction(postgresConfig),
              100000, TimeUnit.MILLISECONDS, 1);

      correlatedWeatherStream.print();
      Iterator<Tuple2<WeatherData, Integer>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
      ArrayList<Integer> locationIds = new ArrayList<>();
      while(iterator.hasNext()){
        locationIds.add(iterator.next().f1);
      }
      Assert.assertTrue(locationIds.contains(4012656));
    }
  }

  @Test
  public void testInsideBoxMatch() throws IOException {
    System.out.println("Start testInsideBoxMatch");
    try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
      postgres.start();
      initDatabaseStuff(postgres);
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());

      DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherInsideBoxLocationData();
      DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = AsyncDataStream
          .unorderedWait(weatherStream, new WeatherLocationCorrelationFunction(postgresConfig),
              100000, TimeUnit.MILLISECONDS, 1);

      // correlatedWeatherStream.print();
      Iterator<Tuple2<WeatherData, Integer>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
      ArrayList<Integer> locationIds = new ArrayList<>();
      while(iterator.hasNext()){
        locationIds.add(iterator.next().f1);
      }
      Assert.assertTrue(locationIds.contains(4012656));
    }
  }

  @Test
  public void testOutsideBoxMatch() throws IOException {
    System.out.println("Start testOutsideBoxMatch");
    try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
      postgres.start();
      initDatabaseStuff(postgres);
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());

      DataStream<WeatherData> weatherStream = WeatherDataProvider.weatherOutsideBoxLocationData();
      DataStream<Tuple2<WeatherData, Integer>> correlatedWeatherStream = AsyncDataStream
          .unorderedWait(weatherStream, new WeatherLocationCorrelationFunction(postgresConfig),
              100000, TimeUnit.MILLISECONDS, 1);

      // correlatedWeatherStream.print();
      Iterator<Tuple2<WeatherData, Integer>> iterator = DataStreamUtils.collect(correlatedWeatherStream);
      while(iterator.hasNext()){
        Assert.assertNotEquals(4012656, (long) iterator.next().f1);
      }
    }    
  }

  public void initDatabaseStuff(PostgreSQLContainer container) {

    System.out.println("Start initDatabaseStuff");
    // JDBC driver name and database URL
    String db_url = container.getJdbcUrl();
    String user = container.getUsername();
    String password = container.getPassword();

    Connection conn = null;
    Statement stmt = null;
    try{
      // Register JDBC driver
      Class.forName("org.postgresql.Driver");

      // Open a connection
      System.out.println("Connecting to a database...");
      conn = DriverManager.getConnection(db_url, user, password);
      System.out.println("Connected database successfully...");
      
      stmt = conn.createStatement();
      String sql;
      // Create table for planned data
      System.out.println("Create table...");
      sql = createLocationDatabaseQuery();
      stmt.executeUpdate(sql);
      System.out.println("Created table!");

      // Execute insert queries
      /*System.out.println("Inserting records into the table...");
      sql = insertLocationWithIdLatLonCodeQuery(4012656, 3.0067, 49.577, "xxx");
      stmt.executeUpdate(sql);
      System.out.println("Inserted records into the table...");*/

      System.out.println("Inserting records into the table...");
      sql = insertDumpQuery();
      stmt.executeUpdate(sql);
      System.out.println("Inserted records into the table...");
      
    }catch(SQLException se){
      //Handle errors for JDBC
      se.printStackTrace();
    }catch(Exception e){
      //Handle errors for Class.forName
      e.printStackTrace();
    }finally{
      //finally block used to close resources
      try{
          if(stmt!=null)
            conn.close();
      }catch(SQLException se){
      }// do nothing
      try{
          if(conn!=null)
            conn.close();
      }catch(SQLException se){
          se.printStackTrace();
      }//end finally try
    }//end try
    System.out.println("Goodbye!");
  }

  private PostgreSQLContainer newPostgreSQLContainer(){
    return new PostgreSQLContainer<>().withDatabaseName("postgres").withUsername("postgres").withPassword("example");
  }

  private String insertLocationWithIdLatLonCodeQuery(long locationId, double lon, double lat, String code){
    return String.format(Locale.US,
      "INSERT INTO public.location(" +
        "id," +
        "lon, " +
        "lat, " +
        "name, " +
        "code, " +
        "country_code)" +
        "VALUES (%d, %f, %f, 'fancy name', '%s', 'country code')", locationId, lon, lat, code);
  }

    private String insertDumpQuery(){
    return "INSERT INTO public.location(" +
        "id," +
        "lon, " +
        "lat, " +
        "name, " +
        "code, " +
        "country_code)" +
        "VALUES (4012656, 3.0067, 49.577, 'fancy name', 'xxx', 'country code')";
  }

  private String createLocationDatabaseQuery(){
    return "CREATE TABLE public.location( " +
        "id integer, " +
        "lon float, " +
        "lat float, " +
        "name varchar(60), " +
        "code varchar(30), " +
        "country_code varchar(30))";
  }


}
