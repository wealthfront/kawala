/**
 * Copyright 2009 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.common.functional;

import static org.junit.Assert.assertEquals;

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
  
  @Test
  public void thunkUsedConcurrently() throws Exception {
    final int[] called = new int[] { 0 };
    
    final Thunk<String> t = new Thunk<String>() {
      @Override
      protected String compute() {
        called[0]++;
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        return "Hello";
      }
    };
    
    Thread thread1 = new Thread() { @Override public void run() { t.get(); }};
    Thread thread2 = new Thread() { @Override public void run() { t.get(); }};
    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();
    
    assertEquals(1, called[0]);
  }

}
