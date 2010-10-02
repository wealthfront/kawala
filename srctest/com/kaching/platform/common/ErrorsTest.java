/**
 * Copyright 2010 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.common;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.kaching.platform.testing.EquivalenceTester;

public class ErrorsTest {

  @Test
  public void toString1() {
    assertEquals("no errors", new Errors().toString());
  }

  @Test
  public void toString2() {
    assertEquals(
        "1) a",
        new Errors().addMessage("a").toString());
  }

  @Test
  public void toString3() {
    assertEquals(
        "1) a 1\n" +
        "\n" +
        "2) b 2",
        new Errors().addMessage("a %s", 1).addMessage("b %s", 2).toString());
  }

  @Test
  public void noErrorsShouldNotThrow() {
    new Errors().throwIfHasErrors();
  }

  @Test
  public void hasErrors() {
    assertFalse(new Errors().hasErrors());
    assertTrue(new Errors().addMessage("").hasErrors());
  }

  @Test
  public void equivalence() {
    EquivalenceTester.check(
        newArrayList(
            new Errors().addMessage("yo %s", 5),
            new Errors().addMessage("yo %s", 5)),
        newArrayList(
            new Errors().addMessage("yo %s", 3),
            new Errors().addMessage("yo %s", 3)));
  }

}
