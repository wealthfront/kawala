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

import static java.lang.String.format;
import static java.lang.Thread.currentThread;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.List;
import java.util.Set;

import org.junit.internal.runners.BeforeAndAfterRunner;
import org.junit.internal.runners.TestMethodRunner;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kaching.platform.common.logging.Log;

/**
 * A {@link SecurityManager} to spotlight and minimize IO access while allowing
 * fine-grained control access to IO resoucres.
 *
 * This class was designed to draw attention to any IO (file and network) your
 * test suite may perform under the hood. IO not only slows down your test
 * suite, but unit tests that accidentally modify their environment may result
 * to flakey builds.
 *
 * Should a unit test need to perform IO, you may grant fine-grained permission
 * by annotating the container class with {@link AllowDNSResolution},
 * {@link AllowExternalProcess}, {@link AllowLocalFileAccess},
 * {@link AllowNetworkAccess}, or {@link AllowNetworkMulticast}. Some of these
 * annotations allow further refinement via parameters.
 *
 * <i>Usage.</i> To use the {@link LessIOSecurityManager}, you must set the
 * "java.security.manager" system property to
 * "com.kaching.platform.testing.LessIOSecurityManager", or your subclass.
 *
 * <i>Usage via command-line arguments.</i> You may add
 * "-Djava.security.manager=com.kaching.platform.testing.LessIOSecurityManager"
 * to your command-line invocation of the JVM to use this class as your
 * {@link SecurityManager}.
 *
 * <i>Usage via Ant.</i> You may declare the "java.security.manager" system
 * property in the "junit" element of your "build.xml" file. You <b>must</b> set
 * the "fork" property to ensure a new JVM, with this class as the
 * {@link SecurityManager} is utilized.
 *
 * <pre>
 * {@code
 * <junit fork="true">
 *   <sysproperty key="java.security.manager" value="com.kaching.platform.testing.LessIOSecurityManager" />
 *   ...
 * </junit>
 * }
 * </pre>
 *
 * <i>Performance.</i> Circa late 2010, the {@link LessIOSecurityManager}'s
 * impact on the performance of our test suite was less than 1.00%.
 *
 * @see {@link AllowDNSResolution}, {@link AllowExternalProcess},
 *      {@link AllowLocalFileAccess}, {@link AllowNetworkAccess}, and
 *      {@link AllowNetworkMulticast}
 */
public class LessIOSecurityManager extends SecurityManager {

  private static final Log log = Log.getLog(LessIOSecurityManager.class);
  protected static final String JAVA_HOME = System.getProperty("java.home");
  protected static final String PATH_SEPARATOR = System.getProperty("path.separator");
  protected static final List<String> CP_PARTS = ImmutableList.of(System.getProperty("java.class.path").split(PATH_SEPARATOR));
  private static final Set<Class<?>> whitelistedClasses = ImmutableSet.<Class<?>>of(
                                                            java.lang.ClassLoader.class,
                                                            java.net.URLClassLoader.class);

  /**
   * Any subclasses that override this method <b>must</b> include any Class<?>
   * elements returned by {@link LessIOSecurityManager#getWhitelistedClasses()}.
   * The recommended pattern is:
   * <blockquote><pre>
   * {@code
   private final Set<Class<?>> whitelistedClasses = ImmutableSet.<Class<?>>builder()
                                                      .addAll(parentWhitelistedClasses)
                                                      .add(javax.crypto.Cipher.class)
                                                      .add(javax.xml.xpath.XPathFactory.class)
                                                      .build();
   protected Set<Class<?>> getWhitelistedClasses() { return whitelistedClasses; }
   }
   </pre></blockquote>
   */
  protected Set<Class<?>> getWhitelistedClasses() {
    return whitelistedClasses;
  }

  private final boolean reporting;

  public LessIOSecurityManager() {
    this(true);
  }

  protected LessIOSecurityManager(boolean reporting) {
    this.reporting = reporting;
  }

  // {{ Allowed only via {@link @AllowNetworkAccess}, {@link @AllowDNSResolution}, or {@link @AllowNetworkMulticast})
  protected void checkDNSResolution(Class<?>[] classContext) throws CantDoItException {
    if (traceWithoutExplicitlyAllowedClass(classContext)) {
      checkClassContextPermissions(classContext, new Predicate<Class<?>>() {
        @Override
        public boolean apply(Class<?> input) {
          if ((input.getAnnotation(AllowDNSResolution.class) != null)
              || (input.getAnnotation(AllowNetworkMulticast.class) != null)
              || (input.getAnnotation(AllowNetworkListen.class) != null)
              || (input.getAnnotation(AllowNetworkAccess.class) != null)) {
            return true;
          }
          return false;
        }

        @Override
        public String toString() {
          return String.format("@AllowDNSResolution permission");
        }
      });
    }
  }

  protected void checkNetworkEndpoint(final String host, final int port, final String description) throws CantDoItException {
    Class<?>[] classContext = getClassContext();

    if (port == -1) {
      checkDNSResolution(classContext);
      return;
    }

    if (traceWithoutExplicitlyAllowedClass(classContext)) {
      checkClassContextPermissions(classContext, new Predicate<Class<?>>() {
        @Override
        public boolean apply(Class<?> input) {
          AllowNetworkAccess a = input.getAnnotation(AllowNetworkAccess.class);
          if (a == null) {
            return false;
          }

          for (String endpoint : a.endpoints()) {
            String[] parts = endpoint.split(":");
            String portAsString = Integer.toString(port);
            if ((parts[0].equals(host) && parts[1].equals(portAsString))
                || (parts[0].equals("*") && parts[1].equals(portAsString))
                || (parts[0].equals(host) && parts[1].equals("*"))) {
              return true;
            }
          }
          return false;
        }

        @Override
        public String toString() {
          return String.format("@AllowNetworkAccess permission for %s:%d (%s)",
              host, port, description);
        }
      });
    }
  }

  @Override
  public void checkAccept(String host, int port) throws CantDoItException {
    checkNetworkEndpoint(host, port, "accept");
  }

  @Override
  public void checkConnect(String host, int port, Object context) throws CantDoItException {
    checkNetworkEndpoint(host, port, "connect");
  }

  @Override
  public void checkConnect(String host, int port) throws CantDoItException {
    checkNetworkEndpoint(host, port, "connect");
  }

  @Override
  public void checkListen(final int port) throws CantDoItException {
    Class<?>[] classContext = getClassContext();
    if (traceWithoutExplicitlyAllowedClass(classContext)) {
      checkClassContextPermissions(classContext, new Predicate<Class<?>>() {
        @Override
        public boolean apply(Class<?> input) {
          AllowNetworkListen a = input.getAnnotation(AllowNetworkListen.class);
          if (a == null) {
            return false;
          }

          for (int p : a.ports()) {
            if (p == port) {
              return true;
            }
          }
          return false;
        }

        @Override
        public String toString() { return String.format("@AllowNetworkListen permission for port %d", port); }
      });
    }
  }

  @Override
  public void checkMulticast(InetAddress maddr) throws CantDoItException {
    Class<?>[] classContext = getClassContext();
    if (traceWithoutExplicitlyAllowedClass(classContext)) {
      checkClassContextPermissions(classContext, new Predicate<Class<?>>() {
        @Override
        public boolean apply(Class<?> input) {
          AllowNetworkMulticast a = input
              .getAnnotation(AllowNetworkMulticast.class);
          if (a != null) {
            return true;
          } else {
            return false;
          }
        }

        @Override
        public String toString() {
          return String.format("@AllowNetworkMulticast permission");
        }
      });
    }
  }

  @Override
  public void checkMulticast(InetAddress maddr, byte ttl) throws CantDoItException {
    checkMulticast(maddr);
  }

  // }}

  // {{ Allowed only via {@link @AllowLocalFileAccess}
  protected void checkFileAccess(final String file, final String description) throws CantDoItException {
    Class<?>[] classContext = getClassContext();
    if (traceWithoutExplicitlyAllowedClass(classContext)) {
      if (file.startsWith(JAVA_HOME)) {
        // Files in JAVA_HOME are always allowed
        return;
      }

      // Ant's JUnit task writes
      if (file.startsWith("/tmp/junit")) {
        return;
      }

      /*
       * Although this is an expensive operation, it needs to be here, in a
       * suboptimal location to avoid ClassCircularityErrors that can occur when
       * attempting to load an anonymous class.
       */
      for (String part : CP_PARTS) {
        if (file.startsWith(part)) {
          // Files in the CLASSPATH are always allowed
          return;
        }
      }

      try {
        checkClassContextPermissions(classContext, new Predicate<Class<?>>() {
          @Override
          public boolean apply(Class<?> input) {
            AllowLocalFileAccess a = input
                .getAnnotation(AllowLocalFileAccess.class);
            if (a == null) {
              return false;
            }

            for (String p : a.paths()) {
              if ((p.equals("*"))
                  || (p.equals(file))
                  || (p.equals("%TMP_DIR%") && (file.startsWith(System.getProperty("java.io.tmpdir"))))
                  || (p.startsWith("*") && p.endsWith("*") && file.contains(p.split("\\*")[1]))
                  || (p.startsWith("*") && file.endsWith(p.replaceFirst("^\\*", "")))
                  || (p.endsWith("*") && file.startsWith(p.replaceFirst("\\*$", "")))) {
                return true;
              }
            }
            return false;
          }

          @Override
          public String toString() {
            return String.format("@AllowLocalFileAccess for %s (%s)", file,
                description);
          }
        });
      } catch (CantDoItException e) {
        throw e;
      }
    }
  }

  public void checkFileDescriptorAccess(final FileDescriptor fd,
      final String description) throws CantDoItException {
    Class<?>[] classContext = getClassContext();
    if (traceWithoutExplicitlyAllowedClass(classContext)) {
      checkClassContextPermissions(classContext, new Predicate<Class<?>>() {
        @Override
        public boolean apply(Class<?> input) {
          if (input.getAnnotation(AllowExternalProcess.class) != null) {
            // AllowExternalProcess implies @AllowLocalFileAccess({"%FD%"}),
            // since it's required.
            return true;
          }
          AllowLocalFileAccess a = input
              .getAnnotation(AllowLocalFileAccess.class);
          if (a == null) {
            return false;
          }

          for (String p : a.paths()) {
            if (p.equals("%FD%")) {
              return true;
            }
          }
          return false;
        }

        @Override
        public String toString() {
          return String.format(
              "@AllowLocalFileAccess for FileDescriptor(%s) (%s)", fd,
              description);
        }
      });
    }
  }

  @Override
  public void checkRead(String file, Object context) {
    checkFileAccess(file, "read");
  }

  @Override
  public void checkRead(String file) {
    checkRead(file, null);
  }

  @Override
  public void checkRead(final FileDescriptor fd) {
    checkFileDescriptorAccess(fd, "read");
  }

  @Override
  public void checkDelete(final String file) {
    checkFileAccess(file, "delete");
  }

  @Override
  public void checkWrite(FileDescriptor fd) {
    checkFileDescriptorAccess(fd, "write");
  }

  @Override
  public void checkWrite(String file) {
    checkFileAccess(file, "write");
  }
  // }}

  // {{ Allowed only via {@link @AllowExternalProcess}
  @Override
  public void checkExec(final String cmd) throws CantDoItException {
    Class<?>[] classContext = getClassContext();
    if (traceWithoutExplicitlyAllowedClass(classContext)) {
      checkClassContextPermissions(classContext, new Predicate<Class<?>>() {
        @Override
        public boolean apply(Class<?> input) {
          AllowExternalProcess a = input
              .getAnnotation(AllowExternalProcess.class);
          if (a != null) {
            return true;
          } else {
            return false;
          }
        }

        @Override
        public String toString() {
          return String.format("@AllowExternalProcess for %s (exec)", cmd);
        }
      });
    }
  }
  // }}

  // {{ Closely Monitored
  @Override
  public void checkExit(int status) {
    log.info("%s: exit(%d)", currentTest(getClassContext()), status);
  }

  @Override
  public void checkLink(String lib) {
    log.info("%s: System.loadLibrary(\"%s\")", currentTest(getClassContext()), lib);
  }

  @Override
  public void checkAwtEventQueueAccess() {
    log.info("%s: AwtEventQueue Access", currentTest(getClassContext()));
  }

  @Override
  public void checkPrintJobAccess() {
    log.info("%s: PrintJob Access", currentTest(getClassContext()));
  }

  @Override
  public void checkSystemClipboardAccess() {
    log.info("%s: SystemClipboard Access", currentTest(getClassContext()));
  }

  @Override
  public boolean checkTopLevelWindow(Object window) {
    log.info("%s: checkTopLevelWindow aka AWTPermission(\"showWindowWithoutWarningBanner\")", currentTest(getClassContext()));
    return true;
  }

  // }}

  // {{ Always Allowed
  @Override public void checkAccess(Thread t) {}

  @Override public void checkAccess(ThreadGroup g) {}

  @Override public void checkMemberAccess(Class<?> clazz, int which) {}

  @Override public void checkPackageAccess(String pkg) {}

  @Override public void checkPackageDefinition(String pkg) {}

  @Override public void checkSetFactory() {}

  @Override public void checkCreateClassLoader() {}

  @Override public void checkPropertiesAccess() {}

  @Override public void checkPropertyAccess(String key) {}

  @Override public void checkSecurityAccess(String target) {}
  // }}

  // {{ Undecided -- Can these be called in the real functions' stead?
  @Override
  public void checkPermission(Permission perm, Object context) {}

  @Override
  public void checkPermission(Permission perm) {}
  // }}

  private boolean isClassWhitelisted(Class<?> clazz) {
    if (getWhitelistedClasses().contains(clazz)) {
      return true;
    }

    Class<?> enclosingClass = clazz.getEnclosingClass();
    if (enclosingClass != null) {
      return isClassWhitelisted(enclosingClass);
    }

    return false;
  }

  private boolean traceWithoutExplicitlyAllowedClass(Class<?>[] classContext) {
    for (Class<?> clazz : classContext) {
      if (isClassWhitelisted(clazz)) {
        return false;
      }
    }
    return true;
  }

  private void checkClassContextPermissions(final Class<?>[] classContext, final Predicate<Class<?>> classAuthorized) throws CantDoItException {
    boolean encounteredTestMethodRunner = false;
    for (Class<?> clazz : classContext) {
      if (clazz.equals(org.junit.internal.runners.TestMethodRunner.class)) {
        encounteredTestMethodRunner = true;
      }
    }

    if (!encounteredTestMethodRunner) {
      return;
    }

    for (Class<?> clazz : classContext) {
      if (classAuthorized.apply(clazz)) {
        return;
      }
    }

    // No class on the stack trace is properly authorized, throw an exception.
    CantDoItException e = new CantDoItException(String.format("No class in the class context satisfies %s", classAuthorized));

    if (this.reporting) {
      StackTraceElement testClassStackFrame = currentTest(classContext);
      String testName = format("%s.%s():%d", testClassStackFrame.getClassName(), testClassStackFrame.getMethodName(), testClassStackFrame.getLineNumber());
      log.error("%s: No %s at %s", testName, classAuthorized, testName);
      for (StackTraceElement el : currentThread().getStackTrace()) {
        log.debug("%s: Stack: %s.%s():%d", testName, el.getClassName(), el.getMethodName(), el.getLineNumber());
      }
      for (Class<?> cl : classContext) {
        log.debug("%s: Class Context: %s %s", testName, cl.getCanonicalName(), cl);
      }

    }
    throw e;
  }

  public StackTraceElement currentTest(Class<?>[] classContext) {
    // The first class right before TestMethodRunner in the class context
    // array is the class that contains our test.
    Class<?> testClass = null;
    for (Class<?> cl : classContext) {
      if (cl.equals(TestMethodRunner.class) || cl.equals(BeforeAndAfterRunner.class)) {
        break;
      }
      testClass = cl;
    }

    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    StackTraceElement testClassStackFrame = null;
    for (StackTraceElement el : stackTrace) {
      if (el.getClassName().equals(testClass.getCanonicalName())) {
        testClassStackFrame = el;
      }
    }

    return testClassStackFrame;
  }

  public static class CantDoItException extends RuntimeException {
    private static final long serialVersionUID = -8858380898538847118L;

    public CantDoItException() {
    }

    public CantDoItException(String s) {
      super(s);
    }

    public CantDoItException(String s, CantDoItException e) {
      super(s, e);
    }
  }
}
