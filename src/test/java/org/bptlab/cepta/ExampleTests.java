package org.bptlab.cepta;

import org.testng.annotations.Test;

public class ExampleTests {
  @Test(groups = {"include-test-one"})
  public void printSomethingUnnecessary() {
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
  public void printSomethingUnnecessaryToo() {
    System.out.println("Test method two");
  }
}
