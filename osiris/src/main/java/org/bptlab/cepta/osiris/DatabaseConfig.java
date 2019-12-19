package org.bptlab.cepta.osiris;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.bptlab.cepta.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.config.constants.DatabaseConstants;
import org.bptlab.cepta.utils.converters.PlannedTrainDataDatabaseConverter;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.slf4j.Logger;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
  protected static final Logger logger =
      LoggerFactory.getLogger(DatabaseConfig.class.getName());

  private String plannedTableName;
  private int id;
  public boolean connected = false;
  public Connection connection;

  public DatabaseConfig() {
    this.plannedTableName = "public.planned";
  }

  public DatabaseConfig(int id){
    this.plannedTableName = "public.planned";
    this.id = id;
  };

  public void connect(PostgresConfig config) {
    try {
      connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
      connected = true;
      logger.info("Successfully connected to database");
    } catch (SQLException e) {
      logger.error("SQLException: database connection could not be established.");
      logger.error(String.format("database connection parameters: %s", config.getUrl()));
      logger.error(e.getMessage());
    }
  }

  private String showPlannedTrainDataQuery() {
    List<String> parts = new ArrayList<String>();
    parts.add(String.format("SELECT * FROM %s", this.plannedTableName));
    parts.add(String.format("WHERE id = %d", this.id));

    return String.join(" ", parts);
  }

  public void showPlannedTrainData() throws Exception {
    try {
      connect(new PostgresConfig()
          .withConnector(DatabaseConstants.CONNECTOR)
          .withProtocol(DatabaseConstants.PROTOCOL)
          .withHost("localhost")
          .withPort(DatabaseConstants.PORT)
          .withName(DatabaseConstants.DATABASE_NAME)
          .withUser(DatabaseConstants.USER)
          .withPassword(DatabaseConstants.PASSWORD));

      String query = showPlannedTrainDataQuery();
      Statement stmt = connection.createStatement();
      ResultSet result = stmt.executeQuery(query);

      while (result.next()){
        PlannedTrainData event = new PlannedTrainDataDatabaseConverter().fromResult(result);
        // do something with an API calls
      }

     } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}