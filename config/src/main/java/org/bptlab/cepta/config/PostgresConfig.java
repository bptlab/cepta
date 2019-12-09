package org.bptlab.cepta.config;

import java.io.Serializable;
import org.bptlab.cepta.config.constants.DatabaseConstants;
import picocli.CommandLine.Option;

public class PostgresConfig implements Serializable {

  @Option(
      names = {"--db-host"},
      description = "Specifies the Database Host (ex. postgres).")
  static String host = DatabaseConstants.HOST;

  @Option(
      names = {"--db-port"},
      description = "Specifies the port where the Database is running.")
  static int port = DatabaseConstants.PORT;

  @Option(
      names = {"-db", "--database"},
      description = "Specifies the name of the Database.")
  static String name = DatabaseConstants.DATABASE_NAME;

  @Option(
      names = {"-u", "--user"},
      description = "Specifies the user of the Database.")
  static String user = DatabaseConstants.USER;

  @Option(
      names = {"-pw", "--password"},
      description = "Specifies the password of the Database.")
  static String password = DatabaseConstants.PASSWORD;

  @Option(
      names = {"-c", "--connector"},
      description = "Specifies the connector to the Database (ex. jdbc).")
  static String connector = DatabaseConstants.CONNECTOR;

  @Option(
      names = {"-proto", "--protocol"},
      description = "Specifies the protocol of the Database (ex. postgresql).")
  static String protocol = DatabaseConstants.PROTOCOL;

  public PostgresConfig() {
  }

  public String getUrl() {
    return String.format("%s:%s://%s:%d/%s", connector, protocol, host, port, name);
  }

  public PostgresConfig withConnector(String connector) {
    PostgresConfig.connector = connector;
    return this;
  }

  public PostgresConfig withProtocol(String protocol) {
    PostgresConfig.protocol = protocol;
    return this;
  }

  public PostgresConfig withHost(String host) {
    PostgresConfig.host = host;
    return this;
  }

  public PostgresConfig withPort(int port) {
    PostgresConfig.port = port;
    return this;
  }

  public PostgresConfig withName(String name) {
    PostgresConfig.name = name;
    return this;
  }

  public PostgresConfig withUser(String user) {
    PostgresConfig.user = user;
    return this;
  }

  public PostgresConfig withPassword(String password) {
    PostgresConfig.password = password;
    return this;
  }

  public String getConnector() {
    return connector;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getName() {
    return name;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
