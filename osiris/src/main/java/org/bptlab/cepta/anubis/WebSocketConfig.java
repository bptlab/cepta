package org.bptlab.cepta.anubis;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@PropertySource(value = "classpath:application.properties")
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.setApplicationDestinationPrefixes("/app");

		// Use a broker to relay to rabbitMQ server instead of Simple Broker
		// config.enableSimpleBroker("/topic");
		config.enableStompBrokerRelay("/topic", "/queue")
				.setRelayHost("localhost")
				.setRelayPort(61613);
				// .setClientLogin("guest")
				// .setClientPasscode("guest");

	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
				.setAllowedOrigins("http://localhost:8081")
				.withSockJS();
	}

}