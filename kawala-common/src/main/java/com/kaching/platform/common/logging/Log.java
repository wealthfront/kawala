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

import static com.kaching.platform.common.Strings.format;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.perf4j.commonslog.CommonsLogStopWatch;

import com.google.common.annotations.VisibleForTesting;
import com.kaching.platform.common.Pair;

/**
 * A convenient interface on top of Log4J that makes using log4j along with
 * {@link String#format(String, Object...)} more concise. Its interface is
 * similar to log4j's {@link org.apache.commons.logging.Log} with the addition
 * of varargs arguments that are used to render the any format specifiers in the
 * log message.
 */
public class Log {
  public static Log getLog(Class<?> clazz) {
    return new Log(clazz);
  }

  public static Log getLog(String name) {
    return new Log(name);
  }

  public static void logContextPut(String key, Object value) {
    MDC.put(key, value);
  }

  public static void logContextRemove(String key) {
    MDC.remove(key);
  }

  public static void withLogContext(Runnable runnable,
      Pair<String, Object>... contexts) {
    try {
      for (Pair<String, Object> context : contexts) {
        logContextPut(context.left, context.right);
      }
      runnable.run();
    } finally {
      for (Pair<String, Object> context : contexts) {
        logContextRemove(context.left);
      }
    }
  }

  private final org.apache.commons.logging.Log log;

  @VisibleForTesting
  Log(Class<?> clazz) {
    this.log = LogFactory.getLog(clazz);
  }

  @VisibleForTesting
  Log(String name) {
    this.log = LogFactory.getLog(name);
  }

  public boolean isTraceEnabled() {
    return log.isTraceEnabled();
  }

  public void trace(String message) {
    if (log.isTraceEnabled()) {
      log.trace(message);
    }
  }

  public void trace(String format, Object... args) {
    if (log.isTraceEnabled()) {
      log.trace(format(format, args));
    }
  }

  public void trace(Throwable t) {
    if (log.isTraceEnabled()) {
      log.trace(t);
    }
  }

  public void trace(Throwable t, String message) {
    if (log.isTraceEnabled()) {
      log.trace(message, t);
    }
  }

  public void trace(Throwable t, String format, Object... args) {
    if (log.isTraceEnabled()) {
      log.trace(format(format, args), t);
    }
  }

  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  public void debug(String message) {
    if (log.isDebugEnabled()) {
      log.debug(message);
    }
  }

  public void debug(String format, Object... args) {
    if (log.isDebugEnabled()) {
      log.debug(format(format, args));
    }
  }

  public void debug(Throwable t) {
    if (log.isDebugEnabled()) {
      log.debug(t == null ? null : t.getMessage(), t);
    }
  }

  public void debug(Throwable t, String message) {
    if (log.isDebugEnabled()) {
      log.debug(message, t);
    }
  }

  public void debug(Throwable t, String format, Object... args) {
    if (log.isDebugEnabled()) {
      log.debug(format(format, args), t);
    }
  }

  /**
   * Log throwable with message and stack trace according to the logging level
   * settings, as in log4j and slf4j. This is probably not what you expect.
   *
   * @param msg
   * @param t
   * @deprecated Use {@link #info(Throwable, String, Object...)} to clarify that
   *             you want the stack trace printed according to log severity
   *             settings or explicitly call toString() if you want to use the
   *             Throwable as an argument to
   *             {@link String#format(String, Object...)}.
   */
  @Deprecated
  public void info(String msg, Throwable t) {
    log.info(msg, t);
  }

  public void info(String message) {
    log.info(message);
  }

  public void info(String format, Object... args) {
    log.info(format(format, args));
  }

  public void info(Throwable t) {
    log.info(t == null ? null : t.getMessage(), t);
  }

  public void info(Throwable t, String message) {
    log.info(message, t);
  }

  public void info(Throwable t, String format, Object... args) {
    log.info(format(format, args), t);
  }

  /**
   * Log throwable with message and stack trace according to the logging level
   * settings, as in log4j and slf4j. This is probably not what you expect.
   *
   * @param msg
   * @param t
   * @deprecated Use {@link #warn(Throwable, String, Object...)} to clarify that
   *             you want the stack trace printed according to log severity
   *             settings or explicitly call toString() if you want to use the
   *             Throwable as an argument to
   *             {@link String#format(String, Object...)}.
   */
  @Deprecated
  public void warn(String msg, Throwable t) {
    log.warn(msg, t);
  }

  public void warn(String message) {
    log.warn(message);
  }

  public void warn(String format, Object... args) {
    log.warn(format(format, args));
  }

  public void warn(Throwable t) {
    log.warn(t == null ? null : t.getMessage(), t);
  }

  public void warn(Throwable t, String message) {
    log.warn(message, t);
  }

  public void warn(Throwable t, String format, Object... args) {
    log.warn(format(format, args), t);
  }

  /**
   * Log throwable with message and stack trace according to the logging level
   * settings, as in log4j and slf4j. This is probably not what you expect.
   *
   * @param msg
   * @param t
   * @deprecated Use {@link #error(Throwable, String, Object...)} to clarify
   *             that you want the stack trace printed according to log severity
   *             settings or explicitly call toString() if you want to use the
   *             Throwable as an argument to
   *             {@link String#format(String, Object...)}.
   */
  @Deprecated
  public void error(String msg, Throwable t) {
    log.error(msg, t);
  }

  public void error(String message) {
    log.error(message);
  }

  public void error(String format, Object... args) {
    log.error(format(format, args));
  }

  public void error(Throwable t) {
    log.error(t == null ? null : t.getMessage(), t);
  }

  public void error(Throwable t, String message) {
    log.error(message, t);
  }

  public void error(Throwable t, String format, Object... args) {
    log.error(format(format, args), t);
  }

  public org.apache.commons.logging.Log getLog() {
    return log;
  }

  public CommonsLogStopWatch getStopWatch() {
    return new CommonsLogStopWatch(log);
  }

  public CommonsLogStopWatch getStopWatch(String tag) {
    return new CommonsLogStopWatch(tag, log);
  }

}
