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
package com.kaching.platform.common.reflect;

import java.lang.reflect.Field;

public class ReflectUtils {

  /* Goal for this utility class:
   * - for every method producing a T, have one which takes an Errors and return Option<T> and fills
   *   the errors object
   * - generic exception handling by wrapping exceptions in a visitable object and passing callables
   *   to expression actual computations
   */

  /**
   * Gets a field from an object.
   * @param obj object from which to read the field
   * @param name the field name to read
   */
  public static Object getField(Object obj, String name) {
    try {
      Class<? extends Object> klass = obj.getClass();
      do {
        try {
          Field field = klass.getDeclaredField(name);
          field.setAccessible(true);
          return field.get(obj);
        } catch (NoSuchFieldException e) {
          klass = klass.getSuperclass();
        }
      } while (klass != null);
      throw new RuntimeException(); // true no such field exception
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
