package org.bptlab.cepta.containers;

import org.bptlab.cepta.config.PostgresConfig;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer<SELF extends PostgreSQLContainer<SELF>> extends org.testcontainers.containers.PostgreSQLContainer<SELF> {

  public PostgresContainer() {
    super();
    this.withDatabaseName("postgres").withUsername("postgres").withPassword("")
            .withCreateContainerCmdModifier(cmd -> cmd.withMemory((long)50 * 1024 * 1024)); // 50MB
  }

  public PostgresConfig getConfig() {
    PostgresConfig config = new PostgresConfig()
            .withUser("postgres")
            .withName("postgres")
            .withPassword("");
    return config;
  }

}