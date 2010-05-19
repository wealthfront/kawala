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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.BindingAnnotation;
import com.google.inject.name.Named;

public class InstantiatorImplFactoryTest {

  @Test
  public void createConverterConvertedBy() throws Exception {
    Converter<?> converter =
        InstantiatorImplFactory.createConverter(HasConvertedBy.class, null);
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
  @Ignore
  public void createConverterConvertedByWrongBound() throws Exception {
    InstantiatorImplFactory.createConverter(HasConvertedByWrongBound.class, null);
    // TODO(pascal): the createConverter must return and error or a converter,
    // here we would check that the error clearly indicates the wrong bound
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
        InstantiatorImplFactory.createConverter(HasStringConstructor.class, null);
    assertNotNull(converter);
    assertEquals(StringConstructorConverter.class, converter.getClass());
  }

  static class HasStringConstructor {
    HasStringConstructor(String representation) {
    }
  }

  @Test
  public void getPublicConstructor() throws Exception {
    InstantiatorImplFactory.getConstructor(A.class);
  }

  @Test
  public void getProtectedConstructor() throws Exception {
    InstantiatorImplFactory.getConstructor(B.class);
  }

  @Test
  public void getDefaultVisibleConstructor() throws Exception {
    InstantiatorImplFactory.getConstructor(C.class);
  }

  @Test
  public void getPrivateConstructor() throws Exception {
    InstantiatorImplFactory.getConstructor(D.class);
  }

  @Test
  public void getNonExistingConstructor() throws Exception {
    try {
      InstantiatorImplFactory.getConstructor(E.class);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void getNonUniqueConstructor() throws Exception {
    try {
      InstantiatorImplFactory.getConstructor(F.class);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void getConstructorFromSuperclass() throws Exception {
    InstantiatorImplFactory.getConstructor(G.class);
  }

  @Test
  public void getConstructorFromSuperclassWithMultipleConstructors() {
    InstantiatorImplFactory.getConstructor(H.class);
  }

  @Test
  public void getNonUniqueConstructorWithAnnotation1() throws Exception {
    assertNotNull(InstantiatorImplFactory.getConstructor(P.class));
  }

  @Test
  public void getNonUniqueConstructorWithAnnotation2() throws Exception {
    try {
      InstantiatorImplFactory.getConstructor(Q.class);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
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
