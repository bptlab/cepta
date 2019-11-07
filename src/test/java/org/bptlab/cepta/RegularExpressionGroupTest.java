package org.bptlab.cepta;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RegularExpressionGroupTest {
  @Test(groups = {"include-test-one"})
  public void testMethodOne() {
    System.out.println("Test method one");
  }

  /*
  / Failing test, is representable as a bad example and TravisCI will fail the build

  @Test(groups = {"include-test-two"})
  public void testMethodTwo() {
    Assert.assertEquals(1, 2);
  }
  */

  @Test(groups = {"test-one-exclude"})
  public void testMethodThree() {
    System.out.println("Test method three");
  }

  @Test(groups = {"test-two-exclude"})
  public void testMethodFour() {
    System.out.println("Test method Four");
  }
}
