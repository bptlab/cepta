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
import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass.TrainDelayNotification;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.DelayShiftFunction;
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

public class DelayShiftFunctionTests {

   public void initDatabase(PostgreSQLContainer container) {
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
         sql = createPlannedDatabaseQuery();
         stmt.executeUpdate(sql);

         // Execute insert queries
         System.out.println("Inserting records into the table...");
         sql = insertTrainWithSectionIdStationIdQuery(42382923, 1);
         stmt.executeUpdate(sql);
         sql = insertTrainWithSectionIdStationIdQuery(42382923, 2);
         stmt.executeUpdate(sql);
         sql = insertTrainWithSectionIdStationIdQuery(42093766, 3);
         stmt.executeUpdate(sql);
         sql = insertTrainWithSectionIdStationIdQuery(333, 666);
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

   @Test
   public void testRightAmount() throws IOException {
      try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
         postgres.start();
         initDatabase(postgres);
         String address = postgres.getContainerIpAddress();
         Integer port = postgres.getFirstMappedPort();
         PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());
         
         DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.matchingLiveTrainDatas(); 
         DataStream<TrainDelayNotification> delayStream = AsyncDataStream
            .unorderedWait(liveStream, new DelayShiftFunction(postgresConfig),
               100000, TimeUnit.MILLISECONDS, 1);

         Iterator<TrainDelayNotification> iterator = DataStreamUtils.collect(delayStream);
         ArrayList<TrainDelayNotification> delayEvents = new ArrayList<>();
         while(iterator.hasNext()){
            TrainDelayNotification delay = iterator.next();
            delayEvents.add(delay);
         }
         Assert.assertEquals(3, delayEvents.size());
      }
   }

   @Test
   public void testDelayNotificationGeneration() throws IOException {
      try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
         postgres.start();
         initDatabase(postgres);
         String address = postgres.getContainerIpAddress();
         Integer port = postgres.getFirstMappedPort();
         PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());
         
         DataStream<LiveTrainData> liveStream = LiveTrainDataProvider.matchingLiveTrainDatas(); 
         DataStream<TrainDelayNotification> delayStream = AsyncDataStream
            .unorderedWait(liveStream, new DelayShiftFunction(postgresConfig),
               100000, TimeUnit.MILLISECONDS, 1);

         Iterator<TrainDelayNotification> iterator = DataStreamUtils.collect(delayStream);
         ArrayList<TrainDelayNotification> delayEvents = new ArrayList<>();
         while(iterator.hasNext()){
            TrainDelayNotification delay = iterator.next();
            delayEvents.add(delay);
         }

         ArrayList<TrainDelayNotification> expectedDelayNotifications = new ArrayList<TrainDelayNotification>();
         expectedDelayNotifications.add(TrainDelayNotification.newBuilder().setTrainId(42382923).setLocationId(1).setDelay(1).build());
         expectedDelayNotifications.add(TrainDelayNotification.newBuilder().setTrainId(42382923).setLocationId(2).setDelay(1).build());
         expectedDelayNotifications.add(TrainDelayNotification.newBuilder().setTrainId(42093766).setLocationId(3).setDelay(1).build());
         Assert.assertEquals(expectedDelayNotifications, delayEvents);
      }
   }
 

   private PostgreSQLContainer newPostgreSQLContainer(){
      return new PostgreSQLContainer<>().withDatabaseName("postgres").withUsername("postgres").withPassword("");
   }

   private String insertTrainWithSectionIdQuery(long trainId){
      return String.format(
      "INSERT INTO public.planned(" +
         "id, " +
         "train_section_id , " +
         "station_id , " +
         "planned_event_time , " +
         "status , " +
         "first_train_id , " +
         "train_id , " +
         "planned_departure_time_start_station , " +
         "planned_arrival_time_end_station , " +
         "ru_id , " +
         "end_station_id , " +
         "im_id , " +
         "following_im_id , " +
         "message_status , " +
         "ingestion_time , " +
         "original_train_id )" +
      "VALUES (1, %d, 3, '1970-01-19 09:10:21.737', 5, 6, 7, '1970-01-19 09:06:21.737', '1970-01-19 09:06:21.737', 10, 11, 12, 13, 14, '1970-01-19 09:06:21.737', 16)", trainId);
   }

   private String insertTrainWithSectionIdStationIdQuery(long trainId, int stationId){
      return String.format(
      "INSERT INTO public.planned(" +
         "id, " +
         "train_section_id , " +
         "station_id , " +
         "planned_event_time , " +
         "status , " +
         "first_train_id , " +
         "train_id , " +
         "planned_departure_time_start_station , " +
         "planned_arrival_time_end_station , " +
         "ru_id , " +
         "end_station_id , " +
         "im_id , " +
         "following_im_id , " +
         "message_status , " +
         "ingestion_time , " +
         "original_train_id )" +
      "VALUES (1, %d, %d, '1970-01-19 09:10:21.737', 5, 6, 7, '1970-01-19 09:06:21.737', '1970-01-19 09:06:21.737', 10, 11, 12, 13, 14, '1970-01-19 09:06:21.737', 16)", trainId, stationId);
   }

   private String createPlannedDatabaseQuery(){
      return "CREATE TABLE public.planned ( " +
         "id integer, " +
         "train_section_id integer, " +
         "station_id integer, " +
         "planned_event_time timestamp, " +
         "status integer, " +
         "first_train_id integer, " +
         "train_id integer, " +
         "planned_departure_time_start_station timestamp, " +
         "planned_arrival_time_end_station timestamp, " +
         "ru_id integer, " +
         "end_station_id integer, " +
         "im_id integer, " +
         "following_im_id integer, " +
         "message_status integer, " +
         "ingestion_time timestamp, " +
         "original_train_id integer)";
   }
}
