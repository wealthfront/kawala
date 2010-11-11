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
