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

import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

class InstantiatorImpl<T> implements Instantiator<T> {

  private final Constructor<T> constructor;
  private final Converter<?>[] converters;
  private final BitSet optionality;
  private final String[] defaultValues;

  InstantiatorImpl(
      Constructor<T> constructor,
      Converter<?>[] converters,
      BitSet optionality,
      String[] defaultValues) {
    this.constructor = constructor;
    this.converters = converters;
    this.optionality = optionality;
    this.defaultValues = defaultValues;
  }

  @Override
  public T newInstance(String... values) {
    return newInstance(Arrays.asList(values));
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
              parameter = (defaultValues == null || defaultValues[i] == null) ?
                  null :
                  convert(converter, defaultValues[i]);
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

  private Object convert(Converter<?> converter, String value) {
    Object parameter = converter.fromString(value);
    if (parameter == null) {
      throw new IllegalStateException(format(
          "converter %s produced a null value", converter.getClass()));
    }
    return parameter;
  }

}
