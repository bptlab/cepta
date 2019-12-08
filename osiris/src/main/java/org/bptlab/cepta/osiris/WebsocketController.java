package org.bptlab.cepta.osiris;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.TimeUnit;
import org.bptlab.cepta.osiris.Greeting;
import org.bptlab.cepta.osiris.HelloMessage;
import org.bptlab.cepta.producers.replayer.Empty;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerBlockingStub;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerStub;
import org.bptlab.cepta.producers.replayer.Success;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebsocketController {
	@MessageMapping("/updates")
	public Object provideNotifications() throws IOException {
		Object feed = new DataFeedStub().nextFeed();
		return feed;
	}
}