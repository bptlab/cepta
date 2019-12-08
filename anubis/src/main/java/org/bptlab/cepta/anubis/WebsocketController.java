package org.bptlab.cepta.anubis;

import com.google.gson.Gson;
import java.util.Map;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

  private Gson gson = new Gson();

  @MessageMapping("/hello")
  @SendTo("/topic/greetings")
  public Greeting greeting(HelloMessage message) throws Exception {
    Thread.sleep(500); // simulated delay
    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
  }

  @MessageMapping("/updates")
  @SendTo("/queue/updates")
  public String provideFeed(@Payload String updates){
    return gson
        .fromJson(updates, Map.class)
        .get("update").toString();
  }

	/*
	@MessageMapping("/updates")
	@SendTo("/queue/updates")
	public DataFeed provideFeed(UpdateMessage update) throws Exception {
		Thread.sleep(500); // wait fo next Update
		return new DataFeed(HtmlUtils.htmlEscape(update.getUpdate()));
	}

	@MessageMapping("/news")
	@SendTo("/topic/news")
	public String broadcastNews(@Payload String message) {
		return message;
	}

	@MessageMapping("/updates")
	@SendToUser("/queue/updates")
	public String provideNotifications(@Payload String message,
			Principal user) {
		return "Hello" + message;
	}
	 */
}