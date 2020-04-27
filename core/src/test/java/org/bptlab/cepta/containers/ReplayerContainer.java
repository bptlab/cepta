package org.bptlab.cepta.containers;

import org.bptlab.cepta.containers.LocalContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ReplayerContainer extends LocalContainer {

    private String host;
    private int port;

    public ReplayerContainer() {
        this("localhost", 8080);
    }

    public ReplayerContainer(String host, int port) {
        super("//auxiliary/producers/replayer");
        this.host = host;
        this.port = port;
        this.withEnv("PORT", Integer.toString(port));
        this.withEnv("PAUSE", "500"); // 500 milliseconds
        this.withEnv("REPLAY_MODE", "constant");
        this.waitingFor(
            Wait.forLogMessage(".*Replayer ready.*\\n", 1)
        );
    }

    public static ReplayerContainer forKafka(KafkaContainer kafka) {
        ReplayerContainer replayer = new ReplayerContainer();
        replayer.withNetworkMode("host");
        // Not needed in host nework mode
        // replayer.withEnv("KAFKA_BROKERS", kafka.getBootstrapServers());
        return replayer;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}