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
package com.kaching.platform.common;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class ThunkTest {

  @Test
  public void usingThunks() {
    final boolean[] called = new boolean[] { false };

    Thunk<String> t = new Thunk<String>() {
      @Override
      protected String compute() {
        if (called[0]) {
          throw new IllegalStateException();
        }
        called[0] = true;
        return "Hello";
      }
    };

    assertEquals("Hello", t.get());
    assertEquals("Hello", t.get());
  }

  static class ControlledThunk extends Thunk<String> {
    private int[] callCounter;

    ControlledThunk(int[] callCounter) {
      this.callCounter = callCounter;
    }

	@Override
    protected String compute() {
      callCounter[0]++;
      synchronized(this) {
      try {
    	wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      }
      return "Hello";
    }
  }

  @Test
  public void thunkUsedConcurrently() throws Exception {
    final int[] called = new int[] { 0 };
    final CountDownLatch latch = new CountDownLatch(2);

    final ControlledThunk t = new ControlledThunk(called);

    Thread thread1 = new Thread() {
    	@Override public void run() { latch.countDown(); t.get(); }};
    Thread thread2 = new Thread() {
    	@Override public void run() { latch.countDown(); t.get(); }};
    thread1.start();
    thread2.start();
    latch.await();

    synchronized(t) {
      t.notify();
    }
    synchronized(t) {
      t.notify();
    }

    thread1.join();
    assertEquals(1, called[0]);

    thread2.join();
    assertEquals(1, called[0]);
  }

}
