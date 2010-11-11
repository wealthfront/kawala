/**
 * Copyright 2009 Wealthfront Inc. Licensed under the Apache License,
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

import com.kaching.platform.common.Option;

public class AllowNetworkMulticastTest extends LessIOSecurityManagerTestHelper {
  protected class DisallowedOperation implements RunnableWithException {
    public void run() throws IOException {
      DatagramSocket socket = new DatagramSocket(0);
      InetAddress group = InetAddress.getByName("230.0.0.1");
      String buffer = "foo";
      DatagramPacket packet = new DatagramPacket(buffer.getBytes(),
          buffer.length(), group, 4446);
      socket.send(packet);
    }
  }

  @AllowNetworkMulticast
  @AllowNetworkListen(ports = { 0 })
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
}
