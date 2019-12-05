package org.bptlab.cepta.anubis;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.security.Principal;
import java.util.concurrent.TimeUnit;
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

class ReplayerClient {

	private final ManagedChannel channel;
	private final ReplayerBlockingStub blockingStub;
	private final ReplayerStub asyncStub;

	public ReplayerClient(String host, int port) {
		this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
	}

	private ReplayerClient(ManagedChannelBuilder<?> channelBuilder) {
		channel = channelBuilder.build();
		blockingStub = ReplayerGrpc.newBlockingStub(channel);
		asyncStub = ReplayerGrpc.newStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public Success start() throws InterruptedException {
		return blockingStub.start(Empty.newBuilder().build());
	}

}

@Controller
public class WebsocketController {

	/*
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {
		Thread.sleep(500); // simulated delay
		return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
	}*/

	@RequestMapping(value = "/test", method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String test() throws Exception {
		ReplayerClient test = new ReplayerClient("localhost", 9000);
		Success success = test.start();
		System.out.println(success);
		System.out.println("FUCK");
		return "{'done': 'done'}";
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

}