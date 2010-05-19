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
package com.kaching.platform.common.values;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.kaching.platform.testing.EquivalenceTester;

public class OptionTest {

  @Test
  public void none() {
    for (Object o : Option.none()) {
      fail(o.toString() /* using o to avoid unused warning */);
    }
  }

  @Test
  public void some() {
    boolean reached = false;
    for (String string : Option.some("string")) {
      assertEquals("string", string);
      reached = true;
    }
    assertTrue(reached);
  }

  @Test(expected = IllegalArgumentException.class)
  public void noneGet() {
    Option.none().get();
  }

  @Test
  public void someGet() {
    assertEquals(600, Option.some(600).get());
  }

  @Test
  public void testOfToString() {
    assertEquals("Option.None", Option.none().toString());
    assertEquals("Option.Some(string)", Option.some("string").toString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void equivalence() {
    EquivalenceTester.check(
        newArrayList(
            Option.none(),
            Option.none(),
            Option.none()),
        newArrayList(
            Option.some("string"),
            Option.some("string"),
            Option.some("string")),
        newArrayList(
            Option.some(Integer.valueOf(400)),
            Option.some(Integer.valueOf(400)),
            Option.some(Integer.valueOf(400))));
  }

}
