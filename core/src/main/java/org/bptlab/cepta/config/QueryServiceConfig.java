package org.bptlab.cepta.config;

import picocli.CommandLine.Option;
import java.io.Serializable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class QueryServiceConfig implements Serializable, Cloneable {

  @Option(
      names = {"-query-port", "--query-service-port"},
      description = "Specifies the Kafka Broker (ex. localhost:29092).")
  static int queryPort = 0;

  @Option(
      names = {"-query-host", "--query-service-host"},
      description = "Specifies the Kafka client.")
  static String queryHost = "localhost";

  int port;
  String host;

  public QueryServiceConfig() {}

  public QueryServiceConfig(String queryHost, int queryPort) {
    this.port = queryPort;
    this.host = queryHost;
  }

  public QueryServiceConfig build() {
    return new QueryServiceConfig(this.host, this.port);
  }

  public QueryServiceConfig withHost(String host) {
    QueryServiceConfig.queryHost = host;
    return this;
  }

  public QueryServiceConfig withPort(int port) {
    QueryServiceConfig.queryPort = port;
    return this;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}
