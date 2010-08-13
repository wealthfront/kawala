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

import static com.google.inject.internal.Maps.newHashMap;

import java.util.Map;

import com.google.inject.TypeLiteral;

class ConverterBinderImpl implements ConverterBinder {

  private final Errors errors;
  private final Map<TypeLiteral<?>, Converter<?>> instances = newHashMap();
  @SuppressWarnings("unchecked")
  private final Map<TypeLiteral<?>, Class<? extends Converter>> bindings = newHashMap();

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

  Map<TypeLiteral<?>, Converter<?>> getInstances() {
    return instances;
  }

  @SuppressWarnings("unchecked")
  Map<TypeLiteral<?>, Class<? extends Converter<?>>> getBindings() {
    return (Map) bindings;
  }

  class ConverterSpecifierImpl<T> implements ConverterSpecifier<T> {

    private final TypeLiteral<T> key;

    ConverterSpecifierImpl(TypeLiteral<T> key) {
      if (bindings.containsKey(key) || instances.containsKey(key)) {
        errors.duplicateConverterBindingForType(key.getType());
      }
      this.key = key;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void converter(Class<? extends Converter> type) {
      bindings.put(key, type);
    }

    @Override
    public void converter(Converter<? extends T> converter) {
      instances.put(key, converter);
    }

  }

}
