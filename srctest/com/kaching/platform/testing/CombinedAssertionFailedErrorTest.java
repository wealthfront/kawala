package com.kaching.platform.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        "message:\n" +
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
        "message:\n" +
        "1) first\n" +
        "\n" +
        "2) second",
        error.toString());
    assertEquals(error.toString(), error.getMessage());
  }

  @Test
  public void toString4() {
    CombinedAssertionFailedError error = new CombinedAssertionFailedError();
    error.addError("first");
    assertEquals("1) first", error.toString());
    assertEquals(error.toString(), error.getMessage());
  }

  @Test
  public void hasErrors() {
    CombinedAssertionFailedError error = new CombinedAssertionFailedError("message");
    error.throwIfHasErrors();

    error.addError("first");
    try {
     error.throwIfHasErrors();
     fail();
    } catch (CombinedAssertionFailedError e) {
      assertTrue(error == e);
    }

    error.addError("second");
    try {
      error.throwIfHasErrors();
      fail();
     } catch (CombinedAssertionFailedError e) {
       assertTrue(error == e);
     }
  }

}
