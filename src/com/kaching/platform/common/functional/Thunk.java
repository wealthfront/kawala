package com.kaching.platform.common.functional;

/**
 * A {@link Thunk} represents a lazy computation.
 *
 * No conditional logic in this super fast implementation!
 */
public abstract class Thunk<T> {

  private volatile Getter<T> getter = new Getter<T>() {
      Getter<T> synchedGetter = new Getter<T>() {
        @Override
        T get() {
          final T value = compute();
          getter = new Getter<T>() {
            @Override
            T get() {
              return value;
            }
          };
          synchedGetter = getter;
          return getter.get();
        }
      };
      @Override
      synchronized T get() {
        return synchedGetter.get();
      }
    };

  public final T get() {
    return getter.get();
  }

  abstract protected T compute();


  private static abstract class Getter<T> { abstract T get(); }

}
