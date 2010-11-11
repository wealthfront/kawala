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

import static com.google.common.collect.Lists.newArrayList;
import static com.kaching.platform.testing.Assert.assertBigDecimalEquals;
import static com.kaching.platform.testing.Assert.assertFloatEquals;
import static com.kaching.platform.testing.Assert.assertNotEquals;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.List;

import org.junit.ComparisonFailure;
import org.junit.Test;

public class AssertTest {

  @Test
  public void assertBigDecimalEquals1() {
    assertBigDecimalEquals((BigDecimal) null, null);
    assertBigDecimalEquals(ONE, ONE);
    assertBigDecimalEquals(5, valueOf(5.0));
    assertBigDecimalEquals(new BigDecimal("0.0"), new BigDecimal("0.000"));
  }

  @Test
  public void assertBigDecimalEquals2() {
    try {
      assertBigDecimalEquals(
          new BigDecimal("1.0000000000000000000000000001"),
          new BigDecimal("1.0000000000000000000000000002"));
      fail();
    } catch (ComparisonFailure e) {
      assertEquals(
          "expected:<...00000000000000000000[1]> " +
          "but was:<...00000000000000000000[2]>",
          e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals3() {
    try {
      assertBigDecimalEquals(null, ONE);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<[null]> but was:<[1]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals4() {
    try {
      assertBigDecimalEquals(ONE, null);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<[1]> but was:<[null]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals5() {
    try {
      assertBigDecimalEquals("includes message", ONE, null);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("includes message expected:<[1]> but was:<[null]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals6() {
    try {
      assertBigDecimalEquals(
          "includes message",
          new BigDecimal("1.0000000000000000000000000001"),
          new BigDecimal("1.0000000000000000000000000002"));
      fail();
    } catch (ComparisonFailure e) {
      assertEquals(
          "includes message " +
          "expected:<...00000000000000000000[1]> " +
          "but was:<...00000000000000000000[2]>",
          e.getMessage());
    }
  }


  @Test
  public void assertBigDecimalEquals7() {
    assertBigDecimalEquals(
        "msg",
        new BigDecimal("1.00001"),
        new BigDecimal("1.00002"),
        4);

    assertBigDecimalEquals(
        "msg",
        1.00001,
        new BigDecimal("1.00002"),
        4);
  }

  @Test
  public void assertBigDecimalEquals8() {
    try {
      assertBigDecimalEquals(
          "includes message",
          new BigDecimal("1.0001"),
          new BigDecimal("1.0002"),
          4);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals(
          "includes message " +
          "expected:<1.000[1]> " +
          "but was:<1.000[2]>",
          e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals9() throws Exception {  // float test
    assertBigDecimalEquals(0.0f, ZERO);
    assertBigDecimalEquals(-0.001f, BigDecimal.valueOf(-0.001));
    assertBigDecimalEquals(0.001f, BigDecimal.valueOf(0.001));
    assertBigDecimalEquals(0.00100f, BigDecimal.valueOf(0.001));
    assertBigDecimalEquals(0.00100f, BigDecimal.valueOf(0.00100000));
  }

  @Test
  public void assertBigDecimalEquals10() {
    try {
      assertBigDecimalEquals(0.223f, null);  // float test with null
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<[0.223]> but was:<[null]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals11() {
    try {
      assertBigDecimalEquals(0.22300f, ONE);  // float test
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<[0.223]> but was:<[1]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals12() {
    try {  // float test
      assertBigDecimalEquals(0.001001f, BigDecimal.valueOf(0.001).setScale(6));
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<0.00100[1]> but was:<0.00100[0]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals13() {
    try {  // float test with message
      assertBigDecimalEquals("msg", 0.001001f, BigDecimal.valueOf(0.001).setScale(6));
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("msg expected:<0.00100[1]> but was:<0.00100[0]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals14() {
    assertBigDecimalEquals(0, ZERO);  // integer test
    assertBigDecimalEquals(-103, valueOf(-103));
    assertBigDecimalEquals(101, valueOf(101));
  }

  @Test
  public void assertBigDecimalEquals15() {
    try {
      assertBigDecimalEquals(31, null);  // integer test with null
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<[31]> but was:<[null]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals16() {
    try {
      assertBigDecimalEquals(13, ONE);  // integer test
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<1[3]> but was:<1[]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals17() {
    try {  // integer test with message
      assertBigDecimalEquals("includes message", 13, ONE);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("includes message expected:<1[3]> but was:<1[]>", e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals1_onLists() {
    assertBigDecimalEquals((List<BigDecimal>) null, null);
    assertBigDecimalEquals(
        newArrayList(new BigDecimal("0")),
        newArrayList(new BigDecimal("0.0")));
  }

  @Test
  public void assertBigDecimalEquals2_onLists() {
    try {
      assertBigDecimalEquals(
          newArrayList(new BigDecimal("1")),
          newArrayList(new BigDecimal("2"), valueOf(3)));
    } catch (AssertionError e) {
      assertEquals(
          "expected:<[1]> but was:<[2, 3]>",
          e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals3_onLists() {
    try {
      assertBigDecimalEquals(
          newArrayList(new BigDecimal("2"), valueOf(3)), null);
    } catch (AssertionError e) {
      assertEquals(
          "expected:<[2, 3]> but was:<null>",
          e.getMessage());
    }
  }

  @Test
  public void assertBigDecimalEquals4_onLists() {
    try {
      assertBigDecimalEquals(
          null, newArrayList(new BigDecimal("2"), valueOf(3)));
    } catch (AssertionError e) {
      assertEquals(
          "expected:<null> but was:<[2, 3]>",
          e.getMessage());
    }
  }

  @Test
  public void assertNotEquals1() throws Exception {
    assertNotEquals(1, 2);
    assertNotEquals("foo", "bar");
    assertNotEquals(1, null);
    assertNotEquals(null, 2);
  }

  @Test
  public void assertNotEquals2() throws Exception {
    try {
      assertNotEquals(1, 1);
      fail();
    } catch (AssertionError e) {
    }
    try {
      assertNotEquals("foo", "foo");
      fail();
    } catch (AssertionError e) {
    }
    try {
      assertNotEquals(null, null);
      fail();
    } catch (AssertionError e) {
    }
  }

  @Test
  public void assertFloatEquals1() throws Exception {
    assertFloatEquals(0.00100f, 0.001f);
    assertFloatEquals(0.001f, 0.001f);
    assertFloatEquals(0.00100f, 0.00100000f);
    assertFloatEquals(0, 0.0000f);
  }

  @Test
  public void assertFloatEquals2() {
    try {
      assertFloatEquals(1.2f, 1);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<1.[2]> but was:<1.[0]>", e.getMessage());
    }
  }

  @Test
  public void assertFloatEquals3() {
    try {
      assertFloatEquals(0, 0.1f);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<0.[0]> but was:<0.[1]>", e.getMessage());
    }
  }

  @Test
  public void assertFloatEquals4() {
    try {
      assertFloatEquals(1.22300f, 1, 3);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<1.[223]> but was:<1.[000]>", e.getMessage());
    }
  }

  @Test
  public void assertFloatEquals5() {
    try {
      assertFloatEquals(0.001001f, 0.001f, 6);
      fail();
    } catch (ComparisonFailure e) {
      assertEquals("expected:<0.00100[1]> but was:<0.00100[0]>", e.getMessage());
    }
  }

}
