package org.bptlab.cepta.osiris;

import java.io.IOException;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.config.constants.KafkaConstants;
import org.bptlab.cepta.config.constants.KafkaConstants.Topics;
import org.bptlab.cepta.serialization.AvroJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DelayEventConsumer {
  private static final Logger logger = LoggerFactory.getLogger(DelayEventConsumer.class);

  @Autowired
  private SimpMessagingTemplate template;

  @KafkaListener(topics = Topics.LIVE_TRAIN_DATA, groupId = KafkaConstants.GROUP_ID_CONFIG)
  public void consume(LiveTrainData event) throws IOException {
    logger.error(String.format("#### -> Consumed message -> %s", event.toString()));
    byte[] test = new AvroJsonSerializer<LiveTrainData>().serialize("/topic/updates", event);
    template.convertAndSend("/topic/updates", event.toString());
  }
}