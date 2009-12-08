/**
 * Copyright 2009 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.testing;

import static com.google.common.collect.Lists.newArrayList;
import static java.math.BigDecimal.valueOf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.ComparisonFailure;

/**
 * Extension of JUnit assertions.
 */
public class Assert extends org.junit.Assert {

  public static void assertBigDecimalEquals(double d1, BigDecimal d2) {
    assertBigDecimalEquals(null, d1, d2);
  }

  public static void assertBigDecimalEquals(String message, double d1, BigDecimal d2) {
    assertBigDecimalEquals(message, valueOf(d1), d2);
  }

  public static void assertBigDecimalEquals(String message, double d1, BigDecimal d2, int scale) {
    assertBigDecimalEquals(message, valueOf(d1), d2, scale);
  }

  public static void assertBigDecimalEquals(String message, BigDecimal d1, BigDecimal d2, int scale) {
    assertBigDecimalEquals(
        message,
        d1.setScale(scale, RoundingMode.HALF_UP),
        d2.setScale(scale, RoundingMode.HALF_UP));
  }

  public static void assertBigDecimalEquals(BigDecimal d1, BigDecimal d2) {
    assertBigDecimalEquals(null, d1, d2);
  }

  public static void assertBigDecimalEquals(BigDecimal d1, BigDecimal d2, int scale) {
    assertBigDecimalEquals(
        null,
        d1.setScale(scale, RoundingMode.HALF_UP),
        d2.setScale(scale, RoundingMode.HALF_UP));
  }

  public static void assertBigDecimalEquals(
      String message, BigDecimal d1, BigDecimal d2) {
    if (d1 == null && d2 == null) {
      return;
    }

    if (d1 == null || d2 == null || d1.compareTo(d2) != 0) {
      throw new ComparisonFailure(
          (message == null) ? "" : message,
          (d1 == null) ? "null" : d1.toPlainString(),
          (d2 == null) ? "null" : d2.toPlainString());
    }
  }

  public static void assertBigDecimalEquals(
      List<BigDecimal> expected, List<BigDecimal> actual) {
    assertBigDecimalEquals(null, expected, actual);
  }

  public static void assertBigDecimalEquals(String message,
      List<BigDecimal> expected, List<BigDecimal> actual) {
    if (expected == null || actual == null) {
      assertEquals(message, expected, actual);
    } else {
      Iterator<BigDecimal> eachExpected = expected.iterator();
      Iterator<BigDecimal> eachActual = actual.iterator();
      while (eachExpected.hasNext() && eachActual.hasNext()) {
        try {
          assertBigDecimalEquals(message, eachExpected.next(), eachActual.next());
        } catch(ComparisonFailure e) {
          assertEquals(message, expected, actual);
        }
      }
      if (eachExpected.hasNext() != eachActual.hasNext()) {
        assertEquals(message, expected, actual);
      }
    }
  }

  public static <K> void assertBigDecimalEquals(
      Map<K, BigDecimal> expected, Map<K, BigDecimal> actual) {
    assertTrue(expected.size() == actual.size());
    for (K key : expected.keySet()) {
      assertTrue(actual.containsKey(key));
      assertBigDecimalEquals(expected.get(key), actual.get(key), 5);
    }
  }

  public static void assertBigDecimalEquals(int d1, BigDecimal d2) {
    assertBigDecimalEquals(null, new BigDecimal(Integer.toString(d1)), d2);
  }

  public static void assertBigDecimalEquals(String message, int d1, BigDecimal d2) {
    assertBigDecimalEquals(message, new BigDecimal(Integer.toString(d1)), d2);
  }


  public static void assertBigDecimalEquals(float d1, BigDecimal d2) {
    assertBigDecimalEquals(null, new BigDecimal(Float.toString(d1)), d2);
  }

  public static void assertBigDecimalEquals(String message, float d1, BigDecimal d2) {
    assertBigDecimalEquals(message, new BigDecimal(Float.toString(d1)), d2);
  }

  public static void assertFloatEquals(float f1, float f2) {
    assertBigDecimalEquals(null, new BigDecimal(Float.toString(f1)),
        new BigDecimal(Float.toString(f2)));
  }

  public static void assertFloatEquals(float f1, float f2, int scale) {
    assertBigDecimalEquals(null, new BigDecimal(Float.toString(f1)),
        new BigDecimal(Float.toString(f2)), scale);
  }

  public static void assertFloatEquals(String message, float f1, float f2) {
    assertBigDecimalEquals(message, new BigDecimal(Float.toString(f1)),
        new BigDecimal(Float.toString(f2)));
  }

  public static <T> void assertContains(Collection<T>collection, T object) {
    if (!collection.contains(object)) {
      junit.framework.Assert.fail("Expected to find " + object);
    }
  }

  public static void assertArrayEquals(Object[] expected, Object[] actual) {
    assertEquals(newArrayList(expected), newArrayList(actual));
  }

  @SuppressWarnings("finally")
  public static void assertNotEquals(Object expected, Object actual) {
    boolean wasEqual = false;
    try {
      assertEquals(expected, actual);
      wasEqual = true;
    } catch (AssertionError e) {
    } finally {
      if (wasEqual) {
        fail();
      }
      return;
    }
  }

}