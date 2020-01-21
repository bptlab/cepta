package org.bptlab.cepta.consumers;

import java.util.Properties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public abstract class Consumer extends KafkaConsumer<Long, LiveTrainData> {
  protected String topic;
  protected Boolean running = false;
  private Integer limit = 10000;
  protected long timeout = 10000;

  public Consumer(Properties props) {
    super(props);
  }

  public abstract void shutdown();

  protected abstract void start() throws Exception;

  protected abstract void stop();

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public long getTimeout() {
    return timeout;
  }

  public String getTopic() {
    return this.topic;
  }

  public Boolean isRunning() {
    return running;
  };

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }
}
