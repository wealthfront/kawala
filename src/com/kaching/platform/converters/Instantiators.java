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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

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
    Type[] genericParameterTypes = constructor.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
    int parametersCount = genericParameterTypes.length;
    Converter<?>[] converters =
        parametersCount == 0 ? null : new Converter<?>[parametersCount];
    for (int i = 0; i < parametersCount; i++) {
      converters[i] = createConverter(
          genericParameterTypes[i], parameterAnnotations[i]);
    }
    // 3. done
    return new InstantiatorImpl<T>(constructor, converters);
  }
  
  @VisibleForTesting
  @SuppressWarnings("unchecked")
  static Converter<?> createConverter(
      Type type, Annotation[] annotations) {
    try {
      // 1. explicit binding
      if (type instanceof Class) {
        // 2. @ConvertedBy
        Annotation[] typeAnnotations = ((Class) type).getAnnotations();
        // need to throw if two @ConvertedBy!!!
        for (Annotation typeAnnotation : typeAnnotations) {
          if (typeAnnotation instanceof ConvertedBy) {
            return ((ConvertedBy) typeAnnotation).value().newInstance();
          }
        }
        // 3. has <init>(Ljava/lang/String;)V;
        Constructor stringConstructor = ((Class) type).getConstructor(String.class);
        stringConstructor.setAccessible(true);
        return null;//new ConstructorAndToStringConverter<?>(stringConstructor);
      }
      return null; // TODO(pascal): should accumulate error (i.e. binding error,
      // cannot create converter for type XYZ)
    } catch (InstantiationException e) {
      // proper error handling
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      // proper error handling
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      // proper error handling
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      // proper error handling
      throw new RuntimeException(e);
    }
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
