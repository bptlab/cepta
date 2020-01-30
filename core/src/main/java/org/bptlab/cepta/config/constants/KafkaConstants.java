package org.bptlab.cepta.config.constants;

public interface KafkaConstants {
  String KAFKA_BROKER = "localhost:29092";
  String CLIENT_ID = "client1";
  String TOPIC_NAME = "test";
  String GROUP_ID_CONFIG = "consumerGroup1";

  public interface Topics {
    String LIVE_TRAIN_DATA = "LIVE_TRAIN_DATA";
    String PLANNED_TRAIN_DATA = "PLANNED_TRAIN_DATA";
    String PREDICTED_TRAIN_DATA = "PREDICTED_TRAIN_DATA";
    String TRAIN_INFO_DATA = "TRAIN_INFO_DATA";
    String DELAY_NOTIFICATIONS = "DELAY_NOTIFICATIONS";
    String WEATHER_DATA = "WEATHER_DATA";
  }

}


