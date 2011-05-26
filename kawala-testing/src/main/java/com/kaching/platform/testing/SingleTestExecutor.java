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

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

abstract class SingleTestExecutor {
  
  private final RunNotifier notifier;

  SingleTestExecutor(RunNotifier notifier) {
    this.notifier = notifier;
  }
  
  public void runSingleTest(Description description) {
    try {
      notifier.fireTestStarted(description);
      doWork();
    } catch (AssertionError e) {
      notifier.fireTestFailure(new Failure(description, e));
    } catch (Exception e) {
      notifier.fireTestFailure(new Failure(description, e));
    } finally {
      notifier.fireTestFinished(description);
    }
  }

  abstract protected void doWork() throws Exception;
  
}
