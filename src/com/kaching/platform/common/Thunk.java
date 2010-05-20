package com.kaching.platform.common;

import com.google.common.annotations.VisibleForTesting;

/**
 * A lazy computation. No conditional logic in this super fast implementation!
 */
public abstract class Thunk<T> {

  /* Indicates whether this thunk is evaluated or not. This field only
   * approximates the indication as a correct implementation requires an
   * atomic reference change on a pair (Getter, boolean). It is only used for
   * testing and this extra cost is therefore not needed.
   */
  private volatile boolean evaluated;

  private volatile Getter<T> threadsafeGetter = new Getter<T>() {
      Getter<T> getter = new Getter<T>() {
        T get() {
          final T value = compute();
          evaluated = true;
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

  protected abstract T compute();


  private static abstract class Getter<T> { abstract T get(); }


  @VisibleForTesting
  boolean isEvaluated() {
    return evaluated;
  }

}
