package com.kaching.platform.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CombinedAssertionFailedErrorTest {

  @Test
  public void toString1() {
    CombinedAssertionFailedError error = new CombinedAssertionFailedError("message");
    assertEquals(
        "message",
        error.toString());
    assertEquals(error.toString(), error.getMessage());
  }

  @Test
  public void toString2() {
    CombinedAssertionFailedError error = new CombinedAssertionFailedError("message");
    error.addError("first");
    assertEquals(
        "message\n" +
        "1) first",
        error.toString());
    assertEquals(error.toString(), error.getMessage());
  }

  @Test
  public void toString3() {
    CombinedAssertionFailedError error = new CombinedAssertionFailedError("message");
    error.addError("first");
    error.addError("second");
    assertEquals(
        "message\n" +
        "1) first\n" +
        "2) second",
        error.toString());
    assertEquals(error.toString(), error.getMessage());
  }

  @Test
  public void hasErrors() {
    CombinedAssertionFailedError error = new CombinedAssertionFailedError("message");
    assertFalse(error.hasErrors());
    error.addError("first");
    assertTrue(error.hasErrors());
    error.addError("second");
    assertTrue(error.hasErrors());
  }

}
