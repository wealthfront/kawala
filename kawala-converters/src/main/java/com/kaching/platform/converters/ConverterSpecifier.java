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
 * Specifies which converter to use for a specific type.
 *
 * @param <T> the type for which to specify a converter
 */
public interface ConverterSpecifier<T> {

  /**
   * Specifies the type of converter to use.
   * @param type the converter's type to use
   */
  @SuppressWarnings("rawtypes")
  void converter(Class<? extends Converter> type);

  /**
   * Specifies the converter to use.
   * @param converter the converter to use
   */
  void converter(Converter<? extends T> converter);

}
