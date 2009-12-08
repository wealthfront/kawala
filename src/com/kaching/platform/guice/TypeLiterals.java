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
package com.kaching.platform.guice;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.inject.TypeLiteral;


/**
 * Factory methods to construct {@link TypeLiteral}s.
 */
public class TypeLiterals {

  /**
   * Creates a new {@link TypeLiteral} for {@code type} parameterized by
   * {@code parameters}.
   *
   * <p>For example, the type {@code Map<Integer, String>} is constructed with
   * {@code TypeLiterals.get(Map.class, Integer.class, String.class)}.
   */
  @SuppressWarnings("unchecked")
  public static <T> TypeLiteral<T> get(Class<T> type, Type... parameters) {
    return (TypeLiteral) TypeLiteral.get(new ParameterizedTypeImpl(type, parameters));
  }

  /**
   * @see TypeLiterals#get(Class, Type...)
   */
  public static <T> TypeLiteral<T> get(Class<T> type, Class<?>... parameters) {
    return get(type, (Type[]) parameters);
  }

  /**
   * @see TypeLiterals#get(Class, Type...)
   */
  public static <T> TypeLiteral<T> get(Class<T> type, TypeLiteral<?>... literals) {
    Type[] types = new Type[literals.length];
    for (int i = 0; i < literals.length; i++) {
      types[i] = literals[i].getType();
    }
    return get(type, types);
  }

  private static class ParameterizedTypeImpl implements ParameterizedType {

    private final Type type;
    private final Type[] parameters;

    private ParameterizedTypeImpl(Type type, Type[] parameters) {
      this.type = type;
      this.parameters = parameters;
    }

    public Type[] getActualTypeArguments() {
      return parameters;
    }

    public Type getOwnerType() {
      return null;
    }

    public Type getRawType() {
      return type;
    }

    @Override
    public String toString() {
      return new StringBuilder()
          .append(convertTypeToString(type))
          .append("<")
          .append(Joiner.on(", ").join(transform(
              newArrayList(parameters),
              new Function<Type, String>() {
                @Override
                public String apply(Type from) {
                  return convertTypeToString(from);
                }
              })))
          .append(">")
          .toString();
    }

    // TODO move this to a generic type pretty printer
    private String convertTypeToString(Type t) {
      if (t instanceof Class<?>) {
        return ((Class<?>) t).getName();
      } else {
        return t.toString();
      }
    }

  }

}
