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

import static java.lang.String.format;

import java.lang.reflect.Type;

import com.kaching.platform.common.Errors;

/**
 * Object helping with capturing and propagating errors.
 */
class InstantiatorErrors {

  @SuppressWarnings("rawtypes")
  static Errors incorrectBoundForConverter(
      Errors errors,
      Type targetType,
      Class<? extends Converter> converterClass,
      Type producedType) {
    return errors.addMessage(
        "the converter %2$s, mentioned on %1$s using @%4$s, does not produce " +
        "instances of %1$s. It produces %3$s.",
        targetType,
        converterClass,
        producedType,
        ConvertedBy.class.getSimpleName());
  }

  static Errors moreThanOneConstructor(Errors errors, Class<?> klass) {
    return errors.addMessage(
        "%s has more than one constructors",
        klass);
  }

  static Errors noConstructorFound(Errors errors, Class<?> klass) {
    return errors.addMessage(
        "No constructor found in %s",
        klass);
  }

  static Errors moreThanOneConstructorWithInstantiate(Errors errors, Class<?> klass) {
    return errors.addMessage(
        "%s has more than one constructor annotated with @%s",
        klass,
        Instantiate.class.getSimpleName());
  }

  static Errors unableToInstantiate(Errors errors, Class<?> klass, Exception e) {
    return errors.addMessage(
        "unable to instantiate %s due to %s",
        klass,
        e.getCause());
  }

  static Errors unableToInstantiate(Errors errors, Class<?> klass, IllegalAccessException e) {
    return errors.addMessage(
        "unable to instantiate %s because of lack of access to the definition of the constructor",
        klass);
  }

  static Errors unableToGetField(Errors errors, String fieldName, SecurityException e) {
    return errors.addMessage(
        "unable to get field %s due to security violation",
        fieldName);
  }

  static Errors noSuchField(Errors errors, String fieldName) {
    return errors.addMessage(
        "no such field %s",
        fieldName);
  }

  static Errors enumHasAmbiguousNames(Errors errors, Class<? extends Enum<?>> clazz) {
    return errors.addMessage(
        "enum %s has ambiguous names",
        clazz.getName());
  }

  static Errors moreThanOneMatchingFunction(Errors errors, Type type) {
    return errors.addMessage(
        "%s has more than one matching function",
        type);
  }

  static Errors noConverterForType(Errors errors, Type type) {
    return errors.addMessage(
        "no converter for %s",
        type);
  }

  static Errors duplicateConverterBindingForType(Errors errors, Type type) {
    return errors.addMessage(
        "duplicate converter binding for %s",
        type);
  }

  static Errors incorrectDefaultValue(Errors errors, String value, RuntimeException e) {
    return errors.addMessage(
        "%s: For default value \"%s\"",
        e.getClass().getName(), value);
  }

  static Errors optionalLiteralParameterMustHaveDefault(Errors errors, int parameterNum) {
    return errors.addMessage(
        "parameter %s: opetional literal parameters must have a default value",
        parameterNum + 1);
  }

  static Errors illegalConstructor(Errors errors, Class<?> klass, String message) {
    return errors.addMessage(
        "%s has an illegal constructor%s",
        klass, message == null ? "" : ": " + message);
  }

  static Errors cannotAnnotateOptionWithOptional(Errors errors, Type genericParameterType) {
    return errors.addMessage(
        "cannot annotate %s with @Optional",
        genericParameterType.toString());
  }

  static Errors cannotSpecifyDefaultValueAndConstant(Errors errors, Optional annotation) {
    return errors.addMessage(
        "cannot specify both a default constant and a default value %s",
        annotation.toString().replaceFirst(Optional.class.getName(), Optional.class.getSimpleName()));
  }

  static Errors unableToResolveConstant(Errors errors, Class<?> container, String constant) {
    return unableToResolveFullyQualifiedConstant(
        errors, localConstantQualifier(container, constant));
  }

  static Errors unableToResolveFullyQualifiedConstant(Errors errors, String constant) {
    return errors.addMessage(
        "unable to resolve constant %s", constant);
  }

  static Errors constantIsNotStaticFinal(Errors errors, Class<?> container, String constant) {
    return errors.addMessage("constant %s is not static final",
        localConstantQualifier(container, constant));
  }

  static Errors constantHasIncompatibleType(Errors errors, Class<?> container, String constant) {
    return errors.addMessage("constant %s of incompatible type",
        localConstantQualifier(container, constant));
  }

  private static String localConstantQualifier(Class<?> container, String constant) {
    return format("%s#%s", container.getName(), constant);
  }

}
