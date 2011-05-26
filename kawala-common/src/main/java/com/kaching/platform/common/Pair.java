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

import com.google.common.base.Objects;

/**
 * An immutable pair.
 */
public class Pair<S, T> {

  public final S left;
  public final T right;

  public static <S, T> Pair<S, T> of(S left, T right) {
    return new Pair<S, T>(left, right);
  }

  public Pair(S left, T right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(left, right);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    Pair<?, ?> other = (Pair<?, ?>) obj;
    return Objects.equal(left, other.left) &&
           Objects.equal(right, other.right);
  }

  @Override
  public String toString() {
    return "(" + left + "," + right + ")";
  }

}
