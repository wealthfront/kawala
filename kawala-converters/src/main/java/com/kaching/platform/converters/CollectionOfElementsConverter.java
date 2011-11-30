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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;

/**
 * Converter for collections of elements, provided we have a converter for
 * each individual element.
 */
class CollectionOfElementsConverter<T extends Collection<?>> implements Converter<T> {

  @SuppressWarnings("rawtypes")
  static final Map<Class<?>, Provider<Collection<?>>> COLLECTION_KINDS =
      ImmutableMap.<Class<?>, Provider<Collection<?>>> builder()
      .put(List.class, new Provider<Collection<?>>() {
        public Collection<?> get() {
          return new ArrayList();
        }
      })
      .put(Set.class, new Provider<Collection<?>>() {
        public Collection<?> get() {
          return new HashSet();
        }
      })
      .put(Collection.class, new Provider<Collection<?>>() {
        public Collection<?> get() {
          return new ArrayList();
        }
      })
      .build();

  private final Converter<?> elementConverter;
  private final Provider<Collection<?>> collectionProvider;

  CollectionOfElementsConverter(
      Type kindOfCollection,
      Converter<?> elementConverter) {
    this.collectionProvider = COLLECTION_KINDS.get(kindOfCollection);
    this.elementConverter = elementConverter;
  }

  @Override
  public String toString(T value) {
    return Joiner.on(",").join(value);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public T fromString(String representation) {
    if (representation == null) {
      return null;
    } else {
      Collection collection = collectionProvider.get();
      if (!representation.isEmpty()) {
        for (String part : representation.split(",")) {
          collection.add(elementConverter.fromString(part));
        }
      }
      return (T) collection;
    }
  }

}
