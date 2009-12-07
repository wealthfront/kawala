package com.kaching.platform.util.collect;

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
