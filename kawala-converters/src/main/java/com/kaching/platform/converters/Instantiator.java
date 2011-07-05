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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Object used to instantiate and destantiate objects.
 */
public interface Instantiator<T> {

  /**
   * Creates a fresh instance of T using the provided values.
   */
  T newInstance(String... values);

  /**
   * Creates a fresh instance of T using the provided values.
   */
  T newInstance(Iterable<String> values);

  /**
   * Creates a fresh instance of T using the provided names values. A value name
   * is the name which is used as parameter name in the constructor used for
   * instantiation. Classes must be compiled with this information to use this
   * method.
   * @throws UnsupportedOperationException if the underlying class of T was not
   *     compiled in debug mode
   */
  T newInstance(Map<String, String> namedValues);

  /**
   * Destantiates an instance.
   */
  List<String> fromInstance(T instance);

  /**
   * Gets the underlying constructor used to instantiate and destantiate.
   */
  Constructor<T> getConstructor();

}
