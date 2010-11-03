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
package com.kaching.platform.common;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.common.base.Function;
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

  @Test
  public void noneGetOrNull() throws Exception {
    assertNull(Option.none().getOrNull());
  }

  @Test(expected = IllegalArgumentException.class)
  public void noneGetOrThrowNoParam() {
    Option.none().getOrThrow();
  }

  @Test
  public void noneGetOrThrowWithParam() {
    RuntimeException eThrown = new RuntimeException();
    try {
      Option.none().getOrThrow(eThrown);
      fail();
    } catch (RuntimeException eCaught) {
      assertTrue(eCaught == eThrown);
    }
  }

  @Test
  public void noneGetOrThrowWithMessage() {
    try {
      Option.none().getOrThrow("foo");
      fail();
    } catch (IllegalArgumentException eCaught) {
      assertEquals("foo", eCaught.getMessage());
    }
  }

  @Test
  public void noneTransform() throws Exception {
    Option.<Integer> none().transform(new Function<Integer, String>() {
      @Override
      public String apply(Integer from) {
        return String.valueOf(from);
      }
    });
  }

  @Test
  public void noneToStringOr() throws Exception {
    assertEquals("foo", Option.none().toStringOr("foo"));
  }

  @Test
  public void someGetOrNull() throws Exception {
    assertEquals(600, Option.some(600).getOrNull());
  }

  @Test
  public void someGetOrThrowNoParam() {
    assertEquals(600, Option.some(600).getOrThrow());
  }

  @Test
  public void someGetOrThrowWithParam() {
    assertEquals("", Option.some("").getOrThrow(new RuntimeException()));
  }

  @Test
  public void someGetOrThrowWithMessage() {
    assertEquals("", Option.some("").getOrThrow("foo"));
  }

  @Test
  public void someTransform() throws Exception {
    assertEquals(Option.some("1"), Option.some(1).transform(
        new Function<Integer, String>() {
          @Override
          public String apply(Integer from) {
            return String.valueOf(from);
          }
        }));
  }

  @Test
  public void someTransformReturnsNull() throws Exception {
    assertFalse(Option.some(1).transform(
        new Function<Integer, String>() {
          @Override
          public String apply(Integer from) {
            return null;
          }
        }).isDefined());
  }

  @Test
  public void someToStringOr() throws Exception {
    assertEquals("1", Option.some(1).toStringOr("foo"));
  }

  @Test
  public void getOrElseEager() {
    assertEquals(3, Option.some(3).getOrElse(2));
    assertEquals(2, Option.none().getOrElse(2));
  }

  @Test
  public void getOrElseLazy() {
    Thunk<Integer> defaultValue = new Thunk<Integer>() {
      @Override
      protected Integer compute() {
        return 2;
      }
    };

    assertEquals(3, Option.some(3).getOrElse(defaultValue));
    assertFalse(defaultValue.isEvaluated());

    /* Note: Parameterizing the none call is required otherwise Object is
     * inferred for T and getOrElse(Object) is called instead of
     * getOrElse(Thunk<Object>). In practice, the option one would rarely write
     * such an expression but rather key off a properly parameterized optional
     * value.
     */
    assertEquals(2, Option.<Integer> none().getOrElse(defaultValue));
    assertTrue(defaultValue.isEvaluated());
  }

  @Test
  public void isEmptyIsDefined() {
    // isEmpty
    assertFalse(Option.some("").isEmpty());
    assertTrue(Option.none().isEmpty());

    // isDefined
    assertTrue(Option.some("").isDefined());
    assertFalse(Option.none().isDefined());
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

  @Test
  @SuppressWarnings("unchecked")
  public void of() {
    EquivalenceTester.check(
        newArrayList(
            Option.none(),
            Option.of(null)),
        newArrayList(
            Option.some("foo"),
            Option.of("foo")));
  }
}
