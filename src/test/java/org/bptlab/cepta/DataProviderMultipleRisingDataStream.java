package org.bptlab.cepta;


import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.testng.annotations.DataProvider;

public class DataProviderMultipleRisingDataStream {

  @DataProvider(name = "data-provider")
  public static Object[][] dataProviderMethod()
  {
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    // set Parallelism to 1 so make sure the test data is transfered in the right order
    env.setParallelism(1);
    DataStream<Integer> seaLevels1 = env.fromElements(1, 2, 3, 1, 2, 3);
    DataStream<Integer> seaLevels2 = env.fromElements( 1, 4, 6, 1, 2, 99);
    return new Object[][] { { seaLevels1 }, { seaLevels2 } };
  }

}
