package com.kaching.platform.common.functional;

/**
 * A lazy computation. No conditional logic in this super fast implementation!
 */
public abstract class Thunk<T> {

  private volatile Getter<T> threadsafeGetter = new Getter<T>() {
      Getter<T> getter = new Getter<T>() {
        T get() {
          final T value = compute();
          threadsafeGetter = getter = new Getter<T>() {
            T get() {
              return value;
            }
          };
          return value;
        }
      };
      synchronized T get() {
        return getter.get();
      }
    };

  public final T get() {
    return threadsafeGetter.get();
  }

  abstract protected T compute();


  private static abstract class Getter<T> { abstract T get(); }

}
