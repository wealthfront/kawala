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

import static com.kaching.platform.converters.InstantiatorErrors.noSuchField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.google.inject.TypeLiteral;
import com.kaching.platform.common.Errors;

public class InstantiatorErrorsTest {

  @Test
  public void incorrectBoundForConverter() {
    check(
        "the converter interface com.kaching.platform.converters.Converter, " +
        "mentioned on class java.lang.String using @ConvertedBy, " +
        "does not produce instances of class java.lang.String. It produces " +
        "class java.lang.Integer.",
        InstantiatorErrors.incorrectBoundForConverter(
            new Errors(), String.class, Converter.class, Integer.class));
  }

  @Test
  public void incorrectDefaultValue() {
    check(
        "java.lang.NumberFormatException: For default value \"90z\"",
        InstantiatorErrors.incorrectDefaultValue(
            new Errors(), "90z", new NumberFormatException("the message")));
  }

  @Test
  public void illegalConstructor1() {
    check(
        "class java.lang.String has an illegal constructor: hello",
        InstantiatorErrors.illegalConstructor(
            new Errors(), String.class, "hello"));
  }

  @Test
  public void illegalConstructor2() {
    check(
        "class java.lang.String has an illegal constructor",
        InstantiatorErrors.illegalConstructor(
            new Errors(), String.class, null));
  }

  @Test
  public void enumHasAmbiguousNames() {
    check(
        "enum com.kaching.platform.converters.InstantiatorErrorsTest$AmbiguousEnum has ambiguous names",
        InstantiatorErrors.enumHasAmbiguousNames(
            new Errors(), AmbiguousEnum.class));
  }

  enum AmbiguousEnum {
  }

  @Test
  public void moreThanOneMatchingFunction() {
    check(
        "class com.kaching.platform.converters.InstantiatorErrorsTest$AmbiguousEnum has more than one matching function",
        InstantiatorErrors.moreThanOneMatchingFunction(
            new Errors(), AmbiguousEnum.class));
  }

  @Test
  public void noConverterForType() {
    check(
        "no converter for java.util.List<java.lang.String>",
        InstantiatorErrors.noConverterForType(
            new Errors(), new TypeLiteral<List<String>>() {}.getType()));
  }

  @Test
  public void addinTwiceTheSameMessageDoesNotDuplicateTheError() {
    check(
        "no such field a",
        noSuchField(noSuchField(new Errors(), "a"), "a"));
  }

  @Test
  public void cannotSpecifyDefaultValueAndConstant() throws Exception {
    check(
        "cannot specify both a default constant and a default value " +
        "@Optional(constant=FOO, value=4)",
        InstantiatorErrors.cannotSpecifyDefaultValueAndConstant(
            new Errors(), inspectMeCannotSpecifyDefaultValueAndConstant(8)));
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
        "unable to resolve constant com.kaching.platform.converters.InstantiatorErrorsTest#MY_CONSTANT",
        InstantiatorErrors.unableToResolveConstant(
            new Errors(), InstantiatorErrorsTest.class, "MY_CONSTANT"));
  }

  private void check(String expected, Errors errors) {
    try {
      errors.throwIfHasErrors();
      fail();
    } catch (RuntimeException e) {
      assertEquals("1) " + expected, e.getMessage());
    }
  }

}
