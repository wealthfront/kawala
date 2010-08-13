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

import java.lang.reflect.Type;

import com.google.inject.TypeLiteral;

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
    return factoryFor(klass, modules).build();
  }

  /**
   * Creates a converter {@code klass}.
   */
  public static <T> Converter<T> createConverter(
      Class<T> klass, InstantiatorModule... modules) {
    return createConverterForType(klass, modules);
  }

  /**
   * Creates a converter {@code typeLiteral}.
   */
  public static <T> Converter<T> createConverter(
      TypeLiteral<T> typeLiteral, InstantiatorModule... modules) {
    return createConverterForType(typeLiteral.getType(), modules);
  }

  private static <T> InstantiatorImplFactory<T> factoryFor(Class<T> klass,
      InstantiatorModule... modules) {
    InstantiatorImplFactory<T> factory = InstantiatorImplFactory
            .createFactory(klass);
    for (InstantiatorModule c : modules) {
      c.configure(factory.binder());
    }
    return factory;
  }

  @SuppressWarnings("unchecked")
  private static <T> Converter<T> createConverterForType(Type type,
      InstantiatorModule... modules) {
    return (Converter<T>) factoryFor(null, modules).createConverter(type).getOrThrow();
  }

}
