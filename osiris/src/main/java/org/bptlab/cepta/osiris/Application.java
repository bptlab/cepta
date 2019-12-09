package org.bptlab.cepta.osiris;

import org.bptlab.cepta.producers.replayer.Success;
import org.bptlab.cepta.schemas.grpc.ReplayerClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:application.properties")
public class Application {

	public static void main(String[] args) {

		/* Start the replayer
		ReplayerClient test = new ReplayerClient("localhost", 9005);
		try {
			Success success = test.start();
		} catch (InterruptedException ex) {}

		/* Connect to Postgres DB for access to public.planned
		try {
			DatabaseConfig database = new DatabaseConfig(12);
			database.showPlannedTrainData();
		} catch (Exception e) {};
		 */

		SpringApplication.run(Application.class, args);
	}

}