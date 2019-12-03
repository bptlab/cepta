package org.bptlab.cepta.config.constants;

public interface KafkaConstants {
  String KAFKA_BROKER = "localhost:29092";
  Integer MESSAGE_COUNT = 1000;
  String CLIENT_ID = "client1";
  String TOPIC = "trainDataRunning";
  String GROUP_ID_CONFIG = "trainData";
  Integer MAX_NO_MESSAGE_FOUND_COUNT = 100;
  String OFFSET_RESET_LATEST = "latest";
  String OFFSET_RESET_EARLIER = "earliest";
  Integer MAX_POLL_RECORDS = 1;
}
