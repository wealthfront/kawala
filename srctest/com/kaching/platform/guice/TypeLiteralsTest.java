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
