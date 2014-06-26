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

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.kaching.platform.common.Option;

@Ignore("broken on java 7")
public class AllowNetworkListenTest extends LessIOSecurityManagerTestHelper {
  protected class DisallowedOperation implements RunnableWithException {
    public void run() throws IOException {
      ServerSocket s1 = new ServerSocket(59413);
      ServerSocket s2 = new ServerSocket(48819);
      s1.close();
      s2.close();
    }
  }

  @AllowNetworkListen(ports = { 59413 })
  protected class MisannotatedOperation extends DisallowedOperation {
    @Override
    public void run() throws IOException {
      super.run();
    }
  }

  @AllowNetworkListen(ports = { 59413, 48819 })
  protected class AllowedOperation extends DisallowedOperation {
    @Override
    public void run() throws IOException {
      super.run();
    }
  }

  LessIOSecurityManager sm;

  @Before
  public void setupSecurityManager() {
    sm = new LessIOSecurityManager();
  }

  @Test
  public void testNonAnnotatedOperation() {
    assertDisallowed(sm, new DisallowedOperation());
  }

  @Test
  public void testAnnotatedOperation() {
    assertAllowed(sm, new AllowedOperation(),
        Option.<Class<? extends Exception>> none());
  }

  @Test
  public void testMisannotatedOperation() {
    assertDisallowed(sm, new MisannotatedOperation());
  }
}
