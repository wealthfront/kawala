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

import static com.google.common.collect.ImmutableMap.builder;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

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
    return s2o.get(representation);
  }

  @Override
  protected String nonNullableToString(T value) {
    return o2s.get(value);
  }

  /**
   * Returns a map of the enum values passed in, using the lower case
   * name as the key. This allows a converter for an enum type to be
   * expresses as {@code super(getMap(EnumType.values()))}
   */
  protected static <T extends Enum<T>> Map<String, T> getMap(T[] values) {
    ImmutableMap.Builder<String, T> builder = builder();
    for (T value : values) {
      builder.put(value.name().toLowerCase(), value);
    }
    return builder.build();
  }

}
