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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.kaching.platform.converters.InstantiatorErrors.duplicateConverterBindingForType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.inject.TypeLiteral;
import com.kaching.platform.common.Errors;
import com.kaching.platform.common.Option;

class ConverterBinderImpl implements ConverterBinder {

  private final Errors errors;
  private final Map<TypeLiteral<?>, Converter<?>> instances = newHashMap();
  @SuppressWarnings("rawtypes")
  private final Map<TypeLiteral<?>, Class<? extends Converter>> bindings = newHashMap();
  private final List<Function<Type, Option<? extends Converter<?>>>> functions = newArrayList();

  ConverterBinderImpl(Errors errors) {
    this.errors = errors;
  }

  @Override
  public <T> ConverterSpecifier<T> registerFor(Class<T> type) {
    return new ConverterSpecifierImpl<T>(TypeLiteral.get(type));
  }

  @Override
  public <T> ConverterSpecifier<T> registerFor(TypeLiteral<T> type) {
    return new ConverterSpecifierImpl<T>(type);
  }

  @Override
  public void register(Function<Type, Option<? extends Converter<?>>> function) {
    functions.add(function);
  }

  Map<TypeLiteral<?>, Converter<?>> getInstances() {
    return instances;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  Map<TypeLiteral<?>, Class<? extends Converter<?>>> getBindings() {
    return (Map) bindings;
  }

  List<Function<Type, Option<? extends Converter<?>>>> getFunctions() {
    return functions;
  }

  class ConverterSpecifierImpl<T> implements ConverterSpecifier<T> {

    private final TypeLiteral<T> key;

    ConverterSpecifierImpl(TypeLiteral<T> key) {
      if (bindings.containsKey(key) || instances.containsKey(key)) {
        duplicateConverterBindingForType(errors, key.getType());
      }
      this.key = key;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void converter(Class<? extends Converter> type) {
      bindings.put(key, type);
    }

    @Override
    public void converter(Converter<? extends T> converter) {
      instances.put(key, converter);
    }

  }

}
