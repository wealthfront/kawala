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
import static com.kaching.platform.testing.EquivalenceTester.check;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class PairTest {

  @Test
  @SuppressWarnings("unchecked")
  public void equivalence() {
    check(
        newArrayList(
            new Pair<Integer, Integer>(0, 2),
            new Pair<Integer, Integer>(0, 2)),
        newArrayList(
            new Pair<Integer, Integer>(1, 3),
            new Pair<Integer, Integer>(1, 3)),
        newArrayList(
            new Pair<String, Integer>("xyz", 0),
            new Pair<String, Integer>("xyz", 0)),
        newArrayList(
            new Pair<String, Double>("xyz", null),
            new Pair<String, Double>("xyz", null)),
        newArrayList(
            new Pair<String, Integer>(null, 0),
            new Pair<String, Integer>(null, 0))
    );
  }

  @Test
  public void factory() {
    assertEquals("abc", Pair.of("abc", 123).left);
    assertEquals(new Integer(123), Pair.of("abc", 123).right);
  }

}
