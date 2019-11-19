package org.bptlab.cepta;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ExampleTests {
  @Test(groups = {"active"})
  public void printSomethingUnnecessary() {
    System.out.println("Test method one");
  }

  @Test(groups = {"active"})
  public void printSomethingUnnecessary1() {
    Assert.assertEquals(1, 1);
  }

  @Test(groups = {"active"})
  public void printSomethingUnnecessary2() {
    Assert.assertEquals(1, 1);
  }

  @Test(groups = {"active"})
  public void printSomethingUnnecessary3() {
    Assert.assertEquals(1, 1);
  }

  // Failing test, is representable as a bad example and TravisCI will fail the build

  @Test(groups = {"broken"})
  public void testMethodTwo() {
    Assert.assertEquals(1, 2);
  }

  @Test(groups = {"broken"})
  public void printSomethingUnnecessaryToo() {
    System.out.println("Test method two");
  }
}
