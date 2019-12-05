package org.bptlab.cepta.producers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.bptlab.cepta.utils.types.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PostgresReplayer<K, V> extends Replayer<K, V> {
  private static final Logger logger =
      LoggerFactory.getLogger(PostgresReplayer.class.getName());

  public String tableName;
  public String sortColumn;
  public TimeRange timeRange = TimeRange.unconfined();

  public boolean connected = false;
  public Connection connection;

  public PostgresReplayer(
      Properties props,
      String connector,
      String protocol,
      String host,
      int port,
      String database,
      String user,
      String password) {
    super(props);
    this.connect(connector, protocol, host, port, database, user, password);
  }

  public PostgresReplayer(Properties props) {
    super(props);
  }

  public void connect(
      String connector,
      String protocol,
      String host,
      int port,
      String database,
      String user,
      String password) {
    String url = String.format("%s:%s://%s:%d/%s", connector, protocol, host, port, database);
    try {
      connection = DriverManager.getConnection(url, user, password);
      connected = true;
    } catch (SQLException e) {
      logger.error("SQLException: database connection could not be established.");
      logger.error(String.format("database connection parameters: %s", url));
      logger.error(e.getMessage());
    }
  }

  public String buildReplayQuery() {
    List<String> parts = new ArrayList<String>();
    // Datasource
    parts.add(String.format("SELECT * FROM %s", this.tableName));

    // Confine time range if either a start or end timestamp were specified
    parts.add(this.timeRange.getQuery(this.sortColumn));

    // Order by column
    parts.add(String.format("ORDER BY %s ASC", this.sortColumn));

    // Eventually limit the replay and specify an offset
    this.limit.map(limit -> parts.add(String.format("LIMIT %d", limit)));
    parts.add(String.format("OFFSET %d", this.offset));

    return String.join(" ", parts);
  }

  public void reset() throws Exception {
    stop();
    start();
  }

  public void seekTo(int offset) throws Exception {
    stop();
    setOffset(offset);
    start();
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
