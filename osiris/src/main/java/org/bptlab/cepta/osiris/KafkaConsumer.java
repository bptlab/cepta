package org.bptlab.cepta.osiris;

import java.io.IOException;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/*
@Service
public class KafkaConsumer {

  @KafkaListener(topics = KafkaConstants.TOPIC, groupId = KafkaConstants.GROUP_ID_CONFIG)
  public void consume(String message) throws IOException {
    System.out.println(String.format("#### -> Consumed message -> %s", message));
  }
}
*/