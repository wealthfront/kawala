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

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Type;

import com.google.common.base.Function;
import com.google.inject.TypeLiteral;
import com.kaching.platform.common.Option;

public abstract class AbstractInstantiatorModule implements InstantiatorModule {

  private ConverterBinder binder;

  @Override
  public void configure(ConverterBinder binder) {
    this.binder = binder;
    configure();
    this.binder = null;
  }

  protected abstract void configure();

  protected <T> ConverterSpecifier<T> registerFor(Class<T> type) {
    checkState(binder != null);
    return binder.registerFor(type);
  }

  protected <T> ConverterSpecifier<T> registerFor(TypeLiteral<T> type) {
    checkState(binder != null);
    return binder.registerFor(type);
  }

  protected void register(Function<Type, Option<? extends Converter<?>>> function) {
    binder.register(function);
  }

}
