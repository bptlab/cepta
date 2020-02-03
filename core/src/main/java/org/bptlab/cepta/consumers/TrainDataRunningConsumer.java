package org.bptlab.cepta.consumers;

import java.sql.Connection;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public class TrainDataRunningConsumer extends Consumer {

  private boolean connected = false;
  private Connection connection;

  TrainDataRunningConsumer(Properties props) {
    super(props);
    subscribe(Collections.singletonList(topic));
  }

  public void shutdown() {
    close();
    // logger.info("Consumer shutting down");
  }

  public void start() {
    this.running = true;

    while (true) {
      final ConsumerRecords<Long, LiveTrainData> consumerRecords = poll(timeout);
      if (consumerRecords.count() < 1) {
        // logger.warn(String.format("Did not receive any records in %d milliseconds", timeout));
        continue;
      }
      consumerRecords.forEach(
          record -> {
            /* logger.debug(
            String.format(
                "Consumer Record:(%d, %s, %d, %d)\n",
                record.key(), record.value(), record.partition(), record.offset())); */
          });
      commitAsync();
    }
  }

  public void stop() {
    this.running = false;
    // logger.info("Consumer stopping");
  }
}
