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

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

class InstantiatorImpl<T> implements Instantiator<T> {

  private final Constructor<T> constructor;
  private final Field[] fields;
  @SuppressWarnings("unchecked")
  private final Converter[] converters;
  private final BitSet optionality;
  private final String[] defaultValues;
  private final Object[] defaultConstants;
  private final String[] parameterNames;

  InstantiatorImpl(
      Constructor<T> constructor,
      Converter<?>[] converters,
      Field[] fields,
      BitSet optionality,
      String[] defaultValues,
      Object[] defaultConstants,
      String[] parameterNames) {
    this.constructor = constructor;
    this.converters = converters;
    this.fields = fields;
    this.optionality = optionality;
    this.defaultValues = defaultValues;
    this.defaultConstants = defaultConstants;
    this.parameterNames = parameterNames;
  }

  @Override
  public T newInstance(String... values) {
    return newInstance(Arrays.asList(values));
  }

  @Override
  public T newInstance(Map<String, String> namedValues) {
    if (parameterNames == null) {
      throw new UnsupportedOperationException();
    }
    List<String> values = newArrayList();
    for (String paramaterName : parameterNames) {
      values.add(namedValues.get(paramaterName));
    }
    return newInstance(values);
  }

  @Override
  public T newInstance(Iterable<String> values) {
    try {
      if (converters != null) {
        Object[] parameters = new Object[converters.length];
        Iterator<String> valuesIterator = values.iterator();
        for (int i = 0; i < converters.length; i++) {
          if (!valuesIterator.hasNext()) {
            throw new IllegalArgumentException("wrong number of arguments");
          }
          String value = valuesIterator.next();
          Converter<?> converter = converters[i];
          // TODO(pascal): properly handle predicates.
          Object parameter;
          if (value == null) {
            if (optionality.get(i)) {
              parameter =
                  (defaultValues != null && defaultValues[i] != null) ?
                  convert(converter, defaultValues[i]) :
                  (defaultConstants != null && defaultConstants[i] != null) ?
                  defaultConstants[i] :
                  null;
            } else {
              throw new IllegalArgumentException(format(
                  "parameter %s is not optional but null was provided",
                  i + 1));
            }
          } else {
            parameter = convert(converter, value);
          }
          parameters[i] = parameter;
        }
        if (valuesIterator.hasNext()) {
          throw new IllegalArgumentException("wrong number of arguments");
        }
        return constructor.newInstance(parameters);
      } else {
        return constructor.newInstance();
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      // do proper exception handling including de-wrapping exceptions
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> fromInstance(T instance) {
    // TODO(pascal): Rewrite this naive implementation. The goal is to show
    // the skeleton a full example of destantiating.
    List<String> parameters = Lists.newArrayListWithCapacity(fields.length);
    for (int i = 0; i < fields.length; i++) {
      try {
        Field field = fields[i];
        parameters.add(field == null ?
            null :
            converters[i].toString(field.get(instance)));
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return parameters;
  }

  private Object convert(Converter<?> converter, String value) {
    Object parameter = converter.fromString(value);
    if (parameter == null) {
      throw new IllegalStateException(format(
          "converter %s produced a null value", converter.getClass()));
    }
    return parameter;
  }

  @Override
  public Constructor<T> getConstructor() {
    return constructor;
  }

  @Override
  public String toString() {
    return "instantiator " + constructor.toString().replaceFirst("(public|protected|private) ", "");
  }

}
