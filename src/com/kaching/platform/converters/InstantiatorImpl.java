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
import java.util.Iterator;

class InstantiatorImpl<T> implements Instantiator<T> {

  private final Constructor<T> constructor;
  private final Converter<?>[] converters;

  InstantiatorImpl(
      Constructor<T> constructor,
      Converter<?>[] converters) {
    this.constructor = constructor;
    this.converters = converters;
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
          // TODO(pascal): properly handle optionality as well as predicates.
          if (value == null) {
            throw new IllegalArgumentException(format(
                "parameter %s is not optional but null was provided",
                i + 1));
          }
          parameters[i] = converters[i].fromString(value);
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

}
