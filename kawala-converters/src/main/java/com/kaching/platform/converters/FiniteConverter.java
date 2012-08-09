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
package com.kaching.platform.converters;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;

import java.util.Map;

/**
 * A converter for a finite set of values.
 */
public class FiniteConverter<T> extends NullHandlingConverter<T> {

  private final Map<String, T> s2o;
  private final Map<T, String> o2s;

  public FiniteConverter(Map<String, T> conversion) {
    this.s2o = conversion;
    this.o2s = newHashMap();
    for (Map.Entry<String, T> e : s2o.entrySet()) {
      o2s.put(e.getValue(), e.getKey());
    }
  }

  @Override
  protected T fromNonNullableString(String representation) {
    T o = s2o.get(representation);
    if (o != null) {
      return o;
    } else {
      throw new IllegalArgumentException(format("Invalid representation: \"%s\"", representation));
    }
  }

  @Override
  protected String nonNullableToString(T value) {
    String s = o2s.get(value);
    if (s != null) {
      return s;
    } else {
      throw new IllegalArgumentException();
    }
  }

}
