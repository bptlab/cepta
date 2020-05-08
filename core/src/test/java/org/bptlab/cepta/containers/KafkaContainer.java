package org.bptlab.cepta.containers;

import org.testcontainers.containers.*;
import org.bptlab.cepta.config.KafkaConfig;

public class KafkaContainer extends org.testcontainers.containers.KafkaContainer {

  public KafkaContainer() {
    super();
    this.withNetworkMode("host").withCreateContainerCmdModifier(cmd -> cmd.withMemory((long)50 * 1024 * 1024)); // 50MB
  }

  public KafkaConfig getConfig() {
    KafkaConfig config = new KafkaConfig()
        // .withBroker(this.getBootstrapServers())
        .withClientId("TestKafkaClient");
    // props.setProperty("bootstrap.servers", this.getBootstrapServers());
    // props.setProperty("group.id", "PersonsConsumer");
    // props.setProperty("client.id", "PersonsConsumer");
    // props.setProperty("transactional.id", "my-transaction");
    // props.setProperty("isolation.level", "read_committed");
    return config;
  }

}