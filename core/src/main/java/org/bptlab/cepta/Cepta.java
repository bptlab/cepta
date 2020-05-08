package org.bptlab.cepta;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.bptlab.cepta.config.KafkaConfig;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.util.HashMap;

public class Cepta {

  protected static final Logger logger = LoggerFactory.getLogger(Cepta.class.getName());
  protected HashMap<Topic, FlinkKafkaConsumer011<Event>> sources;
  protected StreamExecutionEnvironment env;
  protected KafkaConfig kafkaConfig;
  protected PostgresConfig postgresConfig;

  public Cepta(StreamExecutionEnvironment env, HashMap<Topic, FlinkKafkaConsumer011<Event>> sources, KafkaConfig kafkaConfig, PostgresConfig postgresConfig) {
    this.env = env;
    this.sources = sources;
    this.kafkaConfig = kafkaConfig;
    this.postgresConfig = postgresConfig;
  }

  public Cepta(Builder<?> builder) {
    this(builder.env, builder.sources, builder.kafkaConfig, builder.postgresConfig);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder<T extends Builder<T>> {
    private HashMap<Topic, FlinkKafkaConsumer011<Event>> sources = new HashMap<>();
    private StreamExecutionEnvironment env = null;
    private KafkaConfig kafkaConfig;
    private PostgresConfig postgresConfig;

    public Builder() {}

    public T setSources(HashMap<Topic, FlinkKafkaConsumer011<Event>> sources) {
      this.sources = sources;
      return (T) this;
    }

    public T setEnv(StreamExecutionEnvironment env) {
      this.env = env;
      return (T) this;
    }

    public T setKafkaConfig(KafkaConfig config) {
      this.kafkaConfig = config;
      return (T)this;
    }

    public T setPostgresConfig(PostgresConfig config) {
      this.postgresConfig = config;
      return (T) this;
    }

    public Cepta build() { return new Cepta(this); }
  }

  public void start() throws Exception {
    // Override this method
    throw new NotImplementedException();
  }
}
