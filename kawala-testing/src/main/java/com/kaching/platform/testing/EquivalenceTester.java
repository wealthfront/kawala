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

  public static void check(Collection<?>... equivalenceClasses) {
    List<List<Object>> ec =
        newArrayListWithExpectedSize(equivalenceClasses.length);

    // nothing can be equal to null
    for (Collection<? extends Object> congruenceClass : equivalenceClasses) {
      for (Object element : congruenceClass) {
        try {
          assertFalse(
              format("%s can not be equal to null", element),
              element.equals(null));
        } catch (NullPointerException e) {
          throw new AssertionError(
              format("NullPointerException when comparing %s to null", element));
        }
      }
    }

    // reflexivity
    for (Collection<? extends Object> congruenceClass : equivalenceClasses) {
      List<Object> c = newArrayList();
      ec.add(c);
      for (Object element : congruenceClass) {
        assertTrue(format("reflexivity of %s", element),
            element.equals(element));
        compareShouldReturn0(element, element);
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
          compareShouldReturn0(e1, e2);
          compareShouldReturn0(e2, e1);
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
            compareShouldNotReturn0(e1, e2);
            compareShouldNotReturn0(e2, e1);
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void compareShouldReturn0(Object e1, Object e2) {
    if (e1 instanceof Comparable<?> &&
        e2 instanceof Comparable<?>) {
      assertTrue(format("comparison should return 0 for %s and %s", e1, e2),
          ((Comparable<Object>) e1).compareTo(e2) == 0);
    }
  }

  @SuppressWarnings("unchecked")
  private static void compareShouldNotReturn0(Object e1, Object e2) {
    if (e1 instanceof Comparable<?> &&
        e2 instanceof Comparable<?>) {
      assertFalse(format("comparison should not return 0 for %s and %s", e1, e2),
          ((Comparable<Object>) e1).compareTo(e2) == 0);
    }
  }

}
