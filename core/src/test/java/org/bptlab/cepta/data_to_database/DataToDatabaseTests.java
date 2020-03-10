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
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
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

    public void initDatabaseStuff(PostgreSQLContainer container) {
        // CREATE TABLE
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
           sql = createPlannedDatabaseQuery();
           stmt.executeUpdate(sql);
     
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
public int checkDatabaseInput(PostgreSQLContainer container) {
    // CREATE TABLE
    String db_url = container.getJdbcUrl();
    String user = container.getUsername();
    String password = container.getPassword();
    
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    int count = 0;
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
       sql = createSelectQuery();
       rs = stmt.executeQuery(sql);
       System.out.println(rs);
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
       }// do nothing
       try{
          if(conn!=null)
             conn.close();
       }catch(SQLException se){
          se.printStackTrace();
       }//end finally try
    }//end try
    System.out.println("Goodbye!");
    System.out.println(count);
    return count;
}

  @Test
  public void testIdMatch() throws IOException {
    try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
      postgres.start();
      initDatabaseStuff(postgres);
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());
      
      DataStream<PlannedTrainData> inputStream = PlannedTrainDataProvider.plannedTrainDatas();

      DataStream<PlannedTrainData> PlannedTrainData = inputStream.map(new DataToDatabase<PlannedTrainData>("public.planned", postgresConfig));
      
      
      Assert.assertTrue(checkDatabaseInput(postgres) == 2);
      System.out.println(PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent());
    
    }
     // Assert.assertTrue(true);
}
      

  private PostgreSQLContainer newPostgreSQLContainer(){
    return new PostgreSQLContainer<>().withDatabaseName("postgres").withUsername("postgres").withPassword("");
  }

  private String insertTrainWithTrainIdLocationIdQuery(long trainId, long locationId){
    return String.format(
      "INSERT INTO public.planned(" +
        "id, " +
        "train_id , " +
        "location_id, " +
        "planned_time," +
        "status, " +
        "first_train_number, " +
        "train_number_reference, " +
        "planned_departure_reference, " +
        "planned_arrival_reference, " +
        "train_operator_id, " +
        "transfer_location_id, " +
        "reporting_im_id, " +
        "next_im_id, " +
        "message_status, " +
        "message_creation, " +
        "original_train_number)" +
      "VALUES (1, %d, %d, current_timestamp, 5, 6, 7, current_timestamp, current_timestamp, 10, 11, 12, 13, 14, current_timestamp, 16)", trainId, locationId);
  }

  private String createPlannedDatabaseQuery(){
    return "CREATE TABLE public.planned ( " +
        "id integer, " +
        "train_id integer, " +
        "location_id integer, " +
        "planned_time timestamp, " +
        "status integer, " +
        "first_train_number integer, " +
        "train_number_reference integer, " +
        "planned_departure_reference timestamp, " +
        "planned_arrival_reference timestamp, " +
        "train_operator_id integer, " +
        "transfer_location_id integer, " +
        "reporting_im_id integer, " +
        "next_im_id integer, " +
        "message_status integer, " +
        "message_creation timestamp, " +
        "original_train_number integer)";
  }
  private String createSelectQuery(){
      return "Select * from public.planned;";
  }
}
