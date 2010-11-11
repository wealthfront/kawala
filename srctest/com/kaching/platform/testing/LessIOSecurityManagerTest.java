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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.Test;

import com.kaching.platform.common.Option;
import com.kaching.platform.testing.LessIOSecurityManager.CantDoItException;

public class LessIOSecurityManagerTest extends LessIOSecurityManagerTestHelper {

  @Test
  public void testNoSecurityManager() {
    withTemporarySM(null, new Runnable() {
      @Override
      public void run() {
        try {
          openSocket();
        } catch (Exception e) {
          assertTrue(String.format("Received %s (%s) instead of IOException",
              System.getSecurityManager(), e.getClass().getCanonicalName(), e.getLocalizedMessage()),
              e instanceof IOException);
        }
      }
    });
  }

  @Test
  public void testSecurityManager() {
    withTemporarySM(SECURITY_MANAGER, new Runnable() {
      @Override
      public void run() {
        try {
          openSocket();
        } catch (Exception e) {
          assertTrue(String.format("Received %s (%s) instead of CantDoItException",
              e.getClass().getCanonicalName(), e.getLocalizedMessage()),
              e instanceof CantDoItException);
        }
      }
    });
  }

  @Test
  public void testAssertAllowed() {
    assertAllowed(
        new RunnableWithException() {
          @Override
          public void run() throws Exception {
            throw new UnsupportedOperationException();
          }
        },
        Option.<Class<? extends Exception>> of(UnsupportedOperationException.class));

    try {
      assertAllowed(new RunnableWithException() {
        @Override
        public void run() throws Exception {
          throw new CantDoItException();
        }
      }, Option.<Class<? extends Exception>> none());
    } catch (AssertionError e) {
      // Success
    }
  }

  @Test
  public void testAssertDisallowed() {
    assertDisallowed(new RunnableWithException() {
      @Override
      public void run() throws Exception {
        throw new CantDoItException();
      }
    });

    try {
      assertDisallowed(new RunnableWithException() {
        @Override
        public void run() throws Exception {
          // Intentionally left empty.
        }
      });
    } catch (AssertionError e) {
      // Success
    }
  }

  private void openSocket() throws IOException {
    (new Socket()).connect(new InetSocketAddress("localhost", 1));
  }

}
