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

/**
 * A converter handling {@code null} values and delegating the others.
 */
public abstract class NullHandlingConverter<T> implements Converter<T> {

  public T fromString(String representation) {
    return representation == null ? null : fromNonNullableString(representation);
  }

  protected abstract T fromNonNullableString(String representation);

  public String toString(T value) {
    return value == null ? null : nonNullableToString(value);
  }

  protected abstract String nonNullableToString(T value);

}
