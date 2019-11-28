package org.bptlab.cepta;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.testng.annotations.DataProvider;

public class DataProviderWindSpeedDataStream {
  @DataProvider(name = "data-provider")
  public static Object[][] dataProviderMethod() {
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    // set Parallelism to 1 so make sure the test data is transfered in the right order
    env.setParallelism(1);
    DataStream<Integer> windspeed1 = env.fromElements(5, 4, 4);
    DataStream<Integer> windspeed2 = env.fromElements(1, 2, 3, 4, 5, 4, 3, 2, 1);
    DataStream<Integer> windspeed3 = env.fromElements(1, 4, 4, 5, 1);
    return new Object[][] {{windspeed1}, {windspeed2}, {windspeed3}};
  }
}
