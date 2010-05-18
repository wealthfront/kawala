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

import java.lang.reflect.Constructor;

import com.google.common.annotations.VisibleForTesting;

public class Instantiators {

  /* The Instantiators class is the entry point into the library and is not
   * meant to be instantiated.
   */
  private Instantiators() {}
  
  public static <T> Instantiator<T> createInstantiator(Class<T> klass) {
    // 1. find constructor
    Constructor<T> constructor = getConstructor(klass);
    constructor.setAccessible(true);
    // 2. for each parameter, find converter
    int parametersCount = constructor.getParameterTypes().length;
    Converter<?>[] converters =
        parametersCount == 0 ? null : new Converter<?>[parametersCount];
    for (int i = 0; i < parametersCount; i++) {
      converters[i] = null; // do something smart here
    }
    // 3. done
    return new InstantiatorImpl<T>(constructor, converters);
  }
  
  @VisibleForTesting
  static <T> Constructor<T> getConstructor(Class<T> clazz) {
    @SuppressWarnings("unchecked")
    Constructor<T>[] constructors =
        (Constructor<T>[]) clazz.getDeclaredConstructors();
    if (constructors.length > 1) {
      Constructor<T> convertableConstructor = null;
      for (Constructor<T> constructor : constructors) {
        if (constructor.getAnnotation(Instantiate.class) != null) {
          if (convertableConstructor == null) {
            convertableConstructor = constructor;
          } else {
            throw new IllegalArgumentException(clazz.toString()
                + " has more than one constructors annotated with @"
                + Instantiate.class.getSimpleName());
          }
        }
      }
      if (convertableConstructor != null) {
        return convertableConstructor;
      } else {
        throw new IllegalArgumentException(clazz.toString() +
            " has more than one constructors");
      }
    } else if (constructors.length == 0) {
      throw new IllegalArgumentException("No constructor found in " + clazz);
    } else {
      return constructors[0];
    }
  }

}
