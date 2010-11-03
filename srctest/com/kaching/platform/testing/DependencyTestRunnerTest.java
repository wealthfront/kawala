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

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

import com.kaching.platform.testing.DependencyTestRunner.DependenciesBuilder;
import com.kaching.platform.testing.DependencyTestRunner.Violation;

public class DependencyTestRunnerTest {

  private DependenciesBuilder dependencies;

  @Before
  public void before() {
    dependencies = new DependenciesBuilder();
  }

  @Test
  public void wellFormedness1() {
    JavaPackage a = new JavaPackage("a");

    dependencies.forPackages("a").
    check("a").mayDependOn().
    assertIsVerified(newArrayList(a));
  }

  @Test
  public void wellFormedness2() {
    JavaPackage a = new JavaPackage("a");
    JavaPackage b = new JavaPackage("b");

    b.setEfferents(newArrayList(a));

    dependencies.forPackages("*").
    check("a").mayDependOn().
    check("b").mayDependOn("*").
    assertIsVerified(newArrayList(a, b));

    dependencies = new DependenciesBuilder();
    dependencies.forPackages("a").
    check("a").mayDependOn().
    assertIsVerified(newArrayList(a, b));
  }

  @Test
  public void wellFormedness3() {
    JavaPackage a = new JavaPackage("a");
    JavaPackage b = new JavaPackage("b");

    a.setEfferents(newArrayList(b));

    try {
      dependencies.forPackages("*").
      check("a").mayDependOn().
      assertIsVerified(newArrayList(a, b));
      fail("assertIsVerified should fail");
    } catch (AssertionFailedError e) {
      assertEquals(
          "1 violation(s):\n" +
          "package a cannot depend on package b",
          e.getMessage());
    }
  }

  @Test
  public void wellFormedness4() {
    JavaPackage a = new JavaPackage("a");
    JavaPackage b = new JavaPackage("b");
    JavaPackage c = new JavaPackage("c");

    a.setEfferents(newArrayList(b, c));

    try {
      dependencies.forPackages("*").
      check("a").mayDependOn().
      assertIsVerified(newArrayList(a, b));
      fail("assertIsVerified should fail");
    } catch (AssertionFailedError e) {
      assertEquals(
          "2 violation(s):\n" +
          "package a cannot depend on package b\n" +
          "package a cannot depend on package c",
          e.getMessage());
    }
  }

  @Test
  public void wellFormedness5() {
    JavaPackage a = new JavaPackage("a");
    JavaPackage ba = new JavaPackage("b.a");
    JavaPackage bb = new JavaPackage("b.b");
    JavaPackage c = new JavaPackage("c");

    a.setEfferents(newArrayList(ba, bb, c));

    try {
      dependencies.forPackages("*").
      check("a").mayDependOn("b.*").
      assertIsVerified(newArrayList(a, ba, bb, c));
      fail();
    } catch (AssertionFailedError e) {
      assertEquals(
          "1 violation(s):\n" +
          "package a cannot depend on package c",
          e.getMessage());
    }
  }

  @Test
  public void wellFormedness6() {
    JavaPackage aa = new JavaPackage("a.a");
    JavaPackage ab = new JavaPackage("a.b");
    JavaPackage ba = new JavaPackage("b.a");
    JavaPackage caa = new JavaPackage("c.a.a");

    aa.setEfferents(newArrayList(ab, ba, caa));

    try {
      dependencies.forPackages("*").
      check("a.*").mayDependOn("b.*", "c.*").
      assertIsVerified(newArrayList(aa, ab, ba, caa));
      fail();
    } catch (AssertionFailedError e) {
      assertEquals(
          "1 violation(s):\n" +
          "package a.a cannot depend on package a.b",
          e.getMessage());
    }
  }

  @Test
  public void wellFormedness7() {
    JavaPackage aa = new JavaPackage("a.a");
    JavaPackage ab = new JavaPackage("a.b");
    JavaPackage ac = new JavaPackage("a.c");
    JavaPackage ba = new JavaPackage("b.a");
    JavaPackage caa = new JavaPackage("c.a.a");

    aa.setEfferents(newArrayList(ab, ac, ba, caa));

    try {
      dependencies.forPackages("*").
      check("a.*").mayDependOn("b.*", "c.*").
      check("a.a").mayDependOn("a.b").
      assertIsVerified(newArrayList(aa, ab, ac, ba, caa));
      fail();
    } catch (AssertionFailedError e) {
      assertEquals(
          "1 violation(s):\n" +
          "package a.a cannot depend on package a.c",
          e.getMessage());
    }
  }

  @Test
  public void violationToString() {
    Violation violation = new Violation(
        new JavaPackage("a"), new JavaPackage("b"));

    JavaClass classA = new JavaClass("a.BigA");
    classA.addImportedPackage(violation.efferent);
    violation.javaPackage.addClass(classA);

    JavaClass classB = new JavaClass("a.BigB");
    classB.addImportedPackage(violation.efferent);
    violation.javaPackage.addClass(classB);

    violation.javaPackage.addClass(new JavaClass("a.BigC"));

    assertEquals(
        "package a cannot depend on package b (classes BigA, BigB)",
        violation.toString());
  }

  @Test
  public void packageNameMatchesExpression() {
    assertTrue(DependenciesBuilder.packageNameMatchesExpression("a", "*"));
    assertTrue(DependenciesBuilder.packageNameMatchesExpression("a.b", "a.*"));
    assertTrue(DependenciesBuilder.packageNameMatchesExpression("a.b.c", "a.b.*"));
    assertTrue(DependenciesBuilder.packageNameMatchesExpression("a.b.c", "a.*"));
  }
  
}
