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
package com.kaching.platform.common.values;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.MapMaker;
import com.kaching.platform.common.Option;

/**
 * Utility class to help with numbered enums. A numbered enum is one that
 * implements {@link NumberedValue}.
 */
public class NumberedEnum {

  private static ConcurrentMap<Class<?>, Map<Integer, Enum<?>>> mappings =
      new MapMaker().makeComputingMap(new Function<Class<?>, Map<Integer, Enum<?>>>() {
        @Override
        public Map<Integer, Enum<?>> apply(Class<?> from) {
          try {
            Builder<Integer, Enum<?>> builder = ImmutableMap.<Integer, Enum<?>> builder();
            // Multiple casts valid from type parameter bounds declared in valueOf().
            Method method = from.getMethod("values");
            method.setAccessible(true);
            Enum<?>[] values = (Enum<?>[]) method.invoke(null);
            for (Enum<?> value : values) {
              builder.put(((NumberedValue)value).getNumber(), value);
            }
            return builder.build();
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        }
      });

  /**
   * Returns the enum constant of the specified enum type with the specified
   * number. (This function is similar to {@link Enum#valueOf(Class, String)}.)
   *
   * @throws NullPointerException if the {@code value} is {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E> & NumberedValue> Option<E> valueOf(final Class<E> type, int number) {
    checkNotNull(type);
    E value = (E) mappings.get(type).get(number);
    return Option.of(value);
  }
}
