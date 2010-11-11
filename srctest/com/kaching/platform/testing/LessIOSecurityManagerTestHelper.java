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
package com.kaching.platform.testing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.kaching.platform.common.Option;
import com.kaching.platform.testing.LessIOSecurityManager.CantDoItException;

public abstract class LessIOSecurityManagerTestHelper {
  protected interface RunnableWithException {
    public void run() throws Exception;
  }

  protected void withTemporarySM(SecurityManager sm, Runnable runnable) {
    final SecurityManager previous = System.getSecurityManager();
    System.setSecurityManager(sm);
    try {
      runnable.run();
    } finally {
      System.setSecurityManager(previous);
    }
  }

  protected void assertAllowed(SecurityManager sm, final RunnableWithException runnable, final Option<Class<? extends Exception>> expectedOption) {
    withTemporarySM(sm, new Runnable() {
      @Override
      public void run() {
        try {
          runnable.run();
        } catch (Exception e) {
          assertFalse(String.format("Action must be allowed. I should not catch a %s (%s)", e.getClass().getCanonicalName(), e.getLocalizedMessage()), e instanceof CantDoItException);
          for (Class<?> expected : expectedOption) {
            assertTrue(String.format("Expecting exception %s but received %s (%s)", expected.getCanonicalName(), e.getClass().getCanonicalName(), e.getLocalizedMessage()),e.getClass().isAssignableFrom(expected));
            return;
          }
          fail(String.format("Unexpected exception: %s (%s)", e.getClass().getCanonicalName(), e.getLocalizedMessage()));
        }
        if (expectedOption.isDefined()) {
          fail(String.format("Expected exception %s, but no exception was thrown!", expectedOption.getOrThrow().getCanonicalName()));
        }
      }
    });
  }

  protected void assertDisallowed(SecurityManager sm, final RunnableWithException runnable) {
    withTemporarySM(sm, new Runnable() {
      @Override
      public void run() {
        try {
          runnable.run();
        } catch (Exception e) {
          assertTrue(String.format("Action must be disallowed. However, no CantDoItException was thrown. Instead, I caught" +
          		" %s (%s).", e.getClass().getCanonicalName(), e.getLocalizedMessage()),
              e instanceof CantDoItException);
          return;
        }
        fail("Action must be disallowed. However, no CantDoItException was thrown.");
      }
    });
  }
}
