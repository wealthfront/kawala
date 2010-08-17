/**
 * Copyright 2010 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.converters;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

import java.util.Map;

/**
 * A converter for enumerations.
 */
public class EnumConverter<E extends Enum<E>> extends FiniteConverter<E> {

  public EnumConverter(E[] values) {
    super(enumMap(values));
  }

  public EnumConverter(Class<E> clazz) {
    this(clazz.getEnumConstants());
  }

  private static <E extends Enum<E>> Map<String, E> enumMap(E[] values) {
    Map<String, E> maps = newHashMapWithExpectedSize(values.length);
    for (E value : values) {
      maps.put(value.name(), value);
    }
    return maps;
  }

}
