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
package com.kaching.platform.common.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Implementation of a {@link ParameterizedType}.
 */
// TODO(pascal): replace by Guice's implementation com.google.inject.internal.MoreTypes.ParameterizedTypeImpl
// Use Types.newParameterizedType(Type, Type[])
public class ParameterizedTypeImpl implements ParameterizedType {

  private final Type rawType;
  private final Type[] actualTypeParameters;

  public ParameterizedTypeImpl(Type type, Type[] parameters) {
    this.rawType = type;
    this.actualTypeParameters = parameters;
  }

  public Type[] getActualTypeArguments() {
    return actualTypeParameters;
  }

  public Type getOwnerType() {
    return null;
  }

  public Type getRawType() {
    return rawType;
  }

}
