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
package com.kaching.platform.guice;

import static com.kaching.platform.testing.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.inject.TypeLiteral;
import com.kaching.platform.common.Pair;

public class TypeLiteralsTest {

  @Test
  public void get1() throws Exception {
    TypeLiteral<?> expected = new TypeLiteral<List<Foo>>() {};
    TypeLiteral<?> actual = TypeLiterals.get(List.class, Foo.class);

    assertEquals(expected, actual);
  }

  @Test
  public void get2() throws Exception {
    TypeLiteral<?> expected = new TypeLiteral<Pair<Integer, Double>>() {};
    TypeLiteral<?> actual = TypeLiterals.get(
        Pair.class, Integer.class, Double.class);

    assertEquals(expected, actual);
  }

  @Test
  public void get3() throws Exception {
    TypeLiteral<?> expected = new TypeLiteral<List<List<Foo>>>() {};
    TypeLiteral<?> inner = new TypeLiteral<List<Foo>>() {};
    TypeLiteral<?> actual = TypeLiterals.get(List.class, inner);

    assertEquals(expected, actual);
  }

  @Test
  public void get4() throws Exception {
    TypeLiteral<?> a = TypeLiterals.get(List.class, Integer.class);
    TypeLiteral<?> b = TypeLiterals.get(List.class, Double.class);

    assertNotEquals(a, b);
  }

  @Test
  public void get5() throws Exception {
    TypeLiteral<?> a = TypeLiterals.get(List.class, new TypeLiteral<Double>() {});
    TypeLiteral<?> b = TypeLiterals.get(List.class, Double.class);

    assertEquals(a, b);
  }

  @Test
  public void toString1() throws Exception {
    assertEquals("java.util.List<java.lang.Double>",
        TypeLiterals.get(List.class, Double.class).toString());
  }

  @Test
  public void toString2() throws Exception {
    assertEquals("java.util.Map<java.lang.Integer, java.lang.Double>",
        TypeLiterals.get(Map.class, Integer.class, Double.class).toString());
  }

  @Test
  public void toString3() throws Exception {
    assertEquals("java.util.List<java.lang.Double>",
        TypeLiterals.get(List.class, new TypeLiteral<Double>() {}).toString());
  }

  static class Foo {}

}
