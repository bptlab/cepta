package org.bptlab.cepta.consumers;

import java.sql.Connection;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;

public class TrainDataRunningConsumer extends Consumer {

  private boolean connected = false;
  private Connection connection;

  TrainDataRunningConsumer(Properties props) {
    super(props);
    subscribe(Collections.singletonList(topic));
  }

  public void shutdown() {
    close();
  }

  public void start() {
    this.running = true;

    while (true) {
      final ConsumerRecords<Long, LiveTrainData> consumerRecords = poll(timeout);
      if (consumerRecords.count() < 1) {
        continue;
      }
      consumerRecords.forEach(
          record -> {});
      commitAsync();
    }
  }

  public void stop() {
    this.running = false;
  }
}
