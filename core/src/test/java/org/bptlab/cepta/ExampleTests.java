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
}
