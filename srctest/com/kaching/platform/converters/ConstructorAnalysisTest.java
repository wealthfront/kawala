package com.kaching.platform.converters;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.kaching.platform.converters.ConstructorAnalysis.FormalParameter;
import com.kaching.platform.converters.ConstructorAnalysis.IllegalConstructorException;

public class ConstructorAnalysisTest {

  static class NoOp {
  }

  @Test
  public void noOp() throws Exception {
    assertAssignement(NoOp.class, emptyMap());
  }

  static class OneAssignment {
    final int foo;
    OneAssignment(int foo) {
      this.foo = foo;
    }
  }

  @Test
  public void oneAssignment() throws Exception {
    assertAssignement(
        OneAssignment.class,
        ImmutableMap.of(
            "foo", "p0"));
  }

  static class TwoAssignments {
    final int foo;
    final int bar;
    TwoAssignments(int bar, int foo) {
      this.bar = bar;
      this.foo = foo;
    }
  }

  @Test
  public void twoAssignments() throws Exception {
    assertAssignement(
        TwoAssignments.class,
        ImmutableMap.of(
            "bar", "p0",
            "foo", "p1"));
  }

  static class DupAssignment {
    int foo;
    DupAssignment(int bar, int foo) {
      this.foo = bar;
      this.foo = foo;
    }
  }

  @Test
  public void dupAssignment() throws Exception {
    assertAnalysisFails(
        DupAssignment.class, "duplicate assignment to field foo");
  }

  static class KeepingSelfReference {
    KeepingSelfReference reference;
    KeepingSelfReference() {
      this.reference = this;
    }
  }

  @Test
  public void keepingSelfReference() throws Exception {
    assertAnalysisFails(
        KeepingSelfReference.class,
        "cannot assign values other than formal parameters to fields");
  }

  static class AssigningSomethingElseThanParamater {
    int data;
    AssigningSomethingElseThanParamater(int data) {
      this.data = 4;
    }
  }

  @Test
  public void assigningSomethingElseThanParamater() throws Exception {
    assertAnalysisFails(
        AssigningSomethingElseThanParamater.class,
        "illegal constructor");
  }

  static class ComplexCode {
    ComplexCode() {
      new Object();
    }
  }

  @Test
  public void complexCode() throws Exception {
    assertAnalysisFails(
        ComplexCode.class,
        "illegal constructor");
  }

  private void assertAnalysisFails(Class<?> klass, String message) throws IOException {
    try {
      ConstructorAnalysis.analyse(klass, klass.getDeclaredConstructors()[0]);
      fail("analysis should have failed");
    } catch (IllegalConstructorException e) {
      assertEquals(message, e.getMessage());
    }
  }

  private void assertAssignement(
      Class<?> klass, Map<?, ?> expected) throws IOException {
    Map<String, FormalParameter> assignements =
        ConstructorAnalysis.analyse(klass, klass.getDeclaredConstructors()[0]);
    Map<String, String> actual = newHashMap();
    for (Entry<String, FormalParameter> entry : assignements.entrySet()) {
      actual.put(entry.getKey(), entry.getValue().toString());
    }
    assertEquals(expected, actual);
  }

}
