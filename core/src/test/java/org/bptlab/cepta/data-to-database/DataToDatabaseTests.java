package org.bptlab.cepta;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.DataToDatabase;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.testcontainers.containers.PostgreSQLContainer;

import com.google.protobuf.GeneratedMessage;

import sun.awt.image.SunWritableRaster.DataStealer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.*;

public class DataToDatabaseTests {

    public Connection createDatabaseConnection(PostgreSQLContainer container) {
      String db_url = container.getJdbcUrl();
      String user = container.getUsername();
      String password = container.getPassword();
      
      Connection conn = null;

      try{
         // Register JDBC driver
         Class.forName("org.postgresql.Driver");
   
         // Open a connection
         System.out.println("Connecting to a database...");
         conn = DriverManager.getConnection(db_url, user, password);
         System.out.println("Connected database successfully...");
            
      }catch(SQLException se){
         //Handle errors for JDBC
         se.printStackTrace();
      }catch(Exception e){
         //Handle errors for Class.forName
         e.printStackTrace();
      }
      return conn;
    }

public int checkDatabaseInput(PostgreSQLContainer container) {
    
    Connection conn = createDatabaseConnection(container);
    Statement stmt = null;
    ResultSet rs = null;
    int count = 0;
    try{
       
       stmt = conn.createStatement();
       String sql;
       // Execute Select query to check if table contains data
       sql = createSelectQuery();
       rs = stmt.executeQuery(sql);
       while(rs.next()){
         count++;
       }
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
          se.printStackTrace();
       }
       try{
          if(conn!=null)
             conn.close();
       }catch(SQLException se){
          se.printStackTrace();
       }//end finally try
    }//end try
    System.out.println("Goodbye!");
    return count;
}

  @Test
  public void testIdMatch() throws IOException {
    try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
      postgres.start();
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());
      
      DataStream<PlannedTrainData> inputStream = PlannedTrainDataProvider.plannedTrainDatas();
      
      inputStream.map(new DataToDatabase<PlannedTrainData>("plannedTrainData", postgresConfig));
      
      // We need a Iterator because otherwise the events aren't reachable in the Stream
      // Iterator needs to be after every funtion (in this case .map), because ther iterator consumes the events
      Iterator<PlannedTrainData> iterator = DataStreamUtils.collect(inputStream);
      while(iterator.hasNext()){
         PlannedTrainData temp = iterator.next();
         }
      // We insert 2 row into our Database with DataToDatabase() therefore we need to have 2 rows in our table
       Assert.assertTrue(checkDatabaseInput(postgres) == 2);    
      }
}
      

  private PostgreSQLContainer newPostgreSQLContainer(){
    return new PostgreSQLContainer<>().withDatabaseName("postgres").withUsername("postgres").withPassword("");
  }

  private String createSelectQuery(){
      return "Select * from public.plannedTrainData;";
  }
}
