package org.bptlab.cepta.producers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.bptlab.cepta.producers.exceptions.NoDatabaseConnectionException;
import org.bptlab.cepta.utils.types.TimeRange;
import org.bptlab.cepta.config.PostgresConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Option;

public abstract class PostgresReplayer<K, V> extends Replayer<K, V> {
  protected static final Logger logger =
      LoggerFactory.getLogger(PostgresReplayer.class.getName());

  public String tableName;
  public String sortColumn;
  public Optional<String> mustMatch = Optional.empty();
  public TimeRange timeRange = TimeRange.unconfined();

  public boolean connected = false;
  public Connection connection;

  public PostgresReplayer(
      Properties props,
      String topicName,
      String connector,
      String protocol,
      String host,
      int port,
      String database,
      String user,
      String password) {
    super(props, topicName);
    this.connect(connector, protocol, host, port, database, user, password);
  }

  public PostgresReplayer(
      Properties props,
      String topicName,
      PostgresConfig config) {
    super(props, topicName);
    this.connect(config);
  }

  public PostgresReplayer(Properties props, String topicName, String tableName, String sortColumn, TimeRange timeRange) {
    super(props, topicName);
    setTableName(tableName);
    setSortColumn(sortColumn);
    setTimeRange(timeRange);
  }

  public PostgresReplayer(Properties props, String topicName) {
    super(props, topicName);
  }

  public void connect(
      String connector,
      String protocol,
      String host,
      int port,
      String name,
      String user,
      String password) {
    connect(new PostgresConfig().withConnector(connector).withProtocol(protocol).withHost(host).withPort(port).withName(name).withUser(user).withPassword(password));
  }

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
  
  private String buildReplayQuery() {
    List<String> parts = new ArrayList<String>();
    // Datasource
    parts.add(String.format("SELECT * FROM %s", this.tableName));
    mustMatch.map(mustMatch -> parts.add(String.format("WHERE %s", mustMatch)));

    // Confine time range if either a start or end timestamp were specified
    parts.add(this.timeRange.getQuery(this.sortColumn));

    // Order by column
    parts.add(String.format("ORDER BY %s ASC", this.sortColumn));

    // Eventually limit the replay and specify an offset
    this.limit.map(limit -> parts.add(String.format("LIMIT %d", limit)));
    parts.add(String.format("OFFSET %d", this.offset));

    logger.info(String.join(" ", parts));
    return String.join(" ", parts);
  }

  @Override
  public void produce() throws Exception {
    logger.info("Starting to produce");
    if (!connected) {
      logger.error("Not connected to database");
      throw new NoDatabaseConnectionException("Not connected to database");
    }
    this.running = true;
    try {
      String query = buildReplayQuery();
      Statement nextEntryQuery = connection.createStatement();
      ResultSet result = nextEntryQuery.executeQuery(query);

      try {
        while (!result.next()) {
          result = nextEntryQuery.executeQuery(query);
          logger.info("Database query yielded no results, waiting.");
          Thread.sleep(10000);
        }
      } catch (InterruptedException exception) {
        logger.error("Interrupted while waiting for database results");
      }

      while (result.next()) {
        try {
          V event = convertToEvent(result);
          ProducerRecord<K, V> record =
              new ProducerRecord<K, V>(topic, event);
          RecordMetadata metadata = producer.send(record).get();
          logger.info(
              String.format("Sent record(key=%s value=%s) meta(partition=%d, offset=%d)\n",
                  record.key(), (V) record.value(), metadata.partition(), metadata.offset()));
          // producer.flush();
          Thread.sleep(this.frequency);
        } catch (Exception e) {
          logger.warn("Failed to process database entry. Will continue with the next entry.");
          e.printStackTrace();
        }
      }
      logger.info("There is no more live train data left in the database. Exiting.");
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(String.format("Error while executing database query. Closing the %s producer.", this.getClass().getName()));
    } finally {
      producer.close();
    }
  }

  public abstract V convertToEvent(ResultSet result) throws Exception;

  public void reset() throws Exception {
    stop();
    start();
  }

  public void seekTo(int offset) throws Exception {
    stop();
    setOffset(offset);
    start();
  }

  public void setMustMatch(Optional<String> mustMatch) {
    this.mustMatch = mustMatch;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getTableName() {
    return this.tableName;
  }

  public void setSortColumn(String column) {
    this.sortColumn = column;
  }

  public String getSortColumn() {
    return this.sortColumn;
  }

  public void setStartTime(Timestamp start) {
    this.timeRange.setFrom(start);
  }

  public void setEndTime(Timestamp end) {
    this.timeRange.setTo(end);
  }

  public void setTimeRange(TimeRange range) {
    this.timeRange = range;
  }

  public TimeRange getTimeRange() {
    return this.timeRange;
  }
}
