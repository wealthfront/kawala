package com.kaching.platform.hibernate;

import static com.google.common.collect.Lists.newArrayList;
import static com.kaching.platform.hibernate.Id.list;
import static com.kaching.platform.hibernate.Id.toLongs;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.kaching.platform.testing.EquivalenceTester;

public class IdTest {

  @Test
  public void equalities() {
	EquivalenceTester.check(
			newArrayList(newArrayList(Id.of(123L))));
  }

  @Test
  public void equal2() {
    testEqual(Id.<F>of(123L), Id.<G>of(123L));
  }

  @Test
  public void equal3() {
    testEqual(Id.<F>of(9178971216545321525L), Id.<F>of(9178971216545321525L));
  }

  @Test
  public void smaller1() {
    testSmaller(Id.<F>of(123L), Id.<F>of(125L));
  }

  @Test
  public void smaller2() {
    testSmaller(Id.<F>of(123L), Id.<F>of(178971216545321525L));
  }

  @Test
  public void smaller3() {
    testSmaller(Id.<F>of(178971216545321524L), Id.<F>of(9178971216545321525L));
  }

  @Test
  public void compareTo1() {
    assertTrue(Id.<F>of(123L).compareTo(null) > 0);
  }

  @Test
  public void compareTo2() {
    assertTrue(Id.<F>of(9178971216545321525L).compareTo(null) > 0);
  }

  @Test
  public void listOfLongs() {
    assertEquals(singletonList(Id.<F>of(1L)), list(1L));
  }

  @Test
  public void listOfNoLongs() {
    assertEquals(emptyList(), list());
  }

  @Test
  public void toLongsNonEmpty() {
    assertEquals(asList(1L), toLongs(singletonList(Id.<F>of(1L))));
  }

  @Test
  public void toLongsEmpty() {
    assertEquals(emptyList(), toLongs(Collections.<Id<F>>emptyList()));
  }

  @SuppressWarnings("unchecked")
  private void testEqual(Id id1, Id id2) {
    // compareTo
    assertTrue(id1.compareTo(id2) == 0);
    assertTrue(id2.compareTo(id1) == 0);

    // equals
    assertTrue(id1.equals(id2));
    assertTrue(id2.equals(id1));

    // hashCode must be the same
    assertEquals(id1.hashCode(), id2.hashCode());
  }

  @SuppressWarnings("unchecked")
  private void testSmaller(Id id1, Id id2) {
    // compareTo
    assertTrue(id1.compareTo(id2) < 0);
    assertTrue(id2.compareTo(id1) > 0);

    // equals
    assertFalse(id1.equals(id2));
    assertFalse(id2.equals(id1));
  }

  private static class F implements HibernateEntity {
    public Id<F> getId() {
      throw new UnsupportedOperationException();
    }
  }

  private static class G implements HibernateEntity {
    public Id<G> getId() {
      throw new UnsupportedOperationException();
    }
  }

}
