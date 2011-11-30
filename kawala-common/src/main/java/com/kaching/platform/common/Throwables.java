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

public final class Throwables {
  private Throwables() {}

  /**
   * Propagates {@code throwable} as-is, even if it is not an instance of
   * {@link RuntimeException} or {@link Error}. Example usage:
   * <pre>
   *   T doSomething() { // throws clause not necessary
   *     try {
   *       return someMethodThatCouldThrowAnIOException();
   *     } catch (IOException e) {
   *       throw Throwables.unchecked(e);
   *     }
   *   }
   * </pre>
   */
  public static RuntimeException unchecked(Throwable throwable) {
    Throwables.<RuntimeException> throwUnchecked(throwable);
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <E extends Throwable> void throwUnchecked(Throwable throwable) throws E {
    throw (E) throwable;
  }

}
