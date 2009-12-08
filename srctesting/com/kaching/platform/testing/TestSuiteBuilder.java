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
package com.kaching.platform.testing;

import static java.io.File.separator;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * A {@link TestSuite} builder.
 */
public class TestSuiteBuilder {

  private final TestSuite testSuite = new TestSuite();
  private final File root;

  private TestSuiteBuilder(File root) {
    this.root = root;
  }
  
  /**
   * Builds and returns a test suite by traversing the root directory
   * recursively and finding all the tests.
   */
  public static TestSuite buildFromRoot(File root) {
    final String testCaseBuildingSuite = builderCaller();
    return buildFromRoot(root, new TestFilter() {
      public boolean shouldIncludeTest(Class<?> test) {
        return !test.getName().equals(testCaseBuildingSuite);
      }
    });
  }
  
  /**
   * Builds and returns a test suite by traversing the root directory
   * recursively and finding all the tests matching the {@code filter}.
   */
  public static TestSuite buildFromRoot(File root, TestFilter filter) {
    TestSuiteBuilder builder = new TestSuiteBuilder(root);
    builder.addTestsInDirectory(root, filter);
    return builder.testSuite;
  }

  static String builderCaller() {
    String builderClassName = TestSuiteBuilder.class.getName();
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    boolean found = false;
    for (int i = 0; i < stackTrace.length; i++) {
      String className = stackTrace[i].getClassName();
      found = found || className.equals(builderClassName);
      if (found && !className.equals(builderClassName)) {
        return className;
      }
    }
    throw new IllegalStateException("unreachable code");
  }

  private void addTestsInDirectory(File directory, TestFilter filter) {
    for (File file : directory.listFiles()) {
      if (file.isDirectory()) {
        addTestsInDirectory(file, filter);
      } else if (file.getName().endsWith(".java")) {
        Class<?> clazz = forName(file);
        if (filter.shouldIncludeTest(clazz)) {
          if (TestCase.class.isAssignableFrom(clazz) && clazz.getAnnotation(Ignore.class) == null) {
            testSuite.addTestSuite(castToTestCase(clazz));
          } else if (hasTests(clazz) && filter.shouldIncludeTest(clazz)) {
            JUnit4TestAdapter test = new JUnit4TestAdapter(clazz);
            testSuite.addTest(test);
          }
        }
      }
    }
  }

  private boolean hasTests(Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.getAnnotation(org.junit.Test.class) != null) {
        return true;
      }
    }
    for (Annotation a : clazz.getAnnotations()) {
      if (a.annotationType().equals(RunWith.class)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends TestCase> castToTestCase(Class<?> clazz) {
    return (Class<? extends TestCase>) clazz;
  }

  private Class<?> forName(File file) {
    String name = file.toString();
    name = name.replace(root.toString() + separator, "");
    name = name.replace(".java", "");
    name = name.replace(separator, ".");
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
