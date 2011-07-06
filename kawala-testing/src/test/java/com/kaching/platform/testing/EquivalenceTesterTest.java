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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.fail;

import org.junit.Test;

public class EquivalenceTesterTest {

  @Test(expected = AssertionError.class)
  public void nothinCanBeEqualToNull() {
    EquivalenceTester.check(newArrayList(new EqualsDoesNotHandleNullArg()));
  }

  static class EqualsDoesNotHandleNullArg {
    @Override
    public boolean equals(Object that) {
      checkNotNull(that);
      return true;
    }
  }

  @Test
  public void comparisonCompliance() {
    EquivalenceTester.check(
        newArrayList(weird(0, 2), weird(0, 12)),
        newArrayList(weird(0, 5), weird(0, 15)));
  }

  @Test
  public void comparisonCompliance_wontReturn0() {
    try {
      EquivalenceTester.check(
          newArrayList(weird(1, 2), weird(2, 12)),
          newArrayList(weird(2, 5), weird(0, 15)));
      fail("Comparison should have returned non-zero");
    } catch (Throwable t) {
      // as expected
    }
  }

  @Test
  public void comparableAndNotComparable() {
    EquivalenceTester.check(
        newArrayList(new NotComparable()),
        newArrayList("Hello"));
  }

  WeirdClass weird(int shift, int value) {
    return new WeirdClass(shift, value);
  }

  static class WeirdClass implements Comparable<WeirdClass> {
    private int shift;
    private int value;

    WeirdClass(int shift, int value) {
      this.shift = shift;
      this.value = value;
    }

    @Override
    public int compareTo(WeirdClass other) {
      return (shift + value) % 10 - (other.shift + other.value) % 10;
    }

    public boolean equals(Object o) {
      return o instanceof WeirdClass
          && (value - ((WeirdClass) o).value) % 10 == 0;
    }

    public int hashCode() {
      return (((shift + value) % 10) + 3);
    }

    public String toString() {
      return "WeirdClass(" + shift + "," + value + ")";
    }
  }

  static class NotComparable {
  }

}
