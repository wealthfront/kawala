package com.kaching.platform.testing;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

/**
 * Equivalence tester streamlining tests of {@link #equals} and
 * {@link #hashCode} methods. Using this tester makes it easy to verify that
 * {@link #equals} is indeed an
 * <a href="http://en.wikipedia.org/wiki/Equivalence_relation">equivalence relation</a>
 * (reflexive, symmetric and transitive). It also verifies that
 * equality between two objects implies hash code equality, as required by the
 * {@link #hashCode()} contract.
 */
public class EquivalenceTester {

  public static void check(Collection<? extends Object>... equivalenceClasses) {
    List<List<Object>> ec =
        newArrayListWithExpectedSize(equivalenceClasses.length);

    // reflexivity
    for (Collection<? extends Object> congruenceClass : equivalenceClasses) {
      List<Object> c = newArrayList();
      ec.add(c);
      for (Object element : congruenceClass) {
        assertTrue(format("reflexivity of %s", element),
            element.equals(element));
        c.add(element);
      }
    }

    // equality within congruence classes
    for (List<Object> c : ec) {
      for (int i = 0; i < c.size(); i++) {
        Object e1 = c.get(i);
        for (int j = i + 1; j < c.size(); j++) {
          Object e2 = c.get(j);
          assertTrue(format("%s=%s", e1, e2), e1.equals(e2));
          assertTrue(format("%s=%s", e2, e1), e2.equals(e1));
          assertEquals(format("hashCode %s vs. %s", e1, e2), e1.hashCode(), e2.hashCode());
        }
      }
    }

    // inequality across congruence classes
    for (int i = 0; i < ec.size(); i++) {
      List<Object> c1 = ec.get(i);
      for (int j = i + 1; j < ec.size(); j++) {
        List<Object> c2 = ec.get(j);
        for (Object e1 : c1) {
          for (Object e2 : c2) {
            assertFalse(format("%s!=%s", e1, e2), e1.equals(e2));
            assertFalse(format("%s!=%s", e2, e1), e2.equals(e1));
          }
        }
      }
    }
  }

}
