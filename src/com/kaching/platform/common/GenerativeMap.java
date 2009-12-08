/**
 * Copyright 2009 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.common;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ForwardingMap;

/**
 *  Generates and caches new values by calling a sub-class supplied compute()
 *  function instead of returning null from get().
 */
public abstract class GenerativeMap<K, V> extends ForwardingMap<K, V> {

  private final Map<K, V> delegate;

  public GenerativeMap() {
    this(new HashMap<K,V>());
  }

  public GenerativeMap(Map<K,V> delegate) {
    this.delegate = delegate;
  }

  @Override
  protected Map<K, V> delegate() {
    return delegate;
  }

  @SuppressWarnings("unchecked")
  @Override
  public V get(Object key) {
    V value = delegate.get(key);
    if (value == null) {
      value = checkedCompute(key);
      delegate.put((K) key, value);
    }
    return value;
  }

  public void ensureValueExistsFor(K key) {
    get(key);
  }

  public V findOrCreateFor(K key) {
    return get(key);
  }

  @SuppressWarnings("unchecked")
  private V checkedCompute(Object key) {
    V value = compute((K) key);
    if (value == null) {
      throw new NullPointerException("compute() returned null in " + getClass().getName());
    }
    return value;
  }

  protected abstract V compute(K key);

}
