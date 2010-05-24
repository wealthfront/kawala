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
package com.kaching.platform.converters;

import static com.kaching.platform.converters.NativeConverters.C_BOOLEAN;
import static com.kaching.platform.converters.NativeConverters.C_BYTE;
import static com.kaching.platform.converters.NativeConverters.C_CHAR;
import static com.kaching.platform.converters.NativeConverters.C_DOUBLE;
import static com.kaching.platform.converters.NativeConverters.C_FLOAT;
import static com.kaching.platform.converters.NativeConverters.C_INT;
import static com.kaching.platform.converters.NativeConverters.C_LONG;
import static com.kaching.platform.converters.NativeConverters.C_SHORT;
import static com.kaching.platform.converters.NativeConverters.C_STRING;
import static java.lang.String.format;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.BindingAnnotation;
import com.google.inject.name.Named;
import com.kaching.platform.common.Option;

public class InstantiatorImplFactoryTest {

  private InstantiatorImplFactory<?> factory;

  @Before
  public void before() {
    factory = new InstantiatorImplFactory<Object>(null);
  }

  @Test(expected = RuntimeException.class)
  public void buildShouldFailIfErrorsExist() {
    new InstantiatorImplFactory<SomethingUsingHasConvertedByWrongBound>(SomethingUsingHasConvertedByWrongBound.class).build();
  }

  static class SomethingUsingHasConvertedByWrongBound {
    SomethingUsingHasConvertedByWrongBound(HasConvertedByWrongBound p0) {
    }
  }

  @Test
  public void createConverterConvertedBy() throws Exception {
    Converter<?> converter = factory.createConverter(HasConvertedBy.class, null).getOrThrow();
    assertNotNull(converter);
    assertEquals(HasConvertedByConverter.class, converter.getClass());
  }

  @ConvertedBy(HasConvertedByConverter.class)
  static class HasConvertedBy {
  }

  static class HasConvertedByConverter implements Converter<HasConvertedBy> {
    @Override
    public String toString(HasConvertedBy value) { return null; }
    @Override
    public HasConvertedBy fromString(String representation) { return null; }
  }

  @Test
  public void createConverterConvertedByWrongBound() throws Exception {
    factory.createConverter(HasConvertedByWrongBound.class, null);
    assertEquals(
        new Errors().incorrectBoundForConverter(
            HasConvertedByWrongBound.class,
            HasConvertedByConverterWrongBound.class,
            String.class),
        factory.getErrors());
  }

  @ConvertedBy(HasConvertedByConverterWrongBound.class)
  static class HasConvertedByWrongBound {
  }

  /* This converter does not produce objects of type HasConvertedByWrongBound
   * and therefore cannot be used as a converter for HasConvertedByWrongBound.
   */
  static class HasConvertedByConverterWrongBound implements Converter<String> {
    @Override
    public String toString(String value) { return null; }
    @Override
    public String fromString(String representation) { return null; }
  }

  @Test
  public void createConverterDefaultIfHasStringConstructor() throws Exception {
    Converter<?> converter =
        factory.createConverter(HasStringConstructor.class, null).getOrThrow();
    assertNotNull(converter);
    assertEquals(StringConstructorConverter.class, converter.getClass());
  }

  @Test
  public void createConverterNatives() throws Exception {
    Object[] fixtures = new Object[] {
        C_STRING, String.class,
        C_BOOLEAN, Boolean.TYPE,
        C_BYTE, Byte.TYPE,
        C_CHAR, Character.TYPE,
        C_DOUBLE, Double.TYPE,
        C_FLOAT, Float.TYPE,
        C_INT, Integer.TYPE,
        C_LONG, Long.TYPE,
        C_SHORT, Short.TYPE
    };
    for (int i = 0; i < fixtures.length; i += 2) {
      String message = format("type %s", fixtures[i + 1]);
      Option<? extends Converter<?>> converter =
          factory.createConverter((Type) fixtures[i + 1], null);
      assertTrue(message, converter.isDefined());
      assertEquals(message, fixtures[i], converter.getOrThrow());
    }
  }

  static class HasStringConstructor {
    HasStringConstructor(String representation) {
    }
  }

  @Test
  public void getPublicConstructor() throws Exception {
    factory.getConstructor(A.class);
  }

  @Test
  public void getProtectedConstructor() throws Exception {
    factory.getConstructor(B.class);
  }

  @Test
  public void getDefaultVisibleConstructor() throws Exception {
    factory.getConstructor(C.class);
  }

  @Test
  public void getPrivateConstructor() throws Exception {
    factory.getConstructor(D.class);
  }

  @Test
  public void getNonExistingConstructor() throws Exception {
    try {
      factory.getConstructor(E.class);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void getNonUniqueConstructor() throws Exception {
    try {
      factory.getConstructor(F.class);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void getConstructorFromSuperclass() throws Exception {
    factory.getConstructor(G.class);
  }

  @Test
  public void getConstructorFromSuperclassWithMultipleConstructors() {
    factory.getConstructor(H.class);
  }

  @Test
  public void getNonUniqueConstructorWithAnnotation1() throws Exception {
    assertNotNull(factory.getConstructor(P.class));
  }

  @Test
  public void getNonUniqueConstructorWithAnnotation2() throws Exception {
    factory.getConstructor(Q.class);
    assertEquals(
        new Errors().moreThanOnceConstructorWithInstantiate(Q.class),
        factory.getErrors());
  }

  static class A {
    public A() {}
  }

  static class B {
    protected B() {}
  }

  static class C {
    C() {}
  }

  static class D {
    private D() {}
  }

  static interface E {
  }

  static class F {
    F() {}
    F(String s) {}
  }

  static class G extends A {}

  static class H extends F {}

  static class I {
    I(String s) {}
  }

  static class J {
    J(List<String> l) {}
  }

  static class K {
    K(@Named("k") String l) {}
  }

  static class L {
    L(@Named("l") @LocalBindingAnnotation String l) {}
  }

  static class M {
    M(@Optional String s) {}
  }

  static class N {
    N(String s, @Optional String t) {}
  }

  static class O {
    O(String s, @Optional("foo") String t, String u) {}
  }

  static class P {
    P() {}
    @Instantiate P(String s) {}
  }

  static class Q {
    @Instantiate Q() {}
    @Instantiate Q(String s) {}
  }

  @Retention(RUNTIME)
  @Target({ ElementType.FIELD, ElementType.PARAMETER })
  @BindingAnnotation
  static @interface LocalBindingAnnotation { }

}
