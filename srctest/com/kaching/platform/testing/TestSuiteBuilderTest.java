/**
 * Copyright 2009 Wealthfront Inc. Licensed under the Apache License,
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

import org.junit.Test;

public class TestSuiteBuilderTest {

  @Test
  public void builderCaller1() {
    assertEquals(
        TestSuiteBuilderTest.class.getName(),
        TestSuiteBuilder.builderCaller());
  }

  @Test
  public void builderCaller2() {
    assertEquals(
        SomeWeirdoNameHere.class.getName(),
        new SomeWeirdoNameHere().builderCaller());
  }
  
  static class SomeWeirdoNameHere {
    String builderCaller() {
      return TestSuiteBuilder.builderCaller();
    }
  }
  
}
