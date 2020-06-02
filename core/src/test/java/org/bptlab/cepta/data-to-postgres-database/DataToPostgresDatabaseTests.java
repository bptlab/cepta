package org.bptlab.cepta;

import java.io.IOException;
import java.util.Iterator;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.containers.PostgresContainer;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.DataToPostgresDatabase;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.bptlab.cepta.containers.PostgresContainer;

import org.junit.Assert;
import org.junit.Test;

import java.sql.*;

public class DataToPostgresDatabaseTests {

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
    try(PostgreSQLContainer postgres = new PostgresContainer<>()) {
      postgres.start();
      String address = postgres.getContainerIpAddress();
      Integer port = postgres.getFirstMappedPort();
      PostgresConfig postgresConfig = new PostgresConfig().withHost(address).withPort(port).withPassword(postgres.getPassword()).withUser(postgres.getUsername());
      
      DataStream<PlannedTrainData> inputStream = PlannedTrainDataProvider.plannedTrainDatas();
      
      inputStream.map(new DataToPostgresDatabase<PlannedTrainData>("plannedTrainData", postgresConfig));
      
      // We need a Iterator because otherwise the events aren't reachable in the Stream
      // Iterator needs to be after every funtion (in this case .map), because ther iterator consumes the events
      Iterator<PlannedTrainData> iterator = DataStreamUtils.collect(inputStream);
      while(iterator.hasNext()){
         PlannedTrainData temp = iterator.next();
         }
      // We insert 2 row into our Database with DataToPostgresDatabase() therefore we need to have 2 rows in our table
       Assert.assertTrue(checkDatabaseInput(postgres) == 2);    
      }
  }

  private String createSelectQuery(){
      return "Select * from public.plannedTrainData;";
  }
}
