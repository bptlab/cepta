package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.junit.Assert;
import org.testng.annotations.Test;

public class PatternTests {
  @Test(
      groups = {"include-test-seaLevel"},
      dataProvider = "data-provider",
      dataProviderClass = DataProviderSingleRisingDataStream.class)
  public void testRisingSeaLevelsWithDataProvider(DataStream<Integer> seaLevels)
      throws IOException {
    ArrayList<String> testOutputList = new ArrayList<>();
    Iterator<String> testOutputIterator =
        DataStreamUtils.collect(StreamingJob.seaLevelDetector(seaLevels));
    while (testOutputIterator.hasNext()) {
      testOutputList.add(testOutputIterator.next());
    }
    Assert.assertTrue(
        CollectionUtils.isEqualCollection(Collections.singletonList("Gefahr!"), testOutputList));
  }

  @Test(
      groups = {"include-test-seaLevel"},
      dataProvider = "data-provider",
      dataProviderClass = DataProviderMultipleRisingDataStream.class)
  public void testTwoRisingSeaLevels(DataStream<Integer> seaLevels) throws IOException {
    ArrayList<String> testOutputList = new ArrayList<>();
    Iterator<String> testOutputIterator =
        DataStreamUtils.collect(StreamingJob.seaLevelDetector(seaLevels));
    while (testOutputIterator.hasNext()) {
      testOutputList.add(testOutputIterator.next());
    }
    Assert.assertTrue(
        CollectionUtils.isEqualCollection(Arrays.asList("Gefahr!", "Gefahr!"), testOutputList));
  }
}
