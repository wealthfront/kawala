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

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Converter for objects having a constructor taking a {@link String}. This
 * converter creates new values ({@link Converter#fromString(String)}) by
 * calling the constructor and produces textual representation by calling
 * {@link #toString()}.
 */
class ConstructorAndToStringConverter<T> implements Converter<T> {

  private final Constructor<?> constructor;

  /* Here, T should be equal to the type bound on Constructor. However, Java's
   * type system is not up to par to capture that and still have clear code at
   * use sites. We're purposely choosing to be less type safe for overall code
   * clarity.
   */
  ConstructorAndToStringConverter(Constructor<?> constructor) {
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    checkArgument(
        parameterTypes.length == 1 &&
        parameterTypes[0].equals(String.class));
    this.constructor = constructor;
  }
  
  @Override
  public String toString(T value) {
    return value.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T fromString(String representation) {
    try {
      return (T) constructor.newInstance(representation);
    } catch (IllegalArgumentException e) {
      // proper error handling
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      // proper error handling
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      // proper error handling
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      // proper error handling
      throw new RuntimeException(e);
    }
  }

}
