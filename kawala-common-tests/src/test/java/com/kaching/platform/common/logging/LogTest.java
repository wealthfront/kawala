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
package com.kaching.platform.common.logging;

import static org.apache.log4j.LogManager.getLogger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.kaching.platform.common.logging.Log;

public class LogTest {

  private static final Log log = new Log("foo");

  @Before
  public void before() {
    Logger logger = getLogger("foo");
    logger.setLevel(Level.TRACE);
  }

  @Test
  public void errorThrowableStringDoesNotFormat() {
    log.error(new NullPointerException(), "fooo%s");
  }

  @Test
  public void warnThrowableStringDoesNotFormat() {
    log.warn(new NullPointerException(), "fooo%s");
  }

  @Test
  public void infoThrowableStringDoesNotFormat() {
    log.info(new NullPointerException(), "fooo%s");
  }

  @Test
  public void debugThrowableStringDoesNotFormat() {
    log.debug(new NullPointerException(), "fooo%s");
  }

  @Test
  public void traceThrowableStringDoesNotFormat() {
    log.trace(new NullPointerException(), "fooo%s");
  }

}
