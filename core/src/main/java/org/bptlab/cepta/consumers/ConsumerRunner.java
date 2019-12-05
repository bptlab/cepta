package org.bptlab.cepta.consumers;

import org.bptlab.cepta.producers.KafkaServiceRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "Train Data Consumer Runner",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Runs the consumer to capture the train events coming from the Kafka queue.")
public class ConsumerRunner extends KafkaServiceRunner {
  @Option(
      names = {"-t", "--timeout"},
      description = "Specifies the timeout after one event event should be replayed after the other.")
  private long timeout = 2000;

  @Override
  public Integer call() throws Exception {
    TrainDataRunningConsumer consumer = new TrainDataRunningConsumer(getDefaultProperties());
    consumer.setTopic(kafkaTopic);
    consumer.setTimeout(timeout);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              public void run() {
                consumer.shutdown();
              }
            });
    consumer.start();
    return 0;
  }
}
