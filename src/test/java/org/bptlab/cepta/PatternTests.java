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

public class PatternTests {
  StreamExecutionEnvironment env;

  @BeforeTest(groups = "setUp")
  public void testSetUp() {
    env = StreamExecutionEnvironment.createLocalEnvironment();
    // set Parallelism to 1 so make sure the test data is transfered in the right order
    env.setParallelism(1);
    System.out.println("Env created");
  }

  @Test(groups = {"include-test-one"})
  public void testRisingSeaLevels() throws IOException {
    DataStream<Integer> seaLevels = env.fromElements(1, 2, 1, 4, 2, 3, 5, 2, 1);
    ArrayList<String> testOutputList = new ArrayList<>();
    Iterator<String> testOutputIterator =
        DataStreamUtils.collect(StreamingJob.seaLevelDetector(seaLevels));
    while (testOutputIterator.hasNext()) {
      testOutputList.add(testOutputIterator.next());
    }
    Assert.assertTrue(
        CollectionUtils.isEqualCollection(Arrays.asList("Gefahr, Gefahr"), testOutputList));
  }

  @Test(groups = {"test-one-exclude"})
  public void printSomethingUnnecessaryToo() {
    System.out.println("Test method two");
  }
}
