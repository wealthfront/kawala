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
package com.kaching.platform.converters;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.kaching.platform.testing.EquivalenceTester;

public class ErrorsTest {

  @Test
  public void noErrorsShouldNotThrow() {
    new Errors().throwIfHasErrors();
  }

  @Test
  public void errorScenario() {
    try {
      new Errors()
          .incorrectBoundForConverter(String.class, Converter.class, Integer.class)
          .throwIfHasErrors();
      fail();
    } catch (RuntimeException e) {
      assertEquals(
          "1) The converter interface com.kaching.platform.converters.Converter, " +
          "mentioned on class java.lang.String using @ConvertedBy, " +
          "does not produce instances of class java.lang.String. It produces " +
          "class java.lang.Integer.",
          e.getMessage());
    }
  }

  @Test
  public void equivalence() {
    EquivalenceTester.check(
        newArrayList(
            new Errors().incorrectBoundForConverter(String.class, Converter.class, Integer.class),
            new Errors().incorrectBoundForConverter(String.class, Converter.class, Integer.class)));
  }

}
