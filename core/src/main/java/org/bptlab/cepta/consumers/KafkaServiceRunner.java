package org.bptlab.cepta.consumers;

import java.util.concurrent.Callable;
import org.bptlab.cepta.config.KafkaConfig;
import picocli.CommandLine.Mixin;

public abstract class KafkaServiceRunner implements Callable<Integer> {

  @Mixin
  protected KafkaConfig kafkaConfig = new KafkaConfig();

}
