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

import java.lang.reflect.Type;

import com.google.common.base.Function;
import com.google.inject.TypeLiteral;
import com.kaching.platform.common.Option;

/**
 * Binder to configure the instantiator.
 */
public interface ConverterBinder {

  /**
   * Registers a converter for a specific type.
   */
  <T> ConverterSpecifier<T> registerFor(Class<T> type);

  /**
   * Registers a converter for a specific type.
   */
  <T> ConverterSpecifier<T> registerFor(TypeLiteral<T> type);

  /**
   * Registers a function that might know how to create converters for some
   * types. If more than one function can create a converter for a specific
   * type, instantiation fails.
   */
  void register(Function<Type, Option<? extends Converter<?>>> function);

}
