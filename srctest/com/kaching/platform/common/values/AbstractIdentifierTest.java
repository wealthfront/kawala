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
package com.kaching.platform.common.values;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.kaching.platform.testing.EquivalenceTester;

public class AbstractIdentifierTest {

  @Test(expected = NullPointerException.class)
  public void creationWithNull() {
    new MyId1(null);
  }

  @Test
  public void equivalence() {
    EquivalenceTester.check(
        newArrayList(
            new MyId1("a"), new MyId1("a")),
        newArrayList(
            new MyId2("a"), new MyId2("a")),
        newArrayList(
            new MyId1("b"), new MyId1("b")),
        newArrayList(
            new MyId2("b"), new MyId2("b")));
  }
  
  @Test
  public void toStringTest() {
    assertEquals("a", new MyId1("a").toString());
  }
  
  @Test
  public void compareToTest() {
    assertEquals(1, new MyId1("a").compareTo(null));
    compareToTest("a", "a");
    compareToTest("a", "b");
  }

  private void compareToTest(String id1, String id2) {
    assertEquals(
        String.format("%s.compareTo(%s)", id1, id2),
        id1.compareTo(id2),
        new MyId1(id1).compareTo(new MyId1(id2)));
    assertEquals(
        String.format("%s.compareTo(%s)", id2, id1),
        id2.compareTo(id1),
        new MyId1(id2).compareTo(new MyId1(id1)));
  }
  
  static class MyId1 extends AbstractIdentifier<String> {
    private static final long serialVersionUID = 937579551932508792L;
    public MyId1(String id) {
      super(id);
    }
  }
  
  static class MyId2 extends AbstractIdentifier<String> {
    private static final long serialVersionUID = -4598517783225143579L;
    public MyId2(String id) {
      super(id);
    }
  }
  
}
