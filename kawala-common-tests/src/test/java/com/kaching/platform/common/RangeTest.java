package com.kaching.platform.common;

import static com.google.common.collect.Lists.newArrayList;
import static com.kaching.platform.common.Range.range;
import static com.kaching.platform.testing.EquivalenceTester.check;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class RangeTest {

  @Test
  public void testContains() throws Exception {
    assertTrue(new Range(0, 5).contains(new Range(2, 4)));
    assertTrue(new Range(0, 5).contains(new Range(0, 5)));
    assertFalse(new Range(3, 5).contains(new Range(0, 5)));
    assertFalse(new Range(0, 5).contains(new Range(0, 6)));
    assertFalse(new Range(0, 5).contains(new Range(7, 8)));
  }

  @Test
  public void testOverlaps() throws Exception {
    assertTrue(new Range(0, 5).overlaps(new Range(2, 4)));
    assertTrue(new Range(0, 5).overlaps(new Range(0, 5)));
    assertTrue(new Range(3, 5).overlaps(new Range(0, 5)));
    assertTrue(new Range(0, 5).overlaps(new Range(0, 6)));
    assertFalse(new Range(0, 5).overlaps(new Range(7, 8)));
    assertFalse(new Range(4, 6).overlaps(new Range(0, 4)));
    assertTrue(new Range(3, 6).overlaps(new Range(0, 4)));
    assertFalse(new Range(4, 6).overlaps(new Range(6, 7)));
    assertTrue(new Range(4, 6).overlaps(new Range(5, 7)));
    assertTrue(new Range(0, 22).overlaps(new Range(13, 16)));
    assertTrue(new Range(13, 16).overlaps(new Range(0, 22)));
  }

  @Test
  public void equivalence() {
    check(
        newArrayList(
            new Range(0, 2),
            new Range(0, 2)),
        newArrayList(
            new Range(1, 3),
            new Range(1, 3)));
  }

  @Test public void itShouldBeFunctional() {
    List<Integer> accumulator = new ArrayList<Integer>();
    for (Integer i : range(1, 10)) {
      accumulator.add(i);
    }
    assertEquals(newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9), accumulator);
  }

  @Test public void itShouldBeFunctionalCountdown() {
    List<Integer> accumulator = new ArrayList<Integer>();
    for (Integer i : range(10, 0)) {
      accumulator.add(i);
    }
    assertEquals(newArrayList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1), accumulator);
  }

}
