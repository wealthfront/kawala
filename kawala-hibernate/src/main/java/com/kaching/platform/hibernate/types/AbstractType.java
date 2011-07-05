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
package com.kaching.platform.hibernate.types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

/**
 * Common implementation of {@link UserType} and {@link CompositeUserType} for
 * mutable and immutable values.
 */
public abstract class AbstractType {

  /**
   * Delegating to object's {@link #equals} method with proper {@code null}
   * checks.
   */
  public final boolean equals(Object x, Object y)
      throws HibernateException {
    if (x == y) {
      return true;
    } else if (x == null || y == null) {
      return false;
    } else {
      return x.equals(y);
    }
  }

  /**
   * Delegating to object's {@link #hashCode()} method.
   */
  public final int hashCode(Object value) throws HibernateException {
    return value.hashCode();
  }

}
