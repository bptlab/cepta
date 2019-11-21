package org.bptlab.cepta;

import org.testng.annotations.Test;

public class ExampleTests {
  @Test(groups = {"active"})
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
  @Test(groups = {"broken"})
  public void printSomethingUnnecessaryToo() {
    System.out.println("Test method two");
  }
}
