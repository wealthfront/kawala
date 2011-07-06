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

import static org.jmock.lib.legacy.ClassImposteriser.INSTANCE;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class SingleTestExecutorTest {

  private Mockery mockery;

  @Before
  public void before() {
    mockery = new Mockery() {{
      setImposteriser(INSTANCE);
    }};
  }
  
  @Test
  public void assertionError() {
    new SingleTestExecutor(notifier(false)) {
      @Override
      protected void doWork() throws Exception {
        assertTrue(false);
      }
    }.runSingleTest(null);
    
    mockery.assertIsSatisfied();
  }
  
  @Test
  public void randomException() {
    new SingleTestExecutor(notifier(false)) {
      @Override
      protected void doWork() throws Exception {
        throw new Exception();
      }
    }.runSingleTest(null);
    
    mockery.assertIsSatisfied();
  }
  
  @Test
  public void success() {
    new SingleTestExecutor(notifier(true)) {
      @Override
      protected void doWork() {
      }
    }.runSingleTest(null);
    
    mockery.assertIsSatisfied();
  }

  private RunNotifier notifier(final boolean success) {
    final RunNotifier notifier = mockery.mock(RunNotifier.class);
    final Sequence execution = mockery.sequence("execution");
    mockery.checking(new Expectations() {{
      one(notifier).fireTestStarted((Description) with(anything()));
          inSequence(execution);
      if (!success) {
        one(notifier).fireTestFailure((Failure) with(anything()));
            inSequence(execution);
      }
      one(notifier).fireTestFinished((Description) with(anything()));
          inSequence(execution);
    }});
    return notifier;
  }
  
}
