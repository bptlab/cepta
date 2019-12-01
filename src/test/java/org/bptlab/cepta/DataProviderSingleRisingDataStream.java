package org.bptlab.cepta;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.testng.annotations.DataProvider;

public class DataProviderSingleRisingDataStream {

  // in this context a risingDataStream means three directly followed increasing events
  // single means that in every DataSteam provided there is just one of these sequences

  @DataProvider(name = "data-provider")
  public static Object[][] dataProviderMethod() {
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    // set Parallelism to 1 so make sure the test data is transfered in the right order
    env.setParallelism(1);
    DataStream<Integer> seaLevels1 = env.fromElements(1, 2, 1, 4, 2, 3, 5, 2, 1);
    DataStream<Integer> seaLevels2 = env.fromElements(1, 4, 6);
    DataStream<Integer> seaLevels3 = env.fromElements(1000, 4000, 6000);
    return new Object[][] {{seaLevels1}, {seaLevels2}, {seaLevels3}};
  }
}
