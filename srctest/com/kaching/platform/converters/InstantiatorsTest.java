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
package com.kaching.platform.converters;

import static com.kaching.platform.converters.Instantiators.createConverter;
import static com.kaching.platform.converters.Instantiators.createInstantiator;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.TypeLiteral;

public class InstantiatorsTest {

  static class ConstructMe1 {}

  @Test
  public void constructMe1() {
    assertNotNull(Instantiators
        .createInstantiator(ConstructMe1.class)
        .newInstance());
  }

  static class ConstructMe2 {
    private final String name;
    ConstructMe2(String name) {
      this.name = name;
    }
  }

  @Test
  public void constructMe2() {
    ConstructMe2 instance = Instantiators
        .createInstantiator(ConstructMe2.class)
        .newInstance("Jack Bauer");
    assertNotNull(instance);
    assertEquals("Jack Bauer", instance.name);
  }

  @Test
  public void constructMe2ByName() {
    ConstructMe2 instance = Instantiators
        .createInstantiator(ConstructMe2.class)
        .newInstance(ImmutableMap.of("name", "Jack Bauer"));
    assertNotNull(instance);
    assertEquals("Jack Bauer", instance.name);
  }

  static class ConstructMe3 {
    private final WrappedString name;
    private final ConvertedPair pair;
    ConstructMe3(WrappedString name, ConvertedPair pair) {
      this.name = name;
      this.pair = pair;
    }
  }

  @Test
  public void constructMe3() {
    Instantiator<ConstructMe3> instantiator = Instantiators
        .createInstantiator(ConstructMe3.class);
    ConstructMe3 instance = instantiator
        .newInstance("Jack Bauer", "First:Last");
    assertNotNull(instance);
    assertEquals("Jack Bauer", instance.name.content);
    assertEquals("First", instance.pair.first);
    assertEquals("Last", instance.pair.last);
    assertEquals(
        asList("Jack Bauer", "First:Last"),
        instantiator.fromInstance(instance));
  }

  @Test
  public void constructMe3ByName() {
    Instantiator<ConstructMe3> instantiator = Instantiators
        .createInstantiator(ConstructMe3.class);
    ConstructMe3 instance = instantiator
        .newInstance(ImmutableMap.of(
            "name", "Jack Bauer",
            "pair", "First:Last"));
    assertNotNull(instance);
    assertEquals("Jack Bauer", instance.name.content);
    assertEquals("First", instance.pair.first);
    assertEquals("Last", instance.pair.last);
  }

  static class ConstructMe4Optionality {
    private final String name;
    ConstructMe4Optionality(@Optional String name) {
      this.name = name;
    }
  }

  @Test
  public void constructMe4() {
    ConstructMe4Optionality instance = Instantiators
        .createInstantiator(ConstructMe4Optionality.class)
        .newInstance((String) null);
    assertNotNull(instance);
    assertNull(instance.name);
  }

  static class ConstructMe5OptionalityWithDefaultValue {
    private final Integer number;
    ConstructMe5OptionalityWithDefaultValue(
        @Optional("90") Integer number) {
      this.number = number;
    }
  }

  @Test
  public void constructMe5() {
    ConstructMe5OptionalityWithDefaultValue instance = Instantiators
        .createInstantiator(ConstructMe5OptionalityWithDefaultValue.class)
        .newInstance((String) null);
    assertNotNull(instance);
    assertEquals(90, instance.number);
  }

  static class ArgumentAreNotSavedToFields {
    private final int is;
    private final int isToo;
    ArgumentAreNotSavedToFields(int is, int isNot, int isToo) {
      this.is = is;
      this.isToo = isToo;
    }
  }

  @Test
  public void argumentAreNotSavedToFields() {
    Instantiator<ArgumentAreNotSavedToFields> instantiator = Instantiators
        .createInstantiator(ArgumentAreNotSavedToFields.class);

    ArgumentAreNotSavedToFields instance = instantiator
        .newInstance("1", "2", "3");
    assertNotNull(instance);
    assertEquals(1, instance.is);
    assertEquals(3, instance.isToo);

    assertEquals(
        asList("1", null, "3"),
        instantiator.fromInstance(instance));
  }

  static class ObjectWithMapOfIntToString {
    ObjectWithMapOfIntToString(Map<Integer, String> numbers) {
    }
  }

  @Test(expected = RuntimeException.class)
  public void objectWithMapOfIntToStringNoSpecificBindingWillFail() {
    Instantiators.createInstantiator(ObjectWithMapOfIntToString.class);
  }

  static class ObjectWithListOfInt {
    final List<Integer> numbers;
    ObjectWithListOfInt(List<Integer> numbers) {
      this.numbers = numbers;
    }
  }

  @Test
  public void objectWithListOfIntUsingInstances() {
    Instantiator<ObjectWithListOfInt> instantiator = createInstantiator(
        ObjectWithListOfInt.class,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(new TypeLiteral<List<Integer>>() {})
                .converter(new ListOfIntConverter());
          }
        });

    checkObjectWithListOfInt(instantiator);
  }

  @Test
  public void objectWithListOfIntUsingInstancesAndTypeScheme() {
    Instantiator<ObjectWithListOfInt> instantiator = createInstantiator(
        ObjectWithListOfInt.class,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(new TypeLiteral<List<? extends Integer>>() {})
                .converter(new ListOfIntConverter());
          }
        });

    checkObjectWithListOfInt(instantiator);
  }

  @Test
  public void objectWithListOfIntUsingBindings() {
    Instantiator<ObjectWithListOfInt> instantiator = createInstantiator(
        ObjectWithListOfInt.class,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(new TypeLiteral<List<Integer>>() {})
                .converter(ListOfIntConverter.class);
          }
        });

    checkObjectWithListOfInt(instantiator);
  }

  @Test
  public void objectWithListOfIntUsingBindingsAndTypeScheme() {
    Instantiator<ObjectWithListOfInt> instantiator = createInstantiator(
        ObjectWithListOfInt.class,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(new TypeLiteral<List<? extends Integer>>() {})
                .converter(ListOfIntConverter.class);
          }
        });

    checkObjectWithListOfInt(instantiator);
  }

  private void checkObjectWithListOfInt(
      Instantiator<ObjectWithListOfInt> instantiator) {
    ObjectWithListOfInt instance = instantiator.newInstance("1|2|3");
    assertEquals(asList(1, 2, 3), instance.numbers);

    assertEquals(
        asList("1|2|3"),
        instantiator.fromInstance(instance));
  }

  static class ObjectWithListOfIntAndListOfBoolean {
    final List<Integer> numbers;
    final List<Boolean> booleans;
    ObjectWithListOfIntAndListOfBoolean(
        List<Integer> numbers, List<Boolean> booleans) {
      this.numbers = numbers;
      this.booleans = booleans;
    }
  }

  @Test
  public void objectWithListOfIntAndListOfBooleanViaBindings() {
    Instantiator<ObjectWithListOfIntAndListOfBoolean> instantiator = createInstantiator(
        ObjectWithListOfIntAndListOfBoolean.class,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(new TypeLiteral<List<Integer>>() {})
                .converter(ListOfIntConverter.class);
            registerFor(new TypeLiteral<List<Boolean>>() {})
                .converter(ListOfBooleanConverter.class);
          }
        });

    checkObjectWithListOfIntAndListOfBoolean(instantiator);
  }

  @Test
  public void objectWithListOfIntAndListOfBooleanViaInstances() {
    Instantiator<ObjectWithListOfIntAndListOfBoolean> instantiator = createInstantiator(
        ObjectWithListOfIntAndListOfBoolean.class,
        new AbstractInstantiatorModule() {
          @Override
          protected void configure() {
            registerFor(new TypeLiteral<List<Integer>>() {})
                .converter(new ListOfIntConverter());
            registerFor(new TypeLiteral<List<Boolean>>() {})
                .converter(new ListOfBooleanConverter());
          }
        });

    checkObjectWithListOfIntAndListOfBoolean(instantiator);
  }

  private void checkObjectWithListOfIntAndListOfBoolean(
      Instantiator<ObjectWithListOfIntAndListOfBoolean> instantiator) {
    ObjectWithListOfIntAndListOfBoolean instance = instantiator.newInstance(
        "1|2|3", "true|false|true");
    assertEquals(asList(1, 2, 3), instance.numbers);
    assertEquals(asList(true, false, true), instance.booleans);

    assertEquals(
        asList("1|2|3", "true|false|true"),
        instantiator.fromInstance(instance));
  }

  static class LocalConstant {
    static final String MY_CONSTANT = "this text is long and great for a test";
    final String message;
    LocalConstant(@Optional(constant = "MY_CONSTANT") String message) {
      this.message = message;
    }
  }

  @Test
  public void localConstant() {
    Instantiator<LocalConstant> instantiator = Instantiators
        .createInstantiator(LocalConstant.class);

    LocalConstant instance = instantiator
        .newInstance((String) null);
    assertNotNull(instance);
    assertEquals(LocalConstant.MY_CONSTANT, instance.message);
  }

  static class FullyQualifiedConstant {
    final String message;
    FullyQualifiedConstant(@Optional(constant = "com.kaching.platform.converters.InstantiatorsTest$LocalConstant#MY_CONSTANT") String message) {
      this.message = message;
    }
  }

  @Test
  public void fullyQualifiedConstant() {
    Instantiator<FullyQualifiedConstant> instantiator = Instantiators
        .createInstantiator(FullyQualifiedConstant.class);

    FullyQualifiedConstant instance = instantiator
        .newInstance((String) null);
    assertNotNull(instance);
    assertEquals(LocalConstant.MY_CONSTANT, instance.message);
  }

  @Test
  public void createUriConverter() throws URISyntaxException {
    assertEquals(
        new URI("www.kaching.com"),
        createConverter(URI.class).fromString("www.kaching.com"));
  }

  @Test
  public void createConverterPairConverter() throws URISyntaxException {
    Converter<ConvertedPair> converter = createConverter(ConvertedPair.class);
    assertEquals(
        "1:2",
        converter.toString(converter.fromString("1:2")));
  }

  static class WrappedString {
    private final String content;
    WrappedString(String content) {
      this.content = content;
    }
    @Override
    public String toString() {
      return content;
    }
  }

  @ConvertedBy(ConvertedPairConverter.class)
  static class ConvertedPair {
    private final String first;
    private final String last;
    ConvertedPair(String first, String last) {
      this.first = first;
      this.last = last;
    }
  }

  static class ConvertedPairConverter implements Converter<ConvertedPair> {

    @Override
    public String toString(ConvertedPair value) {
      return format("%s:%s", value.first, value.last);
    }

    @Override
    public ConvertedPair fromString(String representation) {
      String[] parts = representation.split(":");
      return new ConvertedPair(parts[0], parts[1]);
    }

  }

  abstract static class CsvValuesListConverter<T> implements Converter<List<T>> {

    private final Converter<T> elementConverter;

    CsvValuesListConverter(Converter<T> elementConverter) {
      this.elementConverter = elementConverter;
    }

    @Override
    public String toString(List<T> value) {
      // NOTE using element.toString instead of elementConverter.toString(element)
      // which is equivalent in the context of this text but certainly not for
      // production code.
      return Joiner.on("|").join(value);
    }

    @Override
    public List<T> fromString(String representation) {
      ArrayList<T> fromString = Lists.newArrayList();
      for (String single : representation.split("\\|")) {
        fromString.add(elementConverter.fromString(single));
      }
      return fromString;
    }

  }

  static class ListOfIntConverter extends CsvValuesListConverter<Integer> {
    ListOfIntConverter() { super(NativeConverters.C_INT); }
  }

  static class ListOfBooleanConverter extends CsvValuesListConverter<Boolean> {
    ListOfBooleanConverter() { super(NativeConverters.C_BOOLEAN); }
  }

}
