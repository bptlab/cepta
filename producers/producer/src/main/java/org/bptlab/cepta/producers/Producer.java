package org.bptlab.cepta.producers;

import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Producer<K, V> {
  private Logger logger = LoggerFactory.getLogger(Producer.class.getName());
  public String topic;
  public Boolean running = false;
  public Optional<Integer> limit = Optional.empty();
  public KafkaProducer<K, V> producer;
  ProducerBackgroundRunner runner;
  Thread runnerThread;

  public Producer(Properties props) {
    this.producer = new KafkaProducer<>(props);
  }

  private class ProducerBackgroundRunner implements Runnable {
    public void run() {
      try {
        produce();
      } catch (Exception exception) {
        logger.error("");
      }
    }
  }

  public abstract void produce() throws Exception;

  public void start() throws Exception {
    runner = new ProducerBackgroundRunner();
    runnerThread = new Thread(runner);
    runnerThread.start();
  };

  public void stop() {
    if (runnerThread != null) {
      runnerThread.interrupt();
    }
    this.running = false;
  };

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getTopic() {
    return this.topic;
  }

  public Boolean isRunning() {
    return this.running;
  };

  public void setLimit(int limit) {
    this.limit = Optional.of(limit);
  }

  public Optional<Integer> getLimit() {
    return this.limit;
  }
}
