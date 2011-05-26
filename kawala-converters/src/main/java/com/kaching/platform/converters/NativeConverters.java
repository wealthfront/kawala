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

import static java.lang.String.format;

class NativeConverters {

  static abstract class ConverterWithToString<T> implements Converter<T> {
    @Override
    public String toString(T value) {
      return value.toString();
    }
  }

  static final Converter<String> C_STRING = new ConverterWithToString<String>() {
    @Override
    public String fromString(String representation) {
      return representation;
    }
  };

  static final Converter<Integer> C_INT = new ConverterWithToString<Integer>() {
    @Override
    public Integer fromString(String representation) {
      return Integer.parseInt(representation);
    }
  };

  static final Converter<Double> C_DOUBLE = new ConverterWithToString<Double>() {
    @Override
    public Double fromString(String representation) {
      return Double.parseDouble(representation);
    }
  };

  static final Converter<Long> C_LONG = new ConverterWithToString<Long>() {
    @Override
    public Long fromString(String representation) {
      return Long.parseLong(representation);
    }
  };

  static final Converter<Short> C_SHORT = new ConverterWithToString<Short>() {
    @Override
    public Short fromString(String representation) {
      return Short.parseShort(representation);
    }
  };

  static final Converter<Character> C_CHAR = new ConverterWithToString<Character>() {
    @Override
    public Character fromString(String representation) {
      if (representation.length() != 1) {
        throw new IllegalArgumentException(format(
            "For input string: \"%s\"", representation));
      }
      return representation.charAt(0);
    }
  };

  static final Converter<Boolean> C_BOOLEAN = new ConverterWithToString<Boolean>() {
    @Override
    public Boolean fromString(String representation) {
      String trimmed = representation.trim();
      if ("true".equalsIgnoreCase(trimmed)) {
        return true;
      }
      if ("false".equalsIgnoreCase(trimmed)) {
        return false;
      }
      throw new IllegalArgumentException(String.format("representation is not a valid boolean : %s", representation));
    }
  };

  static final Converter<Float> C_FLOAT = new ConverterWithToString<Float>() {
    @Override
    public Float fromString(String representation) {
      return Float.parseFloat(representation);
    }
  };

  static final Converter<Byte> C_BYTE = new ConverterWithToString<Byte>() {
    @Override
    public Byte fromString(String representation) {
      return Byte.parseByte(representation);
    }
  };

}
