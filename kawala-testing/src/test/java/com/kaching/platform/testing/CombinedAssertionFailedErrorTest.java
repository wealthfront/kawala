/**
 * Copyright 2010 Wealthfront Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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
