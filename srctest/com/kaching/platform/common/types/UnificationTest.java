/**
 * Copyright 2010 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.common.types;

import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.inject.TypeLiteral;
import com.kaching.platform.converters.Converter;

@SuppressWarnings("unchecked")
public class UnificationTest {

  @Test
  public void testGetReturnType1() throws Exception {
    assertEquals(
        new TypeLiteral<Integer>() {}.getType(),
        queryReturnType(IntegerQuery.class));
  }

  @Test
  public void testGetReturnType2() throws Exception {
    assertEquals(
        new TypeLiteral<Double>() {}.getType(),
        queryReturnType(DoubleAbstractQuery.class));
  }

  @Test
  public void testGetReturnType3() throws Exception {
    assertEquals(
        new TypeLiteral<List<Integer>>() {}.getType(),
        queryReturnType(IntegerListQuery.class));
  }

  @Test
  public void testGetReturnType4() throws Exception {
    assertEquals(
        new TypeLiteral<List<Double>>() {}.getType(),
        queryReturnType(DoubleListAbstractQuery.class));
  }

  @Test
  public void testGetReturnType5() throws Exception {
    assertEquals(
        new TypeLiteral<Double>() {}.getType(),
        queryReturnType(DoubleSecuredQuery.class));
  }

  @Test
  public void testGetReturnType6() throws Exception {
    assertEquals(
        new TypeLiteral<String>() {}.getType(),
        queryReturnType(StringQuery.class));
  }

  @Test
  public void testGetReturnType7() throws Exception {
    assertEquals(
        new TypeLiteral<byte[]>() {}.getType(),
        queryReturnType(ByteArrayQuery.class));
  }

  @Test
  public void instanceOfManyTypeParams1() throws Exception {
    assertEquals(
        new TypeLiteral<Short>() {}.getType(),
        Unification.getActualTypeArgument(
            InstanceOfManyTypeParams.class, ManyTypeParams.class, 0));
  }

  @Test
  public void instanceOfManyTypeParams2() throws Exception {
    assertEquals(
        new TypeLiteral<Map>() {}.getType(),
        Unification.getActualTypeArgument(
            InstanceOfManyTypeParams.class, ManyTypeParams.class, 1));
  }

  /* MUST PORT SCALA RELATED TESTS!
  @Test
  public void instanceOfManyTypeParams3() throws Exception {
    assertEquals(
        new TypeLiteral<List>() {}.getType(),
        Unification.getActualTypeArgument(
            InstanceOfManyTypeParams.class, ManyTypeParams.class, 2));
  }

  @Test
  public void scalaInstanceOfManyTypeParams0() throws Exception {
    assertEquals(
        new TypeLiteral<URI>() {}.getType(),
        Unification.getActualTypeArgument(
            ScalaInstanceOfManyTypeParams.class, ManyTypeParams.class, 0));
  }

  @Test
  public void scalaInstanceOfManyTypeParams1() throws Exception {
    assertEquals(
        new TypeLiteral<String>() {}.getType(),
        Unification.getActualTypeArgument(
            ScalaInstanceOfManyTypeParams.class, ManyTypeParams.class, 1));
  }

  @Test
  public void scalaInstanceOfManyTypeParams2() throws Exception {
    assertEquals(
        new TypeLiteral<Byte>() {}.getType(),
        Unification.getActualTypeArgument(
            ScalaInstanceOfManyTypeParams.class, ManyTypeParams.class, 2));
  }

  @Test
  @Ignore("scalac does not put type parameters in bytecode when extending an abstract class")
  public void scalaInstanceOfAbstractManyTypeParams0() throws Exception {
    assertEquals(
        new TypeLiteral<URI>() {}.getType(),
        Unification.getActualTypeArgument(
            ScalaInstanceOfAbstractManyTypeParams.class, ManyTypeParams.class, 0));
  }

  JAVA CODE

  static class AbstractManyTypeParams<A, B, C> implements ManyTypeParams<A, B, C> {
  }

  SCALA CODE

  class ScalaInstanceOfManyTypeParams extends ManyTypeParams[URI, String, Byte] {
  }

  class ScalaInstanceOfAbstractManyTypeParams extends AbstractManyTypeParams[URI, String, Byte] {
  }

  */

  @Test(expected = IllegalArgumentException.class)
  public void isNotParameterized() throws Exception {
    Unification.getActualTypeArgument(
        Double.class, Double.class, 0);
  }

  @Test
  public void isNotSubtype() throws Exception {
    try {
      Unification.getActualTypeArgument(
          Foo.class, MidLevel1.class, 0);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(
          "class com.kaching.platform.common.types.UnificationTest$Foo " +
          "does not have class com.kaching.platform.common.types.UnificationTest$MidLevel1 " +
          "as super class",
          e.getMessage());
    }
  }

  @Test
  public void classExtendingClass1() throws Exception {
    assertEquals(
        String.class,
        Unification.getActualTypeArgument(
          Sub1.class, Sup1.class, 0));
  }

  static class Sup1<T> {}
  static class Sub1 extends Sup1<String> {}

  @Test
  public void classExtendingClass2() throws Exception {
    assertEquals(
        String.class,
        Unification.getActualTypeArgument(
          SubSub2.class, Sup2.class, 0));
  }

  @Test
  public void classExtendingClass2WithNoParametrization() throws Exception {
    try {
      Unification.getActualTypeArgument(
          SubNoParam2.class, Sup2.class, 0);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(
          "class com.kaching.platform.common.types.UnificationTest$SubNoParam2 " +
          "does extend parametrically class com.kaching.platform.common.types.UnificationTest$Sup2",
          e.getMessage());
    }
  }

  static class SupSup2 {}
  static class Sup2<T> extends SupSup2 {}
  static class Sub2 extends Sup2<String> {}
  static class SubNoParam2 extends Sup2 {}
  static class SubSub2 extends Sub2 {}

  @Test
  public void implementsMultipleInterfaces() throws Exception {
    for (int i = 0; i < 3; i++) {
      assertEquals(
          asList(String.class, Double.class, Integer.class).get(i),
          Unification.getActualTypeArgument(
              MultipleInterfaces.class, ManyTypeParams.class, i));
    }
  }

  static class MultipleInterfaces
      implements TopLevel<Type>, ManyTypeParams<String, Double, Integer> {}

  @Test
  public void implementsMultipleInterfacesSomeWithNoParametrization() throws Exception {
    for (int i = 0; i < 3; i++) {
      assertEquals(
          asList(String.class, Double.class, Integer.class).get(i),
          Unification.getActualTypeArgument(
              MultipleInterfacesSomeWithNoParametrization.class, ManyTypeParams.class, i));
    }
  }

  static class MultipleInterfacesSomeWithNoParametrization
      implements TopLevel, ManyTypeParams<String, Double, Integer> {}

  @Test
  public void listOfIntConverter() throws Exception {
    assertEquals(
        new TypeLiteral<List<Integer>>() {}.getType(),
        Unification.getActualTypeArgument(
            ListOfIntConverter.class, Converter.class, 0));
  }

  abstract static class CsvValuesListConverter<L> implements Converter<List<L>> {}
  static class ListOfIntConverter extends CsvValuesListConverter<Integer> {
    @Override public String toString(List<Integer> value) { return null; }
    @Override public List<Integer> fromString(String representation) { return null; }
  }

  private Type queryReturnType(Class<?> query) {
    return Unification.getActualTypeArgument(query, TopLevel.class, 0);
  }

  private static class Foo {}

  private interface ManyTypeParams<A, B, C> {}

  private static class InstanceOfManyTypeParams implements ManyTypeParams<Short, Map, List> {}

  private interface TopLevel<T> {}

  private static class MidLevel1<T> implements TopLevel<T> {}

  private static class MidLevel2<T> implements TopLevel<T> {}

  private static abstract class StubQuery extends MidLevel1<String> {}

  private static class ByteArrayQuery extends MidLevel1<byte[]> {}

  private static class DoubleAbstractQuery extends MidLevel1<Double> {}

  private static class IntegerQuery implements TopLevel<Integer> {}

  private static class IntegerListQuery implements TopLevel<List<Integer>> {}

  private static class DoubleListAbstractQuery extends MidLevel1<List<Double>> {}

  private static class DoubleSecuredQuery extends MidLevel2<Double> {}

  private static class StringQuery extends StubQuery {}

}
