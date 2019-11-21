package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DetectAgentTests {
  StreamExecutionEnvironment env;

  @BeforeClass(groups = "setUp-global")
  public void testSetUp() {
    env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);
    System.out.println("Env created");
  }

  // tests our custom filter agent, which filters all integers larger than 3
  @Test(groups = {"include-test-wind"})
  public void testStormFilter() throws IOException {
    // mock up Datasource - Stream of "speeds" = integers
    DataStream<Integer> windspeed = env.fromElements(1, 2, 3, 4, 5, 4, 3, 2, 1);
    System.out.println("print windspeed");
    windspeed.print();
    // initialize Outputlist
    ArrayList<Integer> testOutputList = new ArrayList<>();
    // call stormFilter with our Datasource and add results to Outputlist
    Iterator<Integer> testOutputIterator =
        DataStreamUtils.collect(StreamingJob.stormFilter(windspeed));
    while (testOutputIterator.hasNext()) {
      testOutputList.add(testOutputIterator.next());
    }
    // test if Output matches expected results
    Assert.assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(5, 4, 4), testOutputList));
  }
}
