package com.kaching.platform.converters;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyMap;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;
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

  @Test
  public void assignmentsToNatives() throws Exception {
    assertAssignement(
        Natives.class,
        ImmutableMap.<String, String> builder()
            .put("i", "p0")
            .put("d", "p1")
            .put("s", "p2")
            .put("c", "p3")
            .put("l", "p4")
            .put("b", "p5")
            .put("f", "p6")
            .put("y", "p7")
            .build());
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
    assertAssignement(
        KeepingSelfReference.class,
        Collections.emptyMap());
  }

  static class AssigningSomethingElseThanParamater {
    int data;
    AssigningSomethingElseThanParamater(int data) {
      this.data = 4;
    }
  }

  @Test
  public void assigningSomethingElseThanParamater() throws Exception {
    assertAssignement(
        AssigningSomethingElseThanParamater.class,
        Collections.emptyMap());
  }

  static class ObjectInstantiation1 {
    ObjectInstantiation1() {
      new Object();
    }
  }

  @Test
  public void objectInstantiation1() throws Exception {
    assertAssignement(
        ObjectInstantiation1.class,
        Collections.emptyMap());
  }

  static class ObjectInstantiation2 {
    Object foo;
    ObjectInstantiation2() {
      this.foo = new Object();
    }
  }

  @Test
  public void objectInstantiation2() throws Exception {
    assertAssignement(
        ObjectInstantiation2.class,
        Collections.emptyMap());
  }

  static class CheckArgument1 {
    final BigDecimal value;
    CheckArgument1(BigDecimal value) {
      checkArgument(ZERO.compareTo(value) <= 0);
      this.value = value;
    }
  }

  @Test
  @Ignore
  public void checkArgument1() throws Exception {
    assertAssignement(
        CheckArgument1.class,
        ImmutableMap.of(
            "value", "p0"));
  }

  static class DoingMathOperation1 {
    int foo;
    DoingMathOperation1(int foo) {
      this.foo = foo + 9;
    }
  }

  @Test
  public void doingMathOperation1() throws Exception {
    assertAnalysisFails(
        DoingMathOperation1.class,
        "cannot assign non-idempotent expression p0 + 9 to field");
  }

  static class DoingMathOperation2 {
    long foo;
    DoingMathOperation2(long foo) {
      this.foo = foo * 5;
    }
  }

  @Test
  public void doingMathOperation2() throws Exception {
    assertAnalysisFails(
        DoingMathOperation2.class,
        "cannot assign non-idempotent expression p0 * 5 to field");
  }

  static class ConstantHolder {
    static int CONSTANT = 4;
  }

  static class DoingMathOperation3 {
    int foo;
    DoingMathOperation3(int foo) {
      this.foo = foo % ConstantHolder.CONSTANT;
    }
  }

  @Test
  public void doingMathOperation3() throws Exception {
    assertAnalysisFails(
        DoingMathOperation3.class,
        "cannot assign non-idempotent expression p0 % com.kaching.platform.converters.ConstructorAnalysisTest$ConstantHolder#CONSTANT to field");
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
