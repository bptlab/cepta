package org.bptlab.cepta.anubis;

import java.io.IOException;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

  private final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

  @KafkaListener(topics = KafkaConstants.TOPIC, groupId = KafkaConstants.GROUP_ID_CONFIG)
  public void consume(String message) throws IOException {
    logger.info(String.format("#### -> Consumed message -> %s", message));
  }
}
