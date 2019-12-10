package org.bptlab.cepta.osiris;

import org.bptlab.cepta.producers.replayer.Success;
import org.bptlab.cepta.schemas.grpc.ReplayerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:application.properties")
public class Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {

		/* Start the replayer
		ReplayerClient test = new ReplayerClient("localhost", 9005);
		try {
			Success success = test.start();
		} catch (InterruptedException | io.grpc.StatusRuntimeException ex) {
			logger.error("Failed to start the gRPC replayer service");
		}

		/* Connect to Postgres DB for access to public.planned
		try {
			DatabaseConfig database = new DatabaseConfig(12);
			database.showPlannedTrainData();
		} catch (Exception e) {};
		 */

		SpringApplication.run(Application.class, args);
	}

}