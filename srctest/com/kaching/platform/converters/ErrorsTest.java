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

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.google.inject.TypeLiteral;
import com.kaching.platform.testing.EquivalenceTester;

public class ErrorsTest {

  @Test
  public void noErrorsShouldNotThrow() {
    new Errors().throwIfHasErrors();
  }

  @Test
  public void incorrectBoundForConverter() {
    check(
        "the converter interface com.kaching.platform.converters.Converter, " +
        "mentioned on class java.lang.String using @ConvertedBy, " +
        "does not produce instances of class java.lang.String. It produces " +
        "class java.lang.Integer.",
        new Errors().incorrectBoundForConverter(String.class, Converter.class, Integer.class));
  }

  @Test
  public void incorrectDefaultValue() {
    check(
        "java.lang.NumberFormatException: For default value \"90z\"",
        new Errors().incorrectDefaultValue("90z", new NumberFormatException("the message")));
  }

  @Test
  public void illegalConstructor1() {
    check(
        "class java.lang.String has an illegal constructor: hello",
        new Errors().illegalConstructor(String.class, "hello"));
  }

  @Test
  public void illegalConstructor2() {
    check(
        "class java.lang.String has an illegal constructor",
        new Errors().illegalConstructor(String.class, null));
  }

  @Test
  public void enumHasAmbiguousNames() {
    check(
        "enum com.kaching.platform.converters.ErrorsTest$AmbiguousEnum has ambiguous names",
        new Errors().enumHasAmbiguousNames(AmbiguousEnum.class));
  }

  enum AmbiguousEnum {
  }

  @Test
  public void noConverterForType() {
    check(
        "no converter for java.util.List<java.lang.String>",
        new Errors().noConverterForType(new TypeLiteral<List<String>>() {}.getType()));
  }

  @Test
  public void addinTwiceTheSameMessageDoesNotDuplicateTheError() {
    check(
        "no such field a",
        new Errors().noSuchField("a").noSuchField("a"));
  }

  @Test
  public void cannotSpecifyDefaultValueAndConstant() throws Exception {
    check(
        "cannot specify both a default constant and a default value " +
        "@Optional(constant=FOO, value=4)",
        new Errors().cannotSpecifyDefaultValueAndConstant(inspectMeCannotSpecifyDefaultValueAndConstant(8)));
  }

  Optional inspectMeCannotSpecifyDefaultValueAndConstant(
      @Optional(value = "4", constant = "FOO") int i)
      throws Exception {
    return (Optional) this.getClass()
        .getDeclaredMethod("inspectMeCannotSpecifyDefaultValueAndConstant", int.class)
        .getParameterAnnotations()[0][0];
  }

  @Test
  public void unableToResolveLocalConstant() throws Exception {
    check(
        "unable to resolve constant com.kaching.platform.converters.Errors#MY_CONSTANT",
        new Errors().unableToResolveConstant(Errors.class, "MY_CONSTANT"));
  }

  private void check(String expected, Errors errors) {
    try {
      errors.throwIfHasErrors();
      fail();
    } catch (RuntimeException e) {
      assertEquals("1) " + expected, e.getMessage());
    }
  }

  @Test
  public void equivalence() {
    EquivalenceTester.check(
        newArrayList(
            new Errors().incorrectBoundForConverter(String.class, Converter.class, Integer.class),
            new Errors().incorrectBoundForConverter(String.class, Converter.class, Integer.class)));
  }

}
