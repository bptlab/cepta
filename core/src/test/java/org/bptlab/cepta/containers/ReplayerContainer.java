package org.bptlab.cepta.containers;

import org.bptlab.cepta.containers.LocalContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ReplayerContainer extends LocalContainer {
    public ReplayerContainer() {
        super("//auxiliary/producers/replayer");
        this.waitingFor(
            Wait.forLogMessage(".*Replayer ready.*\\n", 1)
        );
    }

    public static ReplayerContainer forKafka(KafkaContainer kafka) {
        ReplayerContainer replayer = new ReplayerContainer();
        replayer.withNetworkMode("host");
        replayer.withEnv("REPLAY_MODE", "constant");
        replayer.withEnv("PAUSE", "500"); // 500 milliseconds
        replayer.withEnv("PORT", "8080"); // 10 milliseconds
        // Not needed in host nework mode
        // replayer.withEnv("KAFKA_BROKERS", kafka.getBootstrapServers());
        return replayer;
    }
}

// TODO: Add more microservice containers