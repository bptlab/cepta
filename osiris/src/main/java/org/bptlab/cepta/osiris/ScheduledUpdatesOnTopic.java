package org.bptlab.cepta.osiris;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledUpdatesOnTopic{

  @Autowired
  private SimpMessagingTemplate template;

  private DataFeedStub feed = new DataFeedStub();

  @Scheduled(fixedDelay=1000)
  public void publishUpdates() throws IOException{
    template.convertAndSend("/topic/updates", feed.nextFeed());
  }
}

