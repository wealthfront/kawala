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
package com.kaching.platform.converters;

import static com.google.common.collect.Lists.newArrayList;
import static com.kaching.platform.converters.InstantiatorErrors.cannotAnnotateOptionWithOptional;
import static com.kaching.platform.converters.InstantiatorErrors.cannotSpecifyDefaultValueAndConstant;
import static com.kaching.platform.converters.InstantiatorErrors.constantHasIncompatibleType;
import static com.kaching.platform.converters.InstantiatorErrors.duplicateConverterBindingForType;
import static com.kaching.platform.converters.InstantiatorErrors.enumHasAmbiguousNames;
import static com.kaching.platform.converters.InstantiatorErrors.illegalConstructor;
import static com.kaching.platform.converters.InstantiatorErrors.incorrectBoundForConverter;
import static com.kaching.platform.converters.InstantiatorErrors.incorrectDefaultValue;
import static com.kaching.platform.converters.InstantiatorErrors.moreThanOneConstructor;
import static com.kaching.platform.converters.InstantiatorErrors.moreThanOneConstructorWithInstantiate;
import static com.kaching.platform.converters.InstantiatorErrors.moreThanOneMatchingFunction;
import static com.kaching.platform.converters.InstantiatorErrors.noConstructorFound;
import static com.kaching.platform.converters.InstantiatorErrors.noConverterForType;
import static com.kaching.platform.converters.InstantiatorErrors.noSuchField;
import static com.kaching.platform.converters.InstantiatorErrors.optionalLiteralParameterMustHaveDefault;
import static com.kaching.platform.converters.InstantiatorErrors.unableToResolveConstant;
import static com.kaching.platform.converters.InstantiatorErrors.unableToResolveFullyQualifiedConstant;
import static com.kaching.platform.converters.InstantiatorImplFactory.createFactory;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.inject.BindingAnnotation;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.kaching.platform.common.Errors;
import com.kaching.platform.common.Option;
import com.kaching.platform.converters.ConstructorAnalysis.FormalParameter;

public class InstantiatorImplFactoryTest {

  private Errors actualErrors;

  @Before
  public void createErrors() {
    actualErrors = new Errors();
  }

  public void buildShouldReportIfErrorsExist() {
    assertFalse(InstantiatorImplFactory
        .createFactory(actualErrors, SomethingUsingHasConvertedByWrongBound.class)
        .build()
        .isDefined());
    assertTrue(actualErrors.hasErrors());
  }

  static class SomethingUsingHasConvertedByWrongBound {
    SomethingUsingHasConvertedByWrongBound(HasConvertedByWrongBound p0) {
    }
  }

  @Test
  public void createConverterConvertedBy() throws Exception {
    Converter<?> converter = createFactory(actualErrors, null).createConverter(HasConvertedBy.class).getOrThrow();
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
    InstantiatorImplFactory<Object> factory = createFactory(actualErrors, null);
    factory.createConverter(HasConvertedByWrongBound.class);
    assertEquals(
        incorrectBoundForConverter(
            new Errors(),
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
      createFactory(actualErrors, null).createConverter(HasStringConstructor.class).getOrThrow();
    assertNotNull(converter);
    assertEquals(StringConstructorConverter.class, converter.getClass());
  }

  static class HasStringConstructor {
    final String representation;
    HasStringConstructor(String representation) {
      this.representation = representation;
    }
  }

  @Test
  public void createConverterDefaultIfHasStringConstructorParameterized() throws Exception {
    Converter<?> converter = createFactory(actualErrors, null)
        .createConverter(new TypeLiteral<HasStringConstructorParam<Double>>(){}.getType())
        .getOrThrow();
    assertNotNull(converter);
    assertEquals(StringConstructorConverter.class, converter.getClass());
  }

  static class HasStringConstructorParam<T> {
    final String representation;
    public HasStringConstructorParam(String representation) {
      this.representation = representation;
    }
  }

  @Test
  public void createConverterForEnum() throws Exception {
    Converter<?> converter = createFactory(actualErrors, null).createConverter(AnEnum.class).getOrThrow();
    assertNotNull(converter);
    assertEquals(EnumConverter.class, converter.getClass());
  }

  static enum AnEnum {
    FOO, BAR
  }

  @Test
  public void createConverterForAmbiguousEnum() throws Exception {
    InstantiatorImplFactory<Object> factory = createFactory(actualErrors, null);
    factory.createConverter(AmbiguousEnum.class);
    assertEquals(
        enumHasAmbiguousNames(new Errors(), AmbiguousEnum.class),
        factory.getErrors());
  }

  static enum AmbiguousEnum {
    FOO, Foo
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
        createFactory(actualErrors, null).createConverter((Type) fixtures[i + 1]);
      assertTrue(message, converter.isDefined());
      assertEquals(message, fixtures[i], converter.getOrThrow());
    }
  }

  @Test
  public void createInstantiatorWithIncorrectDefaultValue() throws Exception {
    checkErrorCase(
        WrongDefaultValue.class,
        incorrectDefaultValue(
            new Errors(),
            "foobar",
            new IllegalArgumentException()));
  }

  static class WrongDefaultValue {
    WrongDefaultValue(@Optional("foobar") char c) {
    }
  }

  @Test
  public void convertedAnnotatedClass() throws Exception {
    InstantiatorImplFactory<Object> factory = createFactory(actualErrors, null);
    Converter<?> converter = factory.createConverter(AnnotatedClass.class).getOrThrow();
    assertEquals(StringConstructorConverter.class, converter.getClass());
  }

  @Test
  public void convertedAnnotatedClassWithFunction() throws Exception {
    InstantiatorImplFactory<Object> factory = createFactory(actualErrors, null);
    InstantiatorModule module = new AbstractInstantiatorModule() {
      @Override
      protected void configure() {
        register(new Function<Type, Option<? extends Converter<?>>>() {
          @SuppressWarnings("unchecked")
          @Override
          public Option<? extends Converter<?>> apply(Type type) {
            if (type instanceof Class &&
                ((Class) type).getAnnotation(AnAnnotation.class) != null) {
              return Option.some(new ConverterForAnnotatedClass());
            } else {
              return Option.none();
            }
          }
        });
      }
    };
    module.configure(factory.binder());
    Converter<?> converter = factory.createConverter(AnnotatedClass.class).getOrThrow();
    assertEquals(ConverterForAnnotatedClass.class, converter.getClass());

    Converter<?> converter2 = factory.createConverter(NonAnnotatedClass.class).getOrThrow();
    assertEquals(StringConstructorConverter.class, converter2.getClass());
  }

  @Test
  public void convertedAnnotatedClassWithTwoFunctions() throws Exception {
    InstantiatorImplFactory<Object> factory = createFactory(actualErrors, null);
    InstantiatorModule module = new AbstractInstantiatorModule() {
      @Override
      protected void configure() {
        Function<Type, Option<? extends Converter<?>>> function =
            new Function<Type, Option<? extends Converter<?>>>() {
              @SuppressWarnings("unchecked")
              @Override
              public Option<? extends Converter<?>> apply(Type type) {
                if (type instanceof Class &&
                    ((Class) type).getAnnotation(AnAnnotation.class) != null) {
                  return Option.some(new ConverterForAnnotatedClass());
                } else {
                  return Option.none();
                }
              }
            };
        register(function);
        register(function);
      }
    };
    module.configure(factory.binder());
    factory.createConverter(AnnotatedClass.class);
    assertEquals(
        moreThanOneMatchingFunction(new Errors(), AnnotatedClass.class),
        factory.getErrors());
  }

  @AnAnnotation
  static class AnnotatedClass {
    String value;
    AnnotatedClass(String value) {
      this.value = value;
    }
  }

  static class NonAnnotatedClass {
    String value;
    NonAnnotatedClass(String value) {
      this.value = value;
    }
  }

  @Retention(RUNTIME)
  @interface AnAnnotation {
  }

  static class ConverterForAnnotatedClass implements Converter<AnnotatedClass> {
    @Override
    public String toString(AnnotatedClass value) {
      return null;
    }
    @Override
    public AnnotatedClass fromString(String representation) {
      return null;
    }
  }

  @Test
  public void createInstantiatorWithLiteralTypeNotHavingDefault() throws Exception {
    checkErrorCase(
        OptionalLiteralParameterWithoutDefault.class,
        optionalLiteralParameterMustHaveDefault(new Errors(), 0));
  }

  @Test
  public void createInstantiatorWithIllegalConstructor() throws Exception {
    checkErrorCase(
        HasIllegalConstructor.class,
        illegalConstructor(new Errors(), HasIllegalConstructor.class, null));
  }

  static class HasIllegalConstructor {
    HasIllegalConstructor() {
      for (int i = 0; i < 10; i++) {
        System.out.println(i);
      }
    }
  }

  @Test
  public void doesNotKnowHowToConvert() throws Exception {
    checkErrorCase(
        DoesNotKnowHowToConvert.class,
        noConverterForType(new Errors(), new TypeLiteral<Map<String, String>>() {}.getType()));
  }

  static class DoesNotKnowHowToConvert {
    DoesNotKnowHowToConvert(Map<String, String> names) {
    }
  }

  @Test
  public void duplicateConverterBinding1() throws Exception {
    Errors expected = new Errors();
    duplicateConverterBindingForType(expected, new TypeLiteral<String>() {}.getType());
    moreThanOneConstructor(expected, String.class);

    checkErrorCase(
        String.class,
        expected,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(String.class).converter(C_STRING);
            registerFor(String.class).converter(C_STRING);
          }
        });
  }

  @Test
  public void duplicateConverterBinding2() throws Exception {
    Errors expected = new Errors();
    duplicateConverterBindingForType(expected, new TypeLiteral<String>() {}.getType());
    moreThanOneConstructor(expected, String.class);

    checkErrorCase(
        String.class,
        expected,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(String.class).converter(C_STRING.getClass());
            registerFor(String.class).converter(C_STRING.getClass());
          }
        });
  }

  @Test
  public void duplicateConverterBinding3() throws Exception {
    Errors expected = new Errors();
    duplicateConverterBindingForType(expected, new TypeLiteral<String>() {}.getType());
    moreThanOneConstructor(expected, String.class);

    checkErrorCase(
        String.class,
        expected,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(String.class).converter(C_STRING);
            registerFor(String.class).converter(C_STRING.getClass());
          }
        });
  }

  static class DefaultValueAndDefaultConstant {
    DefaultValueAndDefaultConstant(@Optional(value = "4", constant = "F") int a) {
    }
  }

  @Test
  public void defaultValueAndDefaultConstant() throws Exception {
    checkErrorCase(
        DefaultValueAndDefaultConstant.class,
        cannotSpecifyDefaultValueAndConstant(
            new Errors(),
            (Optional) DefaultValueAndDefaultConstant.class.getDeclaredConstructor(int.class).getParameterAnnotations()[0][0]));
  }

  static class UnresolvableLocalConstant {
    UnresolvableLocalConstant(@Optional(constant = "F") int a) {
    }
  }

  @Test
  public void unresolvableLocalConstant() throws Exception {
    checkErrorCase(
        UnresolvableLocalConstant.class,
        unableToResolveConstant(new Errors(), UnresolvableLocalConstant.class, "F"));
  }

  static class UnresolvableFullyQualifiedConstant {
    UnresolvableFullyQualifiedConstant(@Optional(constant = "A#F") int a) {
    }
  }

  @Test
  public void unresolvableFullyQualifiedConstant() throws Exception {
    checkErrorCase(
        UnresolvableFullyQualifiedConstant.class,
        unableToResolveFullyQualifiedConstant(new Errors(), "A#F"));
  }

  static class ConstantIsNotStaticFinal {
    int NOT_STATIC_FINAL;
    ConstantIsNotStaticFinal(@Optional(constant = "NOT_STATIC_FINAL") int a) {
    }
  }

  @Test
  public void constantIsNotStaticFinal() throws Exception {
    checkErrorCase(
        ConstantIsNotStaticFinal.class,
        InstantiatorErrors.constantIsNotStaticFinal(
            new Errors(), ConstantIsNotStaticFinal.class, "NOT_STATIC_FINAL"));
  }

  static class ConstantIsOfAnIncompatibleType {
    static final String WRONG_TYPE = "noononono";
    ConstantIsOfAnIncompatibleType(@Optional(constant = "WRONG_TYPE") int a) {
    }
  }

  @Test
  public void constantIsOfAnIncompatibleType() throws Exception {
    checkErrorCase(
        ConstantIsOfAnIncompatibleType.class,
        constantHasIncompatibleType(new Errors(), ConstantIsOfAnIncompatibleType.class, "WRONG_TYPE"));
  }

  static class AtOptionalOnOption {
    AtOptionalOnOption(@Optional Option<Integer> foo) {
    }
  }

  @Test
  public void atOptionalOnOption() throws Exception {
    checkErrorCase(
        AtOptionalOnOption.class,
        cannotAnnotateOptionWithOptional(new Errors(), new TypeLiteral<Option<Integer>>() {}.getType()));
  }

  private <T> void checkErrorCase(Class<T> klass, Errors expected, InstantiatorModule... modules) {
    InstantiatorImplFactory<T> f = InstantiatorImplFactory.createFactory(actualErrors, klass);
    for (InstantiatorModule m : modules) {
      m.configure(f.binder());
    }
    assertTrue(f.build().isEmpty());
    assertEquals(expected, actualErrors);
  }

  @Test
  public void retrieveFieldsFromAssignment1() {
    Field[] fields = createFactory(actualErrors, HasStringConstructor.class)
        .retrieveFieldsFromAssignment(
            1,
            ImmutableMap.of(
                "representation", new FormalParameter(0, null)));
    assertEquals(1, fields.length);
    assertEquals("representation", fields[0].getName());
    assertTrue(fields[0].isAccessible());
  }

  @Test
  public void retrieveFieldsFromAssignment2() {
    InstantiatorImplFactory<HasStringConstructor> f = createFactory(actualErrors, HasStringConstructor.class);
    f.retrieveFieldsFromAssignment(
        1,
        ImmutableMap.of(
            "thisfielddoesnotexist", new FormalParameter(0, null)));
    assertEquals(
        noSuchField(new Errors(), "thisfielddoesnotexist"),
        f.getErrors());
  }

  @Test(expected = IllegalStateException.class)
  public void retrieveFieldsFromAssignment3() {
    createFactory(actualErrors, HasStringConstructor.class).retrieveFieldsFromAssignment(
        1,
        ImmutableMap.of(
            "representation", new FormalParameter(1, null))); // wrong index
  }

  @Test
  public void retrieveFieldsFromAssignment4FieldIsInSuperclass() {
    Field[] fields = createFactory(actualErrors, FieldIsInSuperSuperclass.class)
        .retrieveFieldsFromAssignment(
            1,
            ImmutableMap.of(
                "theFieldYouAreLookingFor", new FormalParameter(0, null)));
    assertEquals(1, fields.length);
    assertEquals("theFieldYouAreLookingFor", fields[0].getName());
    assertTrue(fields[0].isAccessible());
  }

  static class FieldIsHere { String theFieldYouAreLookingFor; }
  static class FieldIsInSuperclass extends FieldIsHere {}
  static class FieldIsInSuperSuperclass extends FieldIsInSuperclass {}

  static class OptionalLiteralParameterWithoutDefault {
    OptionalLiteralParameterWithoutDefault(@Optional short s) {}
  }

  @Test
  public void getPublicConstructor() throws Exception {
    createFactory(actualErrors, A.class).getConstructor();
  }

  @Test
  public void getProtectedConstructor() throws Exception {
    createFactory(actualErrors, B.class).getConstructor();
  }

  @Test
  public void getDefaultVisibleConstructor() throws Exception {
    createFactory(actualErrors, C.class).getConstructor();
  }

  @Test
  public void getPrivateConstructor() throws Exception {
    createFactory(actualErrors, D.class).getConstructor();
  }

  @Test
  public void getNonExistingConstructor() throws Exception {
    assertTrue(createFactory(actualErrors, E.class).getConstructor().isEmpty());
    assertEquals(
        noConstructorFound(new Errors(), E.class),
        actualErrors);
  }

  @Test
  public void getNonUniqueConstructor() throws Exception {
    assertTrue(createFactory(actualErrors, F.class).getConstructor().isEmpty());
    assertEquals(
        moreThanOneConstructor(new Errors(), F.class),
        actualErrors);
  }

  @Test
  public void getConstructorFromSuperclass() throws Exception {
    createFactory(actualErrors, G.class).getConstructor();
  }

  @Test
  public void getConstructorFromSuperclassWithMultipleConstructors() {
    createFactory(actualErrors, H.class).getConstructor();
  }

  @Test
  public void getNonUniqueConstructorWithAnnotation1() throws Exception {
    assertNotNull(createFactory(actualErrors, P.class).getConstructor());
  }

  @Test
  public void getNonUniqueConstructorWithAnnotation2() throws Exception {
    InstantiatorImplFactory<Q> factory = createFactory(actualErrors, Q.class);
    factory.getConstructor();
    assertEquals(
        moreThanOneConstructorWithInstantiate(new Errors(), Q.class),
        factory.getErrors());
  }

  @Test
  public void createConverter_int() {
    testConverter(int.class, "3", 3, "3");
  }

  @Test
  public void createConverter_double() {
    testConverter(double.class, "3", 3, "3");
  }

  @Test
  public void createConverter_listOfIntegers() {
    testConverter(new TypeLiteral<List<Integer>>() {}.getType(), "3", newArrayList(3), "3");
  }

  @SuppressWarnings("unchecked")
  private void testConverter(Type type, String actual,
      Object expectedFromString, String expectedToString) {
    Option<? extends Converter<?>> maybeConverter = createFactory(actualErrors, null).createConverter(type);
    assertTrue(format("no converter for type %s", type), maybeConverter.isDefined());
    Converter converter = maybeConverter.getOrThrow();
    assertEquals(expectedFromString, converter.fromString(actual));
    assertEquals(expectedToString, converter.toString(expectedFromString));
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
