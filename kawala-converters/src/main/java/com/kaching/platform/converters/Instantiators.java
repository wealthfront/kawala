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

import java.lang.reflect.Type;

import com.google.inject.TypeLiteral;
import com.kaching.platform.common.Errors;
import com.kaching.platform.common.Option;

public class Instantiators {

  /* The Instantiators class is the entry point into the library and is not
   * meant to be instantiated.
   */
  private Instantiators() {}

  /**
   * Creates an instantiator for {@code klass}.
   */
  public static <T> Instantiator<T> createInstantiator(
      Class<T> klass, InstantiatorModule... modules) {
    Errors errors = new Errors();
    for (Instantiator<T> instantiator : createInstantiator(errors, klass, modules)) {
      return instantiator;
    }
    errors.throwIfHasErrors();

    // The following program should not be reachable since the factory should
    // produce errors if it is unable to create an instantiator.
    throw new IllegalStateException();
  }

  /**
   * Creates an instantiator for {@code klass} if possible and aggregates errors.
   * This factory method is mostly useful when instantiators are used as a piece
   * in larger framework and allows errors aggregation to be done hollisticly.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> Option<Instantiator<T>> createInstantiator(
      Errors errors, Class<T> klass, InstantiatorModule... modules) {
    // we do not want to expose the covariant option
    return (Option) factoryFor(errors, klass, modules).build();
  }

  /**
   * Creates a converter for {@code klass}.
   */
  public static <T> Converter<T> createConverter(
      Class<T> klass, InstantiatorModule... modules) {
    return createConverterForType(klass, modules);
  }

  /**
   * Creates a converter for {@code typeLiteral}.
   */
  public static <T> Converter<T> createConverter(
      TypeLiteral<T> typeLiteral, InstantiatorModule... modules) {
    return createConverterForType(typeLiteral.getType(), modules);
  }

  private static <T> InstantiatorImplFactory<T> factoryFor(
      Errors errors, Class<T> klass,
      InstantiatorModule... modules) {
    InstantiatorImplFactory<T> factory = InstantiatorImplFactory
            .createFactory(errors, klass);
    for (InstantiatorModule c : modules) {
      c.configure(factory.binder());
    }
    return factory;
  }

  @SuppressWarnings("unchecked")
  private static <T> Converter<T> createConverterForType(Type type,
      InstantiatorModule... modules) {
    return (Converter<T>) factoryFor(new Errors(), null, modules).createConverter(type).getOrThrow();
  }

}
