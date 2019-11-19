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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DetectAgentTests {
  StreamExecutionEnvironment env;

  @BeforeTest(groups = "setUp")
  public void testSetUp() {
    env = StreamExecutionEnvironment.createLocalEnvironment();
    System.out.println("Env created");
  }

  @Test(groups = {"include-test-wind"})
  public void testStormFilter() throws IOException {
    ArrayList<Integer> testOutputList = new ArrayList<>();
    DataStream<Integer> windspeed = env.fromElements(1, 2, 3, 4, 5, 4, 3, 2, 1);
    Iterator<Integer> testOutputIterator =
        DataStreamUtils.collect(StreamingJob.stormFilter(windspeed));
    while (testOutputIterator.hasNext()) {
      testOutputList.add(testOutputIterator.next());
    }
    Assert.assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(5, 4, 4), testOutputList));
  }
}
